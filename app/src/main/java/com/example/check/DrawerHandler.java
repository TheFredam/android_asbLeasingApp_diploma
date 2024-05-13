package com.example.check;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;

public class DrawerHandler {
    private DrawerLayout drawerLayout;
    private ImageView burgerIcon;
    private ImageView phoneIcon;
    private LinearLayout navView;
    private TextView menuItem1;
    private TextView menuItem2;
    private TextView menuItem3;
    private TextView menuItem4;
    private TextView menuItem5;
    private TextView menuItem6;
    private Context context;

    public DrawerHandler(Context context, DrawerLayout drawerLayout, ImageView burgerIcon, ImageView phoneIcon, LinearLayout navView) {
        this.context = context;
        this.drawerLayout = drawerLayout;
        this.burgerIcon = burgerIcon;
        this.phoneIcon = phoneIcon;
        this.navView = navView;
        initializeViews();
    }

    private void initializeViews() {
        menuItem1 = navView.findViewById(R.id.menu_item1);
        menuItem1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        });
        menuItem2 = navView.findViewById(R.id.menu_item2);
        menuItem2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NewsActivity.class);
                context.startActivity(intent);
            }
        });
        menuItem3 = navView.findViewById(R.id.menu_item3);
        menuItem3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ContactsActivity.class);
                context.startActivity(intent);
            }
        });
        menuItem4 = navView.findViewById(R.id.menu_item4);
        menuItem4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PartnersActivity.class);
                context.startActivity(intent);
            }
        });
        menuItem6 = navView.findViewById(R.id.menu_item6);
        menuItem6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FAQActivity.class);
                context.startActivity(intent);
            }
        });

        burgerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDrawer();
            }
        });
        phoneIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = "+375172151153";
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                if (callIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(callIntent);
                } else {
                    Toast.makeText(context, "Приложение для звонка не найдено", Toast.LENGTH_SHORT).show();
                }
            }
        });

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        navView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Предотвращаем закрытие бургер-меню
            }
        });
    }

    private void toggleDrawer() {
        if (drawerLayout.isDrawerOpen(navView)) {
            drawerLayout.closeDrawer(navView);
        } else {
            drawerLayout.openDrawer(navView);
        }
    }
}
