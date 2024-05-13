package com.example.check;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;


public class ContactsActivity extends AppCompatActivity {
    private DrawerHandler drawerManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contacts);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ImageView burgerIcon = findViewById(R.id.burgerIcon);
        ImageView phoneIcon = findViewById(R.id.phoneIcon);
        LinearLayout navView = findViewById(R.id.nav_view);
        drawerManager = new DrawerHandler(this, drawerLayout, burgerIcon, phoneIcon, navView);


        Button mapButton = findViewById(R.id.mapButton);
        Button listButton = findViewById(R.id.listButton);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.green)); // Замените на ваш цвет
        }


        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Заменяем текущий фрагмент на MapFragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new MapFragment())
                        .commit();
            }
        });

        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Заменяем текущий фрагмент на ListFragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new ListFragment())
                        .commit();
            }
        });

        mapButton.performClick();
    }
}
