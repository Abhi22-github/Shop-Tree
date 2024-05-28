package com.roaa.shops_customers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
    private static final String TAG = "AboutActivity";

    //widgets
    private ImageButton button;
    private RelativeLayout termsOfServices, privacyPolicyLayout, openSourceLibraryLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        button = findViewById(R.id.about_activity_image_button_back_button);
        termsOfServices = findViewById(R.id.terms_of_sevices);
        privacyPolicyLayout = findViewById(R.id.privacy_policy);
        openSourceLibraryLayout = findViewById(R.id.open_source_libarary);

        openSourceLibraryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutActivity.this, LibrariesActivity.class);
                startActivity(intent);
            }
        });

        termsOfServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://shoptree-seller.flycricket.io/terms.html";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        privacyPolicyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://shoptree-seller.flycricket.io/privacy.html";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }
}