package com.example.check;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
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


public class PartnersActivity extends AppCompatActivity {

    private LinearLayout container;
    private DrawerHandler drawerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_partners);

        container = findViewById(R.id.container);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ImageView burgerIcon = findViewById(R.id.burgerIcon);
        ImageView phoneIcon = findViewById(R.id.phoneIcon);
        LinearLayout navView = findViewById(R.id.nav_view);
        drawerManager = new DrawerHandler(this, drawerLayout, burgerIcon, phoneIcon, navView);


        // Выполняем парсинг в фоновом потоке
        new ParseHtmlTask(PartnersActivity.this, container).execute();


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
                Document document = Jsoup.connect("https://asbleasing.by/company/our_partners/").get();

                Elements partners = document.select(".about-partners-item");

                // Перебираем каждый элемент "about-partners-item"
                for (final Element partner : partners) {
                    Element titleElement = partner.selectFirst(".about-partners-item__title");
                    Element imageElement = partner.selectFirst(".about-partners-item__img img");
                    Element templateTextElement = partner.selectFirst(".template__text");
                    Element subtitleElement = partner.selectFirst(".about-partners-item__subtitle");
                    Element secondSubtitleElement = null;
                    Element pElement = partner.selectFirst("p");
                    Element aElement = partner.selectFirst("a");
                    Elements subtitleElements = partner.select(".about-partners-item__subtitle");
                    if (subtitleElements.size() > 1) {
                        secondSubtitleElement = subtitleElements.get(1);
                    }


                    final String title = titleElement != null ? titleElement.text() : "";
                    final String imageUrl = imageElement != null ? "https://asbleasing.by" + imageElement.attr("src") : "";
                    final String templateText = templateTextElement != null ? templateTextElement.text() : "";
                    final String subtitle = subtitleElement != null ? subtitleElement.text() : "";
                    final String subtitle2 = secondSubtitleElement != null ? secondSubtitleElement.text() : "";
                    final String pText = pElement != null ? pElement.text() : "";
                    final String websiteUrl = aElement != null ? aElement.attr("href") : "";

                    if (!title.equals("ГО «Белорусская железная дорога»") &&
                            !title.equals("ГП «Минсктранс»") &&
                            !title.equals("Белоруснефть Производственное объединение")) {

                        final View customView = LayoutInflater.from(context).inflate(R.layout.custom_partner_item, container, false);

                        TextView titleTextView = customView.findViewById(R.id.titleTextView);
                        titleTextView.setText(title);


                        TextView templateTextView = customView.findViewById(R.id.templateTextView);
                        templateTextView.setText(templateText);

                        TextView subtitleTextView = customView.findViewById(R.id.subtitleTextView);
                        subtitleTextView.setText(subtitle);

                        TextView subtitleTextView2 = customView.findViewById(R.id.subtitleTextView2);
                        subtitleTextView2.setText(subtitle2);


                        TextView pTextView = customView.findViewById(R.id.pTextView);
                        pTextView.setText(pText);

                        TextView websiteTextView = customView.findViewById(R.id.websiteTextView);
                        websiteTextView.setText(websiteUrl);
                        UnderlineSpan underlineSpan = new UnderlineSpan();
                        SpannableString content = new SpannableString(websiteTextView.getText());
                        content.setSpan(underlineSpan, 0, content.length(), 0);
                        websiteTextView.setText(content);
                        websiteTextView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Открываем ссылку в браузере
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl));
                                startActivity(intent);
                            }
                        });



                        final ImageView imageView = customView.findViewById(R.id.imageView);
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Picasso.get()
                                        .load(imageUrl)
                                        .resize(500, 500) // Укажите здесь желаемые ширину и высоту
                                        .centerInside() // Это поможет сохранить пропорции изображения
                                        .into(imageView);
                            }
                        });
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Открываем ссылку в браузере
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl));
                                startActivity(intent);
                            }
                        });


                        // Добавляем кастомный элемент в контейнер
                        ((PartnersActivity) context).runOnUiThread(new Runnable() {
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

