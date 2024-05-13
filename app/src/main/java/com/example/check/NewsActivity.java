package com.example.check;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.drawerlayout.widget.DrawerLayout;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity {

    private LinearLayout container;
    private Spinner spinner;
    private List<Pair<String, String>> spinnerItemsList;
    private DrawerHandler drawerManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_news);

        spinner = findViewById(R.id.spinner);
        spinnerItemsList = new ArrayList<>();

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ImageView burgerIcon = findViewById(R.id.burgerIcon);
        ImageView phoneIcon = findViewById(R.id.phoneIcon);
        LinearLayout navView = findViewById(R.id.nav_view);
        drawerManager = new DrawerHandler(this, drawerLayout, burgerIcon, phoneIcon, navView);


        loadData();

        // Устанавливаем слушатель для выбора элемента в Spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                // Получаем выбранный элемент
                Pair<String, String> selectedPair = spinnerItemsList.get(position);
                // Показываем Toast с названием и значением выбранного элемента
                container.removeAllViews();
                new NewsActivity.ParseHtmlTask(NewsActivity.this, container, selectedPair.second).execute();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Ничего не делаем, если ничего не выбрано
            }
        });




        container = findViewById(R.id.container);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.green)); // Замените на ваш цвет
        }


    }


    private void loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document = Jsoup.connect("https://asbleasing.by/news/").get();
                    Elements selectItemInputElements = document.select(".select-item__input");
                    for (Element selectItemInputElement : selectItemInputElements) {
                        Element spanElement = selectItemInputElement.nextElementSibling();
                        String name = spanElement != null ? spanElement.text() : "";
                        String value = selectItemInputElement.attr("value");
                        spinnerItemsList.add(new Pair<>(name, value));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Создаем список только с названиями
                            List<String> spinnerNamesList = new ArrayList<>();
                            for (Pair<String, String> pair : spinnerItemsList) {
                                spinnerNamesList.add(pair.first);
                            }

                            String[] spinnerNames = spinnerNamesList.toArray(new String[0]);
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, spinnerNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(adapter);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public class ParseHtmlTask extends AsyncTask<Void, Void, Void> {
        private Context context;
        private LinearLayout container;
        private ProgressDialog progressDialog;
        private String valueSpinner;

        public ParseHtmlTask(Context context, LinearLayout container, String valueSpinner) {
            this.context = context;
            this.container = container;
            this.valueSpinner = valueSpinner;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Создаем ProgressDialog
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Загрузка...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Скрываем ProgressDialog после загрузки данных
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // Получаем HTML-документ с веб-сайта с учетом выбранного значения Spinner
                Document document = Jsoup.connect("https://asbleasing.by/news/?selectedLabel=" + valueSpinner + "&PAGEN_1=1").get();

                // Находим элементы pagination__item на странице
                Elements paginationItems = document.select(".pagination__item");

                // Получаем количество элементов pagination__item
                int totalPages = paginationItems.size() > 0 ? paginationItems.size() : 2;


                for (int page = 1; page < totalPages; page++) {
                    // Получаем HTML-документ с веб-сайта для каждой страницы
                    document = Jsoup.connect("https://asbleasing.by/news/?selectedLabel=" + valueSpinner + "&PAGEN_1=" + page).get();
                    Log.d("Selected Value", "https://asbleasing.by/news/?selectedLabel=" + valueSpinner + "&PAGEN_1=" + page);
                    // Находим все элементы с классом "news-card"
                    Elements partners = document.select(".news-card");

                    // Перебираем каждый элемент "news-card"
                    for (final Element partner : partners) {
                        // Находим элементы "news-card__title" и "news-card__img" внутри текущего партнера
                        Element titleElement = partner.selectFirst(".news-card__title");
                        Element imageElement = partner.selectFirst(".news-card__img img");
                        Element templateTextElement = partner.selectFirst(".news-card__text");
                        Element dateTextElement = partner.selectFirst(".news-card__date");
                        Element srcToMoreInfoElement = partner.selectFirst("a.button--fill-green");

                        // Получаем текст заголовка и изображения
                        final String title = titleElement != null ? titleElement.text() : "";
                        final String imageUrl = imageElement != null ? "https://asbleasing.by" + imageElement.attr("src") : "";
                        final String templateText = templateTextElement != null ? templateTextElement.text() : "";
                        final String dateText = dateTextElement != null ? dateTextElement.text() : "";
                        final String srcToMoreInfoTest = srcToMoreInfoElement != null ? "https://asbleasing.by" + srcToMoreInfoElement.attr("href") : "";

                        final View customView = LayoutInflater.from(context).inflate(R.layout.custom_news_item, container, false);

                        // Находим TextView в макете и устанавливаем текст
                        TextView titleTextView = customView.findViewById(R.id.titleTextView);
                        titleTextView.setText(title);

                        TextView templateTextView = customView.findViewById(R.id.templateTextView);
                        templateTextView.setText(templateText);

                        TextView dateTextView = customView.findViewById(R.id.dateTextView);
                        dateTextView.setText(dateText);

                        AppCompatButton button = customView.findViewById(R.id.button);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(context, NewDetailsAcitivity.class);
                                intent.putExtra("STRING_KEY", srcToMoreInfoTest);
                                context.startActivity(intent);

                            }
                        });



                        final ImageView imageView = customView.findViewById(R.id.imageView);
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Picasso.get()
                                        .load(imageUrl)
                                        .resize(900, 900) // Укажите желаемые ширину и высоту
                                        .centerInside()
                                        .into(imageView);
                            }
                        });
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(context, NewDetailsAcitivity.class);
                                intent.putExtra("STRING_KEY", srcToMoreInfoTest);
                                context.startActivity(intent);

                            }
                        });

                        // Добавляем кастомный элемент в контейнер
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                container.addView(customView);
                            }
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }



}