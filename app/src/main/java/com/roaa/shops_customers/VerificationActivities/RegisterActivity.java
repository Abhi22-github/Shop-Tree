package com.roaa.shops_customers.VerificationActivities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.roaa.shops_customers.MainActivity;
import com.roaa.shops_customers.R;
import com.roaa.shops_customers.Utils.PermissionClass;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsApi;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.hbb20.CountryCodePicker;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    //layout widgets
    private MaterialButton sendOTPButton;
    private CountryCodePicker countryCodePicker;
    private EditText phoneNumberField;
    private ProgressBar progressBar;
    private TextView privacyPolicy, termsOfService;

    //vars
    private String PhoneNumberText;
    private Context mcontext;
    private static final int CREDENTIAL_PICKER_REQUEST = 1;
    public static Activity registerActivity;
    private PermissionClass permissionClass;
    private Boolean IS_FIRST_TIME = true;

    //firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //initializing functions and fields
        initFields();

        //initializing firebase variables
        initFirebase();

        permissionClass.requestSMSPermission();

        phoneNumberField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (IS_FIRST_TIME) {
                    getPhoneNumberFromSystem();
                    IS_FIRST_TIME = false;
                }
                return false;
            }
        });
        ButtonAppearanceState();

        sendOTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //verifying user phone number

                showProgressDialog();
                PhoneNumberText = phoneNumberField.getText().toString();
                PhoneNumberText = countryCodePicker.getSelectedCountryCodeWithPlus() + PhoneNumberText;
                Log.d(TAG, "onClick: phone number with country code" + PhoneNumberText);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendUserToRegisterOTPVerifyActivity(PhoneNumberText);
                    }
                }, 300);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                    }
                }, 500);


            }
        });


    }


    private void initFields() {
        registerActivity = this;
        sendOTPButton = findViewById(R.id.registration_activity_material_button_send_OTP);
        sendOTPButton.getBackground().setAlpha(100);
        countryCodePicker = findViewById(R.id.registration_activity_CCP_country_code);
        phoneNumberField = findViewById(R.id.registration_activity_edit_text_phone_number);
        mcontext = RegisterActivity.this;
        progressBar = findViewById(R.id.registration_activity_progress_bar_progress);
        permissionClass = new PermissionClass(RegisterActivity.this);
        privacyPolicy = findViewById(R.id.privacy_policy);
        termsOfService = findViewById(R.id.terms_of_sevices);
        privacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());
        termsOfService.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void getPhoneNumberFromSystem() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

        PendingIntent intent = Credentials.getClient(RegisterActivity.this).getHintPickerIntent(hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(), CREDENTIAL_PICKER_REQUEST, null, 0, 0, 0, new Bundle());
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == RESULT_OK) {
            // Obtain the phone number from the result
            Credential credentials = data.getParcelableExtra(Credential.EXTRA_KEY);
            phoneNumberField.setText(credentials.getId().substring(3)); //get the selected phone number
            //Do what ever you want to do with your selected phone number here
            Log.d(TAG, "onActivityResult: " + credentials.getId().substring(3));
            sendUserToRegisterOTPVerifyActivity("+91" + phoneNumberField.getText().toString());


        } else if (requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == CredentialsApi.ACTIVITY_RESULT_NO_HINTS_AVAILABLE) {
            // *** No phone numbers available ***
            Toast.makeText(RegisterActivity.this, "No phone numbers found", Toast.LENGTH_LONG).show();
        }
    }


    private void showProgressDialog() {
        progressBar.setVisibility(View.VISIBLE);
        sendOTPButton.setText("");
        sendOTPButton.setEnabled(false);
    }

    private void hideProgressDialog() {
        progressBar.setVisibility(View.GONE);
        sendOTPButton.setText("Send OTP");
        sendOTPButton.setEnabled(true);
    }

    //button appearance state
    private void ButtonAppearanceState() {
        Log.d(TAG, "ButtonAppearanceState: button appearance method");
        phoneNumberField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (start == 9) {
                    sendOTPButton.getBackground().setAlpha(255);
                    sendOTPButton.setEnabled(true);
                } else {
                    sendOTPButton.getBackground().setAlpha(200);
                    sendOTPButton.setEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start == 9) {
                    sendOTPButton.getBackground().setAlpha(255);
                    sendOTPButton.setEnabled(true);
                } else {
                    sendOTPButton.getBackground().setAlpha(200);
                    sendOTPButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
    }

    /**
     * all intent methods
     */

    private void sendUserToMainActivity() {
        Intent MainIntent = new Intent(mcontext, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

    private void sendUserToRegisterOTPVerifyActivity(String phoneNumberString) {
        Intent Otpactivity = new Intent(mcontext, OTPActivity.class);
        Otpactivity.putExtra("PhoneNumber", phoneNumberString);
        startActivity(Otpactivity);
    }
}