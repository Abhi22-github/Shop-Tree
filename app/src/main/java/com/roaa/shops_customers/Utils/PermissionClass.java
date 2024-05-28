package com.roaa.shops_customers.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionClass {
    private static final String TAG = "PermissionClass";

    private Context mContext;

    public PermissionClass(Context mContext) {
        this.mContext = mContext;

    }

    public void requestSMSPermission() {
        String smspermission = Manifest.permission.RECEIVE_SMS;
        int grant = ContextCompat.checkSelfPermission(mContext, smspermission);
        //check if read SMS permission is granted or not
        if (grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = smspermission;
            ActivityCompat.requestPermissions((Activity) mContext, permission_list, 1);
        }
    }

}
