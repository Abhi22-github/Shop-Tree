package com.roaa.shops_customers.Other;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseData extends Application {

    @Override
    public void onCreate() {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        super.onCreate();
    }
}
