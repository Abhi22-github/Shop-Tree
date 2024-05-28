package com.roaa.shops_customers.VerificationActivities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.roaa.shops_customers.MainActivity;
import com.roaa.shops_customers.Other.DatabaseClass;
import com.roaa.shops_customers.Other.OTPReceiverClass;
import com.roaa.shops_customers.R;
import com.roaa.shops_customers.UserProfileSetupActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

import in.aabhasjindal.otptextview.OTPListener;
import in.aabhasjindal.otptextview.OtpTextView;

public class OTPActivity extends AppCompatActivity {
    private static final String TAG = "OTPActivity";

    //layout widgets
    private ImageButton backButton;
    private TextView phoneNumberShow;
    private TextView resendCounter, resendOTPButton;
    private OtpTextView otpTextView;
    private MaterialButton submitButton;
    private ProgressBar submitProgressBar;

    //local variables
    private static String phoneNumber;
    private Context mContext;
    private static CountDownTimer countDownTimer;
    private String codeFromSystem;
    private DatabaseClass databaseClass;
    private OTPReceiverClass otpReceiverClass = new OTPReceiverClass();

    //Firebase vars
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_o_t_p);

        getValues();

        initFields();

        initFirebase();

        backButton.setOnClickListener(v -> finish());

        //button appearance
        ButtonAppearanceState();

        sendVerificationCodeToUser();


        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    IntentFilter filter = new IntentFilter("com.example.Broadcast");
                    registerReceiver(otpReceiverClass, filter);
                    otpReceiverClass.setOTP(otpTextView);
                } catch (Exception e) {

                }
            }
        });

        otpCompleteListenerMethod();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = otpTextView.getOTP();
                if (!code.isEmpty()) {
                    showProgressDialog();
                    verifyCode(code);
                }
            }
        });


        /*signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = otpTextView.getOTP();
                if (!code.isEmpty()) {
                    showProgressDialog();
                    verifyCode(code);
                }
            }
        });*/
        resendOTPButton.setOnClickListener(v -> resendVerificationCode(phoneNumber, resendingToken));
    }

    private void otpCompleteListenerMethod() {

        otpTextView.setOtpListener(new OTPListener() {
            @Override
            public void onInteractionListener() {

            }

            @Override
            public void onOTPComplete(String otp) {
                String code = otpTextView.getOTP();
                if (!code.isEmpty()) {
                    // showProgressDialog();
                    verifyCode(code);
                }
            }
        });
    }

    private void showProgressDialog() {
        submitProgressBar.setVisibility(View.VISIBLE);
        submitButton.setText("");
        submitButton.setEnabled(false);
    }

    private void hideProgressDialog() {
        submitProgressBar.setVisibility(View.GONE);
        submitButton.setText("Sign Up");
        submitButton.setEnabled(true);
    }

    private void sendVerificationCodeToUser() {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(OTPActivity.this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, "OTP sent successfully", Toast.LENGTH_SHORT).show();
                startTimer(60 * 1000);
            }


        }, 0);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    codeFromSystem = s;
                    resendingToken = forceResendingToken;

                }

                @Override
                public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                    String code = phoneAuthCredential.getSmsCode();
                    if (code != null) {


                        verifyCode(code);
                    }
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Toast.makeText(OTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    sendUserToRegisterOTPActivity();
                }
            };

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeFromSystem, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void initFields() {
        backButton = findViewById(R.id.otp_activity_image_button_back_button);
        submitButton = findViewById(R.id.otp_activity_submit_button);
        submitProgressBar = findViewById(R.id.otp_activity_submit_button_loading);
        submitButton.getBackground().setAlpha(100);
        submitButton.setEnabled(false);
        mContext = OTPActivity.this;
        phoneNumberShow = findViewById(R.id.otp_activity_text_view_number);
        databaseClass = new DatabaseClass(OTPActivity.this);
        phoneNumberShow.setText(phoneNumber);
        otpTextView = findViewById(R.id.otp_activity_otp_text_view_otp_view);
        resendCounter = findViewById(R.id.otp_activity_text_view_counter);
        resendOTPButton = findViewById(R.id.otp_activity_text_view_resend_otp);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    private void getValues() {
        phoneNumber = getIntent().getStringExtra("PhoneNumber");
    }


    //button appearance state
    private void ButtonAppearanceState() {
        Log.d(TAG, "ButtonAppearanceState: button appearance method");
        otpTextView.setOtpListener(new OTPListener() {
            @Override
            public void onInteractionListener() {

            }

            @Override
            public void onOTPComplete(String otp) {

            }
        });
    }
/*
    private void showProgressDialog() {
        progressBar.setVisibility(View.VISIBLE);
        signUpButton.setText("");
        signUpButton.setEnabled(false);
    }

    private void hideProgressDialog() {
        progressBar.setVisibility(View.GONE);
        signUpButton.setText("Sign Up");
        signUpButton.setEnabled(true);
    }*/

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        //showProgressDialog();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((AppCompatActivity) mContext, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //currentUserID = mAuth.getCurrentUser().getUid();
                            //side method
                            verifyUserExistence();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    addPhoneNumberInDevice();
                                     hideProgressDialog();
                                }
                            }, 300);
                            unregisterReceiver(otpReceiverClass);
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                //Toast.makeText(mContext, "Incorrect OTP", Toast.LENGTH_SHORT).show();
                                 hideProgressDialog();
                                ButtonAppearanceState();
                            }
                            hideProgressDialog();
                        }
                    }


                });
    }


    public void resendVerificationCode(String phoneNumber,
                                       PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                OTPActivity.this,           //a reference to an activity if this method is in a custom service
                mCallbacks,
                token);
        startTimer(60 * 1000);// resending with token got at previous call's `callbacks` method `onCodeSent`
    }

    private void addPhoneNumberInDevice() {

        SharedPreferences preferences = getSharedPreferences("User Profile", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("phoneNumber", phoneNumber);
        editor.apply();
        return;
    }

    //Start Countdown method
    private void startTimer(int noOfMinutes) {
        countDownTimer = new CountDownTimer(noOfMinutes, 1000) {
            public void onTick(long millisUntilFinished) {
                long millis = millisUntilFinished;
                //Convert milliseconds into hour,minute and seconds
                String hms = String.format("%01d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
                resendCounter.setText(hms);//set text
            }

            public void onFinish() {

                resendCounter.setText("0:00"); //On finish change timer text
                countDownTimer = null;//set CountDownTimer to null
                resendOTPButton.setEnabled(true);
                resendOTPButton.setTextColor(getResources().getColor(R.color.sky_blue));
            }
        }.start();

    }

    private void verifyUserExistence() {
        String currentUserID = mAuth.getCurrentUser().getUid();
        DocumentReference userStoreRef = firestore.collection("Users").document(currentUserID);
        userStoreRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        try {
                            databaseClass.gettingUserProfileDataFromDatabaseWithoutCallback();
                            databaseClass.gettingUserBookmarksDataFromDatabaseWithoutCallbacks();
                            databaseClass.savingUserTokenInDatabase();
                            sendUserToMainActivity();
                        } catch (Exception e) {
                            sendUserToUserNameActivity();
                        }

                    } else {
                        sendUserToUserNameActivity();

                    }
                } else {
                    Toast.makeText(OTPActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });

    }

    private void sendUserToMainActivity() {
        Intent MainIntent = new Intent(mContext, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        RegisterActivity.registerActivity.finish();
        finish();
    }

    private void sendUserToRegisterOTPActivity() {
        Intent RegisterIntent = new Intent(mContext, RegisterActivity.class);
        RegisterIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(RegisterIntent);
        finish();
    }

    private void sendUserToUserNameActivity() {
        Intent userNameIntent = new Intent(OTPActivity.this, UserProfileSetupActivity.class);
        userNameIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        userNameIntent.putExtra("from", "MainActivity");
        startActivity(userNameIntent);
        finish();
    }
}