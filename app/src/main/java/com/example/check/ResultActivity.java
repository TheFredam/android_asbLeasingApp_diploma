package com.example.check;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.slider.Slider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ResultActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private DrawerHandler drawerManager;
    private Slider avansSlider;
    private EditText avansEditText;
    private EditText avansProcentEditText;
    private Slider srokSlider;
    private EditText srokEditText;
    private AppCompatButton graficButton;
    private TextView priceTextView;
    private TextView modelTextView;
    private TextView marcaTextView;
    private TextView yearOpisTextView;
    private TextView transmissionOpisTextView;
    private TextView engineCopacityOpisTextView;
    private TextView typeFuelOpisTextView;
    private TextView mileageOpisTextView;
    private TextView colorOpisTextView;

    String resultCode;
    Integer price;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ImageView burgerIcon = findViewById(R.id.burgerIcon);
        ImageView phoneIcon = findViewById(R.id.phoneIcon);
        LinearLayout navView = findViewById(R.id.nav_view);
        drawerManager = new DrawerHandler(this, drawerLayout, burgerIcon, phoneIcon, navView);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.green)); // Замените на ваш цвет
        }

        avansEditText = findViewById(R.id.avansEditText);
        graficButton = findViewById(R.id.graficButton);
        marcaTextView = findViewById(R.id.marcaTextView);
        priceTextView = findViewById(R.id.priceTextView);
        modelTextView = findViewById(R.id.modelTextView);
        yearOpisTextView = findViewById(R.id.yearOpisTextView);
        transmissionOpisTextView = findViewById(R.id.transmissionOpisTextView);
        engineCopacityOpisTextView = findViewById(R.id.engineCopacityOpisTextView);
        typeFuelOpisTextView = findViewById(R.id.typeFuelOpisTextView);
        mileageOpisTextView = findViewById(R.id.mileageOpisTextView);
        colorOpisTextView = findViewById(R.id.colorOpisTextView);
        setupSliderAndEditText();
        setupSliderAndEditText2();
        makeGrafic();

        Intent intent = getIntent();
        if (intent != null) {
            resultCode = intent.getStringExtra("CODE");
            Toast.makeText(getApplicationContext(), resultCode, Toast.LENGTH_SHORT).show();
        }





        databaseReference = FirebaseDatabase.getInstance().getReference("codes").child("CR").child(resultCode);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ViewPager viewPager = findViewById(R.id.viewPagerImages);
                    List<String> allImageUrls = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.child("FRONT").getChildren()) {
                        String imageUrlFront = snapshot.getValue(String.class);
                        allImageUrls.add(imageUrlFront);
                    }
                    for (DataSnapshot snapshot : dataSnapshot.child("BACK").getChildren()) {
                        String imageUrlBack = snapshot.getValue(String.class);
                        allImageUrls.add(imageUrlBack);
                    }

                    ImagePagerAdapter adapter = new ImagePagerAdapter(getApplicationContext(), allImageUrls);
                    viewPager.setAdapter(adapter);




                    // Получаем значения Marca, Price и Model из dataSnapshot
                    String marca = dataSnapshot.child("INFO").child("Marca").getValue(String.class);
                    Integer price = dataSnapshot.child("INFO").child("Price").getValue(Integer.class);
                    String model = dataSnapshot.child("INFO").child("Model").getValue(String.class);

                    Integer year = dataSnapshot.child("INFO").child("Year").getValue(Integer.class);
                    String transmission = dataSnapshot.child("INFO").child("Transmission").getValue(String.class);
                    Float engineCapacity = dataSnapshot.child("INFO").child("EngineCapacity").getValue(Float.class);
                    String typeFuel = dataSnapshot.child("INFO").child("TypeFuel").getValue(String.class);
                    Long mileage = dataSnapshot.child("INFO").child("Mileage").getValue(Long.class);
                    String color = dataSnapshot.child("INFO").child("Color").getValue(String.class);

                    if (price != null && marca != null && model != null && year != null && transmission != null && engineCapacity != null && typeFuel != null && mileage != null && color != null) {
                        // Форматируем цену для вывода с разделителем тысяч
                        String formattedPrice = NumberFormat.getNumberInstance(Locale.getDefault()).format(price);
                        // Устанавливаем значения в соответствующие поля
                        marcaTextView.setText(marca);
                        priceTextView.setText(String.valueOf(formattedPrice + " BYN"));
                        modelTextView.setText(model);

                        yearOpisTextView.setText(String.valueOf(year)+" г.,");
                        transmissionOpisTextView.setText(transmission+",");
                        engineCopacityOpisTextView.setText(String.valueOf(engineCapacity)+" л.,");
                        typeFuelOpisTextView.setText(typeFuel+",");
                        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
                        String formattedMileage = numberFormat.format(mileage);
                        mileageOpisTextView.setText(formattedMileage+" км,");
                        colorOpisTextView.setText(color);
                    } else {
                        Log.d("Data", "One of the values is null");
                    }

                } else {
                    Log.d("Data", "DataSnapshot does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок при чтении из базы данных
            }
        });




        makePriceForLeasing();
    }

    private void makePriceForLeasing(){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Получаем значение Price из dataSnapshot
                    price = dataSnapshot.child("INFO").child("Price").getValue(Integer.class);
                    if (price != null) {
                        float valueFromSlider = avansSlider.getValue(); // Получаем значение из Slider
                        float avansPercent = (valueFromSlider / 100) * price; // Вычисляем процент от цены
                        String formattedValue = String.format("%.2f", avansPercent);
                        avansEditText.setText(formattedValue); // Устанавливаем результат в EditText
                    } else {
                        Log.d("Price", "Price is null");
                    }
                } else {
                    Log.d("Price", "DataSnapshot does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void makeGrafic(){
        graficButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Получаем значение из avansEditText

                String avansValueString = avansEditText.getText().toString();
                if (!avansValueString.isEmpty()) {
                    // Получаем значение из srokEditText
                    String srokValueString = srokEditText.getText().toString();
                    if (!srokValueString.isEmpty()) {
                        int srokValue = Integer.parseInt(srokValueString);

                        // Инфлейтим ваш готовый макет
                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View popupView = inflater.inflate(R.layout.graficoplat_layout, null);

                        // Находим TableLayout, в который будем добавлять TableRow
                        TableLayout tableLayout = popupView.findViewById(R.id.table);

                        // Создаем и добавляем TableRow для "Аванс"
                        TableRow avansRow = new TableRow(getApplicationContext());
                        avansRow.setPadding(5, 5, 5, 5); // оформление из макета

                        // Создаем TextView для слова "Аванс"
                        TextView avansLabelTextView = new TextView(getApplicationContext());
                        avansLabelTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                        avansLabelTextView.setGravity(Gravity.CENTER);
                        avansLabelTextView.setText("Аванс");
                        avansLabelTextView.setTextColor(Color.BLACK);
                        avansLabelTextView.setPadding(5, 5, 5, 5);
                        avansRow.addView(avansLabelTextView);

                        // Создаем TextView для значения из avansEditText
                        TextView avansValueTextView = new TextView(getApplicationContext());
                        avansValueTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                        avansValueTextView.setGravity(Gravity.CENTER);
                        avansValueTextView.setText(avansValueString);
                        avansValueTextView.setTextColor(Color.BLACK);
                        avansValueTextView.setPadding(5, 5, 5, 5);
                        avansRow.addView(avansValueTextView);

                        // Добавляем строку "Аванс" в таблицу
                        tableLayout.addView(avansRow);

                        // Добавляем разделитель после строки "Аванс"
                        View dividerAvans = new View(getApplicationContext());
                        dividerAvans.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                        dividerAvans.setBackgroundColor(Color.parseColor("#636363"));
                        tableLayout.addView(dividerAvans);

                        // Создаем и добавляем TableRow с настройками, аналогичными exampleRow
// Получаем значение из avansEditText

                        String avansValueString1 = avansEditText.getText().toString();
                        avansValueString1 = avansValueString1.replace(",", ".");
                        float avansValue = Float.parseFloat(avansValueString1);
                        float totalAmount = price - avansValue;

                        int years = (int) (srokValue / 12);
                        float precentGodovoi = (float) (totalAmount * (0.07*years));
                        totalAmount = totalAmount+precentGodovoi;

                        Random random = new Random();
                        int minPercentage = 95; // 97% от общей суммы
                        int maxPercentage = 105; // 103% от общей суммы
                        int numberOfPayments = srokValue;
                        int totalPercentage = 100;
                        float[] payments = new float[numberOfPayments];

                        for (int i = 0; i < numberOfPayments; i++) {
                            // Генерируем процент от общей суммы в пределах minPercentage и maxPercentage
                            int randomPercentage = random.nextInt(maxPercentage - minPercentage + 1) + minPercentage;

                            // Вычисляем платеж на основе процента от общей суммы
                            float payment = (totalAmount * randomPercentage) / (totalPercentage * numberOfPayments);

                            // Добавляем платеж в массив
                            payments[i] = payment;
                        }

// Выводим платежи на экран
                        for (int i = 0; i < numberOfPayments; i++) {
                            TableRow tableRow = new TableRow(getApplicationContext());
                            tableRow.setPadding(5, 5, 5, 5); // оформление из макета

                            TextView monthTextView = new TextView(getApplicationContext());
                            monthTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                            monthTextView.setGravity(Gravity.CENTER);
                            monthTextView.setText((i + 1) + " месяц");
                            monthTextView.setTextColor(Color.BLACK);
                            monthTextView.setPadding(5, 5, 5, 5);
                            tableRow.addView(monthTextView);

                            // Создаем TextView для платежа
                            TextView paymentTextView = new TextView(getApplicationContext());
                            paymentTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                            paymentTextView.setGravity(Gravity.CENTER);
                            paymentTextView.setText(String.format("%.2f", payments[i]));
                            paymentTextView.setTextColor(Color.BLACK);
                            paymentTextView.setPadding(5, 5, 5, 5);
                            tableRow.addView(paymentTextView);

                            tableLayout.addView(tableRow);

                            // Добавляем разделитель после каждой строки, кроме последней
                            if (i < numberOfPayments - 1) {
                                View divider = new View(getApplicationContext());
                                divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                                divider.setBackgroundColor(Color.parseColor("#636363"));
                                tableLayout.addView(divider);
                            }
                        }




                        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                        boolean focusable = true; // позволяет закрыть PopupWindow при нажатии вне его области
                        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                        int marginInDp = 15; // величина отступа в dp
                        int marginInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginInDp, getResources().getDisplayMetrics());
                        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
                        popupWindow.setClippingEnabled(false);
                        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) popupView.getLayoutParams();
                        layoutParams.setMargins(marginInPx, marginInPx, marginInPx, marginInPx);
                        popupView.setLayoutParams(layoutParams);

                        // Найдите кнопку closeButton в вашем макете
                        ImageView closeButton = popupView.findViewById(R.id.closeButton);

// Установите обработчик события для кнопки closeButton
                        closeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Закройте PopupWindow при нажатии на кнопку closeButton
                                popupWindow.dismiss();
                            }
                        });

                    }
                }
            }
        });
    }
    private void setupSliderAndEditText2(){
        avansSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                if (price != null) {
                    float avansPercent = (value / 100) * price; // Вычисляем процент от цены
                    String formattedValue = String.format("%.2f", avansPercent);
                    avansEditText.setText(formattedValue); // Устанавливаем результат в EditText
                } else {
                    Log.d("Price", "Price is null");
                }
            }
        });
        avansEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // Выполняем действия при потере фокуса (завершении редактирования)
                    String text = avansEditText.getText().toString();
                    if (!TextUtils.isEmpty(text)) {
                        String formattedText = text.replace(',', '.'); // Заменяем запятую на точку
                        float enteredValue = Float.parseFloat(formattedText);
                        float maxPrice = price * 0.4f; // Максимальное значение (40% от Price)
                        float minPrice = price * 0.2f; // Минимальное значение (20% от Price)

                        // Проверяем, входит ли введенное значение в заданный диапазон
                        if (enteredValue < minPrice) {
                            avansEditText.setText(String.valueOf(minPrice));
                            avansProcentEditText.setText("20 %");
                            avansSlider.setValue(20);
                        } else if (enteredValue > maxPrice) {
                            avansEditText.setText(String.valueOf(maxPrice));
                            avansProcentEditText.setText("40 %");
                            avansSlider.setValue(40);
                        }
                        else {
                            float percent = (enteredValue / price) * 100;
                            String percentString = String.format("%.2f", percent); // Форматируем результат до двух знаков после запятой
                            avansProcentEditText.setText(percentString + " %");
                            float percentFloat = Float.parseFloat(percentString.replace(',', '.')); // Заменяем запятую на точку
                            avansSlider.setValue(percentFloat);
                        }
                    }
                }
            }
        });

    }
    private void setupSliderAndEditText() {
        avansSlider = findViewById(R.id.avansSlider);
        avansProcentEditText = findViewById(R.id.avansProcentEditText);

        avansSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                String formattedValue = String.format("%.2f %%", value);
                avansProcentEditText.setText(formattedValue);
            }
        });

        srokSlider = findViewById(R.id.srokSlider);
        srokEditText = findViewById(R.id.srokEditText);

        srokSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                srokEditText.setText(String.valueOf((int) value));
            }
        });

        srokEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!srokEditText.getText().toString().isEmpty()) {
                        int value = Integer.parseInt(srokEditText.getText().toString());
                        if (value > 60) {
                            srokEditText.setText("60");
                            value = 60;
                        } else if (value < 12) {
                            srokEditText.setText("12");
                            value = 12;
                        }
                        if (srokSlider.getValue() != value) {
                            srokSlider.setValue(value);
                        }
                    }
                }
            }
        });

    }


}


class ImagePagerAdapter extends PagerAdapter {
    private Context context;
    private List<String> imageUrls;

    public ImagePagerAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        ImageView imageView = new ImageView(context);
        // Set scale type
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        // Load image using Picasso or Glide
        try {
            Picasso.get().load(imageUrls.get(position)).into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set click listener to open image in popup
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageInPopup(imageUrls.get(position), v);
            }
        });

        container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    private void openImageInPopup(String imageUrl, View anchorView) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_image, null);

        PhotoView imageView = popupView.findViewById(R.id.popup_image_view);
        Picasso.get().load(imageUrl).into(imageView);



        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.showAsDropDown(anchorView);

        ImageView closeImg;
        closeImg = popupView.findViewById(R.id.closeButton);
        closeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }
}

