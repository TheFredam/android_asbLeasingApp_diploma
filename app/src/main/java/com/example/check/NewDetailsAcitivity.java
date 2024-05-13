package com.example.check;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class NewDetailsAcitivity extends AppCompatActivity {
    private LinearLayout container;
    private DrawerHandler drawerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_details);
        // Получаем данные из Intent
        String receivedString = getIntent().getStringExtra("STRING_KEY");

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ImageView burgerIcon = findViewById(R.id.burgerIcon);
        ImageView phoneIcon = findViewById(R.id.phoneIcon);
        LinearLayout navView = findViewById(R.id.nav_view);
        drawerManager = new DrawerHandler(this, drawerLayout, burgerIcon, phoneIcon, navView);


        container = findViewById(R.id.container);
        new NewDetailsAcitivity.ParseHtmlTask(NewDetailsAcitivity.this, container, receivedString).execute();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.green)); // Замените на ваш цвет
        }

    }



    public class ParseHtmlTask extends AsyncTask<Void, Void, Void> {
        private Context context;
        private LinearLayout container;
        private ProgressDialog progressDialog;
        private String valueURL;

        public ParseHtmlTask(Context context, LinearLayout container, String valueURL) {
            this.context = context;
            this.container = container;
            this.valueURL = valueURL;
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
                Document document = Jsoup.connect(valueURL).get();
                Log.d("Selected Value", valueURL);

                Elements partners = document.select(".container.news__container");

                // Перебираем каждый элемент "container news__container"
                for (final Element partner : partners) {
                    // Получаем содержимое news__title внутри контейнера
                    String newsTitle = partner.select(".news__title").text();

                    // Создаем копию элемента partner без таблицы
                    Element partnerWithoutTable = partner.clone();
                    partnerWithoutTable.select("table").remove();

                    // Получаем все содержимое контейнера и удаляем из него news__title
                    String newsContent = partnerWithoutTable.html();
                    newsContent = newsContent.replace(newsTitle, "");

                    // Удаляем все теги <img> из содержимого
                    newsContent = Jsoup.parse(newsContent).text();

                    // Извлекаем дату из template-head-info
                    Elements templateHeadInfoItems = partner.select(".template-head-info__item");
                    StringBuilder dateBuilder = new StringBuilder();
                    for (Element item : templateHeadInfoItems) {
                        dateBuilder.append(item.text()).append(" ");
                    }
                    String date = dateBuilder.toString().trim();
                    newsContent = newsContent.replace(date + " ", "");

                    final View customView = LayoutInflater.from(context).inflate(R.layout.custom_news_item_detail, container, false);

                    // Находим TextView в макете и устанавливаем текст
                    TextView titleTextView = customView.findViewById(R.id.titleTextView);
                    titleTextView.setText(newsTitle);

                    // Находим contentTextView в макете и устанавливаем текст без изображений
                    TextView contentTextView = customView.findViewById(R.id.contentTextView);
                    contentTextView.setText(newsContent);
                    contentTextView.setMovementMethod(LinkMovementMethod.getInstance());

                    // Находим dateTextView в макете и устанавливаем дату
                    TextView dateTextView = customView.findViewById(R.id.dateTextView);
                    dateTextView.setText(date);

                    // Добавляем кастомный элемент в контейнер
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            container.addView(customView);
                        }
                    });

                    // Обработка изображений
                    Elements images = partner.select("img");
                    for (Element image : images) {
                        String imageUrl = image.attr("src");
                        // Загружаем изображение из URL с помощью библиотеки Picasso в основном потоке
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ImageView imageView = customView.findViewById(R.id.imageView);
                                Picasso.get().load("https://asbleasing.by" + imageUrl).resize(900, 900).centerInside().into(imageView);
                            }
                        });
                    }

                    // Обработка таблицы
                    Elements tables = partner.select("table");
                    for (Element table : tables) {
                        // Создаем горизонтальный скроллвью для таблицы
                        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(context);
                        // Создаем таблицу внутри горизонтального скроллвью
                        TableLayout tableLayout = new TableLayout(context);
                        tableLayout.setStretchAllColumns(true);

                        Elements rows = table.select("tr");
                        for (Element row : rows) {
                            TableRow tableRow = new TableRow(context);

                            // Получаем все ячейки в текущей строке
                            Elements cells = row.select("td");
                            for (Element cell : cells) {
                                TextView textView = new TextView(context);
                                textView.setText(cell.text());
                                textView.setPadding(10, 5, 10, 5);
                                textView.setBackgroundResource(R.drawable.cell_border); // создайте нужный вам drawable для границы
                                tableRow.addView(textView);
                            }
                            tableLayout.addView(tableRow);
                        }
                        horizontalScrollView.addView(tableLayout);

                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                container.addView(horizontalScrollView);
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