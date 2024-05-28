package com.roaa.shops_customers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.roaa.shops_customers.ModelClasses.UserProfileClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import ir.shahabazimi.instagrampicker.InstagramPicker;

public class UserProfileSetupActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";

    //layout widgets
    private EditText firstNameField, lastNameField, emailField;
    private MaterialButton saveButton;
    private ProgressBar progressBar;
    private ImageButton cancelButton, AddImageButton;
    private RelativeLayout imageHolder;
    private CircleImageView imageView;

    //local vars
    private Uri imageUri;
    private String downloadUri = "";
    boolean doubleBackToExitPressedOnce = false;
    private UserProfileClass userProfileClass;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private StorageReference profilePicsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_setup);

        //initialize fields
        initFields();

        //initialize Firebase
        initFirebase();


        imageHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InstagramPicker instagramPicker = new InstagramPicker(UserProfileSetupActivity.this);
                instagramPicker.show(1, 1, address -> {
                    imageUri = Uri.parse(address);
                    Log.d(TAG, "onActivityResult: " + imageUri);
                    AddImageButton.setVisibility(View.GONE);
                    imageView.setImageURI(imageUri);

                });
            }
        });

        cancelButton.setOnClickListener(v -> onBackPressed());

        saveButton.setOnClickListener(v -> SaveDataInDatabase());
    }


    private void initFields() {
        firstNameField = findViewById(R.id.user_profile_setup_activity_edit_text_first_name);
        lastNameField = findViewById(R.id.user_profile_setup_activity_edit_text_last_name);
        emailField = findViewById(R.id.user_profile_setup_activity_edit_text_email);
        saveButton = findViewById(R.id.user_profile_setup_activity_material_button_save_button);
        progressBar = findViewById(R.id.user_profile_setup_activity_progress_bar_progress);
        cancelButton = findViewById(R.id.user_profile_setup_activity_image_button_cancel_button);
        AddImageButton = findViewById(R.id.user_profile_setup_activity_image_button_add_image_button);
        imageHolder = findViewById(R.id.user_profile_setup_activity_relative_layout_image_holder);
        imageView = findViewById(R.id.user_profile_setup_activity_circle_image_view_user_image);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        profilePicsRef = FirebaseStorage.getInstance().getReference().child("User Images");
    }

  /*  //on result for crop intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK
                && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            Log.d(TAG, "onActivityResult: " + imageUri);
            AddImageButton.setVisibility(View.GONE);
            imageView.setImageURI(imageUri);

        } else {
            Log.d(TAG, "onActivityResult: error ,try again");
        }
    }*/

    private void SaveDataInDatabase() {

        if (!firstNameField.getText().toString().isEmpty() && !lastNameField.getText().toString().isEmpty()) {
            showProgressBar();
            Log.d(TAG, "SaveDataInDatabase: showing progress bar");
            Log.d(TAG, "SaveDataInDatabase: fields are valid");
            UploadProfileImageToFirebaseStorage();
        }

    }

    private void UploadInformationToDatabase(String downloadUri) {

        String phoneNumberString;
        SharedPreferences preferences = getSharedPreferences("User Profile", 0);
        phoneNumberString = preferences.getString("phoneNumber", null);

        DocumentReference userRef = firestore.collection("Users").document(mAuth.getCurrentUser().getUid());
        UserProfileClass userProfileClass = new UserProfileClass();
        userProfileClass.setProfileImage(downloadUri);
        userProfileClass.setFirstName(firstNameField.getText().toString());
        userProfileClass.setLastName(lastNameField.getText().toString());
        userProfileClass.setEmail(emailField.getText().toString());
        userProfileClass.setPhoneNumber(phoneNumberString);

        userRef.set(userProfileClass).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: profile is saved in firestore");
                    SendUserToMainActivity();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressBar();
                        }
                    }, 500);
                } else {
                    Log.d(TAG, "onComplete: unable to save profile in firestore");
                }
            }
        });
    }


    private void UploadProfileImageToFirebaseStorage() {
        Log.d(TAG, "UploadProfileImageToFirebaseStorage: before bitmap");
        if (imageUri != null) {
            Bitmap bmp = null;
            try {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            //here you can choose quality factor in third parameter(ex. i choosen 25)
            bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
            byte[] fileInBytes = baos.toByteArray();

            Log.d(TAG, "UploadProfileImageToFirebaseStorage: after bitmap");
            StorageReference fileRef = profilePicsRef.child(mAuth.getCurrentUser().getUid() + ".jpg");
            fileRef.putBytes(fileInBytes)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: profile image saved in storage");

                                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Uri img = uri;
                                        downloadUri = img.toString();
                                        Log.d(TAG, "onSuccess: " + downloadUri);
                                        UploadInformationToDatabase(downloadUri);
                                    }
                                });
                            } else {
                                Log.d(TAG, "onComplete: failed to store profile image in storage " + task.getException());
                                hideProgressBar();
                            }
                        }
                    });
        } else {
            UploadInformationToDatabase(null);
        }
    }


    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            UserProfileSetupActivity.this.finish();
            System.exit(0);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Tap again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);

    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setText("");
        saveButton.setEnabled(false);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        saveButton.setText("Save");
        saveButton.setEnabled(true);
    }

    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(UserProfileSetupActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

}