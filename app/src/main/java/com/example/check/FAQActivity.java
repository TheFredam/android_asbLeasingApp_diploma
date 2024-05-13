package com.example.check;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FAQActivity extends AppCompatActivity {

    private DrawerHandler drawerManager;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_faqactivity);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ImageView burgerIcon = findViewById(R.id.burgerIcon);
        ImageView phoneIcon = findViewById(R.id.phoneIcon);
        LinearLayout navView = findViewById(R.id.nav_view);
        drawerManager = new DrawerHandler(this, drawerLayout, burgerIcon, phoneIcon, navView);

        container = findViewById(R.id.container);
        new FAQActivity.ParseHtmlTask(FAQActivity.this, container).execute();

        FloatingActionButton btnAskQuestion = findViewById(R.id.btnAskQuestion);
        btnAskQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ваш код обработки нажатия кнопки
                Intent intent = new Intent(FAQActivity.this, WriteQuestionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Добавляем флаг FLAG_ACTIVITY_NEW_TASK
                startActivity(intent);
            }
        });



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

        public ParseHtmlTask(Context context, LinearLayout container) {
            this.context = context;
            this.container = container;
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
                // Подключаемся к первой странице
                Document document1 = Jsoup.connect("https://asbleasing.by/chastnym-litsam/").get();
                Elements dropdowns1 = document1.select(".dropdown");
                Document document2 = Jsoup.connect("https://asbleasing.by/business/").get();
                Elements dropdowns2 = document2.select(".dropdown");
                Elements allDropdowns = new Elements();
                allDropdowns.addAll(dropdowns1);
                allDropdowns.addAll(dropdowns2);
                Set<String> uniqueButtonTexts = new HashSet<>();

                for (Element dropdown : allDropdowns) {
                    final String buttonText = dropdown.select(".dropdown-btn").text().trim();
                    if (!uniqueButtonTexts.contains(buttonText)) {
                        uniqueButtonTexts.add(buttonText);
                        final String templateText = dropdown.select(".template__text").html();

                        ((FAQActivity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                View itemView = LayoutInflater.from(context).inflate(R.layout.item_dropdown, container, false);

                                TextView textQuestion = itemView.findViewById(R.id.textQuestion);
                                TextView textAnswer = itemView.findViewById(R.id.textAnswer);
                                ImageButton buttonExpand = itemView.findViewById(R.id.imageButtonExpand);
                                textQuestion.setText(buttonText); // Устанавливаем текст вопроса
                                buttonExpand.setImageResource(R.drawable.ic_plus); // Устанавливаем иконку кнопки
                                buttonExpand.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // По нажатию меняем видимость текста ответа и иконки кнопки
                                        if (textAnswer.getVisibility() == View.VISIBLE) {
                                            textAnswer.setVisibility(View.GONE);
                                            buttonExpand.setImageResource(R.drawable.ic_plus);
                                        } else {
                                            textAnswer.setVisibility(View.VISIBLE);
                                            buttonExpand.setImageResource(R.drawable.ic_minus);
                                        }
                                    }
                                });
                                textAnswer.setText(Html.fromHtml(templateText)); // Устанавливаем текст ответа

                                // Добавляем созданный элемент в контейнер
                                container.addView(itemView);
                            }
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }



        private void parseDropdowns(String url) {
            try {
                Document document = Jsoup.connect(url).get();
                Elements dropdowns = document.select(".dropdown");

                for (Element dropdown : dropdowns) {
                    final String buttonText;
                    Element buttonElement = dropdown.selectFirst(".dropdown-btn");
                    if (buttonElement != null) {
                        buttonText = buttonElement.text().trim();
                    } else {
                        buttonText = "";
                    }

                    final String templateText = dropdown.select(".template__text").html(); // Извлекаем HTML-текст из блока template__text

                    ((FAQActivity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Заполняем макет item_dropdown.xml данными
                            View itemView = LayoutInflater.from(context).inflate(R.layout.item_dropdown, container, false);

                            TextView textQuestion = itemView.findViewById(R.id.textQuestion);
                            TextView textAnswer = itemView.findViewById(R.id.textAnswer);
                            ImageButton buttonExpand = itemView.findViewById(R.id.imageButtonExpand);
                            textQuestion.setText(buttonText); // 'questionText' - переменная с текстом вопроса
                            buttonExpand.setImageResource(R.drawable.ic_plus);
                            buttonExpand.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // По нажатию меняем видимость текста ответа
                                    if (textAnswer.getVisibility() == View.VISIBLE) {
                                        textAnswer.setVisibility(View.GONE);
                                        buttonExpand.setImageResource(R.drawable.ic_plus);
                                    } else {
                                        textAnswer.setVisibility(View.VISIBLE);
                                        buttonExpand.setImageResource(R.drawable.ic_minus);
                                    }
                                }
                            });
                            textAnswer.setText(Html.fromHtml(templateText)); //


                            // Добавляем созданный элемент в контейнер
                            container.addView(itemView);
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        private void removeDuplicateQuestions() {
            Set<String> buttonTextSet = new HashSet<>();

            for (int i = container.getChildCount() - 1; i >= 0; i--) {
                View view = container.getChildAt(i);
                TextView textQuestion = view.findViewById(R.id.textQuestion);
                String buttonText = textQuestion.getText().toString();

                // Если buttonText уже был добавлен, то удаляем элемент
                if (buttonTextSet.contains(buttonText)) {
                    container.removeViewAt(i);
                } else {
                    buttonTextSet.add(buttonText);
                }
            }
        }










    }

}