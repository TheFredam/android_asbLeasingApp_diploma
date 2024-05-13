package com.example.check;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class WriteQuestionActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private DrawerHandler drawerManager;
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextMessage;
    private AppCompatButton buttonSend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_write_question);

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


        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("question");


        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataToFirebase();
            }
        });

    }
    private void sendDataToFirebase() {
        // Получение данных из EditText
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String message = editTextMessage.getText().toString().trim();

        if (!isValidEmail(email)) {
            Toast.makeText(getApplicationContext(), "Пожалуйста, введите действительный адрес электронной почты", Toast.LENGTH_SHORT).show();
            return;
        }

        String questionId = databaseReference.push().getKey();

        if (!name.isEmpty() && !email.isEmpty() && !message.isEmpty()) {
            // Добавление текущей даты и времени
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateAndTime = sdf.format(new Date());

            databaseReference.child(questionId).child("Name").setValue(name);
            databaseReference.child(questionId).child("Email").setValue(email);
            databaseReference.child(questionId).child("Message").setValue(message);
            databaseReference.child(questionId).child("Timestamp").setValue(currentDateAndTime);

            // Очистка полей ввода
            editTextName.setText("");
            editTextEmail.setText("");
            editTextMessage.setText("");

            Toast.makeText(getApplicationContext(), "Успешно отправлено", Toast.LENGTH_SHORT).show();

            returnToFAQActivity();
        } else {
            Toast.makeText(getApplicationContext(), "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
        }
    }
    private void returnToFAQActivity() {
        Intent intent = new Intent(this, FAQActivity.class);
        startActivity(intent);
        finish(); // Закрываем текущую активность
    }

    // Метод для проверки валидности почтового адреса
    private boolean isValidEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }
}