package com.example.check;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceNew;

    private DrawerHandler drawerManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Загрузка...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        databaseReference = FirebaseDatabase.getInstance().getReference("codes");

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ImageView burgerIcon = findViewById(R.id.burgerIcon);
        ImageView phoneIcon = findViewById(R.id.phoneIcon);
        LinearLayout navView = findViewById(R.id.nav_view);
        drawerManager = new DrawerHandler(this, drawerLayout, burgerIcon, phoneIcon, navView);


        FloatingActionButton filterIcon = findViewById(R.id.filterIcon);
        filterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View menuLayout = LayoutInflater.from(MainActivity.this).inflate(R.layout.menu_layout, null);

                // Вычисление размеров экрана
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenWidth = displayMetrics.widthPixels;
                int screenHeight = displayMetrics.heightPixels;

                // Создание PopupWindow
                PopupWindow popupWindow = new PopupWindow(menuLayout, screenWidth, (int) (screenHeight * 0.8), true);
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                popupWindow.setFocusable(true);
                popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.TOP, 0, 0);


                SharedPreferences sharedPreferences = getSharedPreferences("FilterPrefs", Context.MODE_PRIVATE);
                int startYearFilter = sharedPreferences.getInt("startYear", 0);
                int endYearFilter = sharedPreferences.getInt("endYear", 0);
                int minMileageFilter = sharedPreferences.getInt("minMileage", 0);
                int maxMileageFilter = sharedPreferences.getInt("maxMileage", 0);
                int minPriceFilter = sharedPreferences.getInt("minPrice", 0);
                int maxPriceFilter = sharedPreferences.getInt("maxPrice", 0);

                // Устанавливаем сохраненные значения фильтров для элементов интерфейса
                EditText yearFromEditText = menuLayout.findViewById(R.id.yearFromEditText);
                EditText yearToEditText = menuLayout.findViewById(R.id.yearToEditText);
                EditText mileageFromEditText = menuLayout.findViewById(R.id.mileageFromEditText);
                EditText mileageToEditText = menuLayout.findViewById(R.id.mileageToEditText);
                EditText priceFromEditText = menuLayout.findViewById(R.id.priceFromEditText);
                EditText priceToEditText = menuLayout.findViewById(R.id.priceToEditText);

                yearFromEditText.setText(startYearFilter != 0 ? String.valueOf(startYearFilter) : "");
                yearToEditText.setText(endYearFilter != 0 ? String.valueOf(endYearFilter) : "");
                mileageFromEditText.setText(minMileageFilter != 0 ? String.valueOf(minMileageFilter) : "");
                mileageToEditText.setText(maxMileageFilter != 0 ? String.valueOf(maxMileageFilter) : "");
                priceFromEditText.setText(minPriceFilter != 0 ? String.valueOf(minPriceFilter) : "");
                priceToEditText.setText(maxPriceFilter != 0 ? String.valueOf(maxPriceFilter) : "");

                // Инициализация Spinner для марки
                Spinner marcaSpinner = menuLayout.findViewById(R.id.marcaSpinner);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MainActivity.this,
                        R.array.marca_array, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                marcaSpinner.setAdapter(adapter);

                Spinner transmissionSpinner = menuLayout.findViewById(R.id.transmissionSpinner);
                ArrayAdapter<CharSequence> transmissionAdapter = ArrayAdapter.createFromResource(MainActivity.this,
                        R.array.transmission_array, android.R.layout.simple_spinner_item);
                transmissionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                transmissionSpinner.setAdapter(transmissionAdapter);


                // Загружаем сохраненное значение марки из SharedPreferences
                String selectedMarca = sharedPreferences.getString("selectedMarca", "");
                if (!selectedMarca.isEmpty()) {
                    int position = adapter.getPosition(selectedMarca);
                    if (position != -1) {
                        marcaSpinner.setSelection(position);
                    }
                }
                String selectedTransmission = sharedPreferences.getString("selectedTransmission", "");
                if (!selectedTransmission.isEmpty()) {
                    int position = transmissionAdapter.getPosition(selectedTransmission);
                    if (position != -1) {
                        transmissionSpinner.setSelection(position);
                    }
                }

                Button filterButton = menuLayout.findViewById(R.id.applyFilterButton);
                filterButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Получаем значения из полей ввода
                        int startYear = Integer.parseInt(yearFromEditText.getText().toString().isEmpty() ? "0" : yearFromEditText.getText().toString());
                        int endYear = Integer.parseInt(yearToEditText.getText().toString().isEmpty() ? "0" : yearToEditText.getText().toString());
                        int minMileage = Integer.parseInt(mileageFromEditText.getText().toString().isEmpty() ? "0" : mileageFromEditText.getText().toString());
                        int maxMileage = Integer.parseInt(mileageToEditText.getText().toString().isEmpty() ? "0" : mileageToEditText.getText().toString());
                        int minPrice = Integer.parseInt(priceFromEditText.getText().toString().isEmpty() ? "0" : priceFromEditText.getText().toString());
                        int maxPrice = Integer.parseInt(priceToEditText.getText().toString().isEmpty() ? "0" : priceToEditText.getText().toString());

                        // Получаем выбранную марку из Spinner
                        String selectedMarca = (String) marcaSpinner.getSelectedItem();
                        String selectedTransmission = (String) transmissionSpinner.getSelectedItem();

                        // Сохраняем значения фильтров в SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("startYear", startYear);
                        editor.putInt("endYear", endYear);
                        editor.putInt("minMileage", minMileage);
                        editor.putInt("maxMileage", maxMileage);
                        editor.putInt("minPrice", minPrice);
                        editor.putInt("maxPrice", maxPrice);
                        editor.putString("selectedMarca", selectedMarca); // Сохраняем выбранную марку
                        editor.putString("selectedTransmission", selectedTransmission); // Сохраняем выбранную марку
                        editor.apply();

                        // Вызываем метод фильтрации с полученными значениями
                        filterData(startYear, endYear, minMileage, maxMileage, selectedMarca, selectedTransmission, minPrice, maxPrice);

                    }
                });

                Button resetFilterButton = menuLayout.findViewById(R.id.resetFilterButton);
                resetFilterButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Удаляем все значения фильтра из SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("startYear");
                        editor.remove("endYear");
                        editor.remove("minMileage");
                        editor.remove("maxMileage");
                        editor.remove("minPrice");
                        editor.remove("maxPrice");
                        editor.remove("selectedMarca"); // Удаляем сохраненную марку
                        editor.remove("selectedTransmission"); // Удаляем сохраненную марку
                        editor.apply();

                        // Обновляем интерфейс
                        yearFromEditText.setText("");
                        yearToEditText.setText("");
                        mileageFromEditText.setText("");
                        mileageToEditText.setText("");
                        priceFromEditText.setText("");
                        priceToEditText.setText("");
                        marcaSpinner.setSelection(0);
                        transmissionSpinner.setSelection(0);

                    }
                });
            }
        });


        databaseReferenceNew = FirebaseDatabase.getInstance().getReference("codes").child("CR");
        databaseReferenceNew.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                LinearLayout parentLayout = findViewById(R.id.parentLayout);

                parentLayout.removeAllViews();

                for (DataSnapshot codeSnapshot : dataSnapshot.getChildren()) {
                    String codeKey = codeSnapshot.getKey();
                    String layoutId = codeKey;
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, // Ширина равна родительскому макету
                            LinearLayout.LayoutParams.WRAP_CONTENT // Высота автоматически подстраивается под содержимое
                    );
                    layoutParams.setMargins(15, 15, 15, 15); // Устанавливаем отступы по бокам (левый, верхний, правый, нижний)
                    View newLayout = getLayoutInflater().inflate(R.layout.block_layout, null);
                    newLayout.setLayoutParams(layoutParams);


                    String marcaUrl = codeSnapshot.child("INFO").child("Marca").getValue(String.class);
                    TextView marcaTextView = newLayout.findViewById(R.id.marcaTextView);
                    marcaTextView.setText(marcaUrl);

                    String modelUrl = codeSnapshot.child("INFO").child("Model").getValue(String.class);
                    TextView modelTextView = newLayout.findViewById(R.id.modelTextView);
                    modelTextView.setText(modelUrl);

                    String transmissionUrl = codeSnapshot.child("INFO").child("Transmission").getValue(String.class);
                    TextView transmissionTextView = newLayout.findViewById(R.id.transmissionTextView);
                    transmissionTextView.setText(transmissionUrl + ", ");

                    Long mileageValue = codeSnapshot.child("INFO").child("Mileage").getValue(Long.class);
                    String formattedMileage = String.format("%,d", mileageValue); // Форматирование числа с разделителями для тысяч
                    TextView mileageTextView = newLayout.findViewById(R.id.mileageTextView);
                    mileageTextView.setText(formattedMileage + " км");


                    String yearUrl = String.valueOf(codeSnapshot.child("INFO").child("Year").getValue(Integer.class));
                    TextView yearTextView = newLayout.findViewById(R.id.yearTextView);
                    yearTextView.setText(yearUrl + " г., ");

                    String priceUrl = String.valueOf(codeSnapshot.child("INFO").child("Price").getValue(Integer.class));
                    String formattedPrice = String.format("%,d BYN", Integer.parseInt(priceUrl));
                    TextView priceTextView = newLayout.findViewById(R.id.priceTextView);
                    priceTextView.setText(formattedPrice);






                    String imageUrl = codeSnapshot.child("INFO").child("MainPhoto").getValue(String.class);
                    ImageView imageView = newLayout.findViewById(R.id.imageView);
                    Picasso.get().load(imageUrl).into(imageView);
                    Picasso.get().load(imageUrl).into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            // Скрыть диалог загрузки после успешной загрузки изображения
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onError(Exception e) {
                            // Скрыть диалог загрузки в случае ошибки загрузки изображения
                            progressDialog.dismiss();
                        }
                    });


                    newLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String userCode = layoutId;

                            if (!TextUtils.isEmpty(userCode)) {
                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {
                                            };
                                            Map<String, Object> allData = dataSnapshot.getValue(genericTypeIndicator);

                                            boolean codeExists = checkCodeInData(allData, userCode);

                                            if (codeExists) {
                                                openResultActivity(userCode, "Дополнительные данные, которые вы хотите передать");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }

                                });
                            }
                        }
                    });


                    parentLayout.addView(newLayout);
                }

                progressDialog.dismiss();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок при чтении данных
                Toast.makeText(MainActivity.this, "Failed to read value. Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }


    private boolean checkCodeInData(Map<String, Object> data, String userCode, String currentPath) {
        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String currentKey = entry.getKey();
                String currentFullPath = currentPath + "/" + currentKey;

                if (currentKey.equals(userCode)) {
                    Log.d("FirebaseCheck", "Code exists at path: " + currentFullPath);
                    return true;
                }

                if (entry.getValue() instanceof Map) {
                    Map<String, Object> nestedData = (Map<String, Object>) entry.getValue();
                    if (checkCodeInData(nestedData, userCode, currentFullPath)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean checkCodeInData(Map<String, Object> allData, String userCode) {
        return checkCodeInData(allData, userCode, "/codes");
    }

    private void openResultActivity(String resultCode, String additionalData) {
        // Извлекаем код из resultCode
        String codeOnly = resultCode.replaceAll("[^a-zA-Z0-9]", "");

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("CODE", codeOnly);
        intent.putExtra("ADDITIONAL_DATA", additionalData);
        startActivity(intent);
    }


    private void filterData(int startYear, int endYear, int minMileage, int maxMileage, String selectedMarca, String selectedTransmission, int startPrice, int endPrice) {
        databaseReferenceNew = FirebaseDatabase.getInstance().getReference("codes").child("CR");
        databaseReferenceNew.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LinearLayout parentLayout = findViewById(R.id.parentLayout);
                parentLayout.removeAllViews();

                for (DataSnapshot codeSnapshot : dataSnapshot.getChildren()) {
                    String codeKey = codeSnapshot.getKey();
                    String layoutId = codeKey;
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, // Ширина равна родительскому макету
                            LinearLayout.LayoutParams.WRAP_CONTENT // Высота автоматически подстраивается под содержимое
                    );
                    layoutParams.setMargins(15, 15, 15, 15); // Устанавливаем отступы по бокам (левый, верхний, правый, нижний)
                    View newLayout = getLayoutInflater().inflate(R.layout.block_layout, null);
                    newLayout.setLayoutParams(layoutParams);

                    DataSnapshot infoSnapshot = codeSnapshot.child("INFO");

                    // Проверяем наличие данных в узле INFO
                    if (infoSnapshot.exists()) {
                        String marcaUrl = infoSnapshot.child("Marca").getValue(String.class);
                        String modelUrl = infoSnapshot.child("Model").getValue(String.class);
                        String transmissionUrl = infoSnapshot.child("Transmission").getValue(String.class);
                        Long mileageValue = infoSnapshot.child("Mileage").getValue(Long.class);
                        Integer yearValue = infoSnapshot.child("Year").getValue(Integer.class);
                        Integer priceValue = infoSnapshot.child("Price").getValue(Integer.class);

                        // Фильтрация по марке
                        boolean isMarcaSelected = selectedMarca == null || selectedMarca.isEmpty() || selectedMarca.equals(marcaUrl);

                        // Фильтрация по трансмиссии
                        boolean isTransmissionSelected = selectedTransmission == null || selectedTransmission.isEmpty() || selectedTransmission.equals(transmissionUrl);

                        // Фильтрация по году производства
                        boolean isYearInRange = (startYear == 0 || yearValue >= startYear) && (endYear == 0 || yearValue <= endYear);

                        boolean isPriceInRange = (startPrice == 0 || priceValue >= startPrice) && (endPrice == 0 || priceValue <= endPrice);

                        // Фильтрация по пробегу
                        boolean isMileageInRange = (minMileage == 0 || mileageValue >= minMileage) && (maxMileage == 0 || mileageValue <= maxMileage);

                        // Если данные удовлетворяют всем условиям фильтрации, добавляем их в макет
                        if (isMarcaSelected && isTransmissionSelected && isYearInRange && isMileageInRange && isPriceInRange) {
                            TextView marcaTextView = newLayout.findViewById(R.id.marcaTextView);
                            marcaTextView.setText(marcaUrl);

                            TextView modelTextView = newLayout.findViewById(R.id.modelTextView);
                            modelTextView.setText(modelUrl);

                            TextView transmissonTextView = newLayout.findViewById(R.id.transmissionTextView);
                            transmissonTextView.setText(transmissionUrl + ", ");
                            // Вывод пробега
                            String formattedMileage = String.format("%,d", mileageValue); // Форматирование числа с разделителями для тысяч
                            TextView mileageTextView = newLayout.findViewById(R.id.mileageTextView);
                            mileageTextView.setText(formattedMileage + " км");

                            String formattedPrice = String.format("%,d", priceValue); // Форматирование числа с разделителями для тысяч
                            TextView priceTextView = newLayout.findViewById(R.id.priceTextView);
                            priceTextView.setText(formattedPrice + " BYN");

                            // Вывод года
                            TextView yearTextView = newLayout.findViewById(R.id.yearTextView);
                            yearTextView.setText(yearValue + " г., ");


                            String imageUrl = codeSnapshot.child("INFO").child("MainPhoto").getValue(String.class);
                            ImageView imageView = newLayout.findViewById(R.id.imageView);
                            Picasso.get().load(imageUrl).into(imageView);
                            newLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String userCode = layoutId;

                                    if (!TextUtils.isEmpty(userCode)) {
                                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {
                                                    };
                                                    Map<String, Object> allData = dataSnapshot.getValue(genericTypeIndicator);

                                                    boolean codeExists = checkCodeInData(allData, userCode);

                                                    if (codeExists) {
                                                        openResultActivity(userCode, "Дополнительные данные, которые вы хотите передать");
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }

                                        });
                                    }
                                }
                            });


                            parentLayout.addView(newLayout);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок при чтении данных
                Toast.makeText(MainActivity.this, "Failed to read value. Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}

