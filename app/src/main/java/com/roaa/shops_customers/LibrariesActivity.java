package com.roaa.shops_customers;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;

public class LibrariesActivity extends AppCompatActivity {
    private static final String TAG = "LibrariesActivity";

    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libraries);
        backButton = findViewById(R.id.libraries_activity_image_button_back_button);
        backButton.setOnClickListener(v -> onBackPressed());
    }
}