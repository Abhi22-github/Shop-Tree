package com.roaa.shops_customers;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.roaa.shops_customers.ModelClasses.UserProfileClass;
import com.roaa.shops_customers.Other.DatabaseClass;
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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import ir.shahabazimi.instagrampicker.InstagramPicker;

public class UserProfileEditActivity extends AppCompatActivity implements TextWatcher {
    private static final String TAG = "UserProfileEditActivity";

    //layout widgets
    private EditText firstNameField, lastNameField, emailField;
    private MaterialButton saveButton;
    private ProgressBar progressBar, loadingProgressBar;
    private ImageButton backButton, addImageButton;
    private RelativeLayout imageHolder;
    private CircleImageView imageView;
    private LinearLayout mainContentLayout;

    //local vars
    private Uri imageUri;
    private String downloadUri = "";
    private UserProfileClass userProfileClass;
    private SharedPreferences preferences;
    private Boolean is_image_changed = false;
    private DatabaseClass databaseClass;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private StorageReference profilePicsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_edit);
        //initialize fields
        initFields();

        //initialize Firebase
        initFirebase();

        setFields();

        imageHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InstagramPicker instagramPicker = new InstagramPicker(UserProfileEditActivity.this);
                instagramPicker.show(1, 1, address -> {
                    imageUri = Uri.parse(address);
                    Log.d(TAG, "onActivityResult: " + imageUri);
                    addImageButton.setVisibility(View.GONE);
                    imageView.setImageURI(imageUri);
                    is_image_changed = true;
                    if (is_image_changed) {
                        saveButton.getBackground().setAlpha(255);
                        saveButton.setEnabled(true);
                    }
                });
            }
        });

        backButton.setOnClickListener(v -> onBackPressed());

        saveButton.setOnClickListener(v -> SaveDataInDatabase());

        firstNameField.addTextChangedListener(this);
        lastNameField.addTextChangedListener(this);
        emailField.addTextChangedListener(this);

    }

    private void initFields() {
        firstNameField = findViewById(R.id.user_profile_edit_activity_edit_text_first_name);
        lastNameField = findViewById(R.id.user_profile_edit_activity_edit_text_last_name);
        emailField = findViewById(R.id.user_profile_edit_activity_edit_text_email);
        saveButton = findViewById(R.id.user_profile_edit_activity_material_button_save_button);
        progressBar = findViewById(R.id.user_profile_edit_activity_progress_bar_progress);
        backButton = findViewById(R.id.user_profile_edit_activity_image_button_back_button);
        addImageButton = findViewById(R.id.user_profile_edit_activity_image_button_add_image_button);
        imageHolder = findViewById(R.id.user_profile_edit_activity_relative_layout_image_holder);
        imageView = findViewById(R.id.user_profile_edit_activity_circle_image_view_user_image);
        preferences = getSharedPreferences("userProfile", MODE_PRIVATE);
        saveButton.getBackground().setAlpha(100);
        saveButton.setEnabled(false);
        databaseClass = new DatabaseClass(UserProfileEditActivity.this);
        loadingProgressBar = findViewById(R.id.user_profile_edit_activity_progress_bar_loading_progress);
        mainContentLayout = findViewById(R.id.user_profile_edit_linear_layout_main_content);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        profilePicsRef = FirebaseStorage.getInstance().getReference().child("User Images");
    }

 /*   //on result for crop intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK
                && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            Log.d(TAG, "onActivityResult: " + imageUri);
            addImageButton.setVisibility(View.GONE);
            imageView.setImageURI(imageUri);
            is_image_changed = true;
            if (is_image_changed) {
                saveButton.getBackground().setAlpha(255);
                saveButton.setEnabled(true);
            }

        } else {
            Log.d(TAG, "onActivityResult: error ,try again");
        }
    }
*/

    private void setFields() {
        showLoadingProgressbar();
        databaseClass.gettingUserProfileDataFromDatabaseWithoutCallback();
        userProfileClass = databaseClass.gettingUserProfileDataFromDevice();

        userProfileClass = databaseClass.gettingUserProfileDataFromDevice();
        try {
            firstNameField.setText(userProfileClass.getFirstName());
            lastNameField.setText(userProfileClass.getLastName());
            try {
                if (userProfileClass.getEmail() == null) {
                } else {
                    emailField.setText(userProfileClass.getEmail());
                }
            } catch (Exception e) {
            }
            try {
                if (userProfileClass.getProfileImage() == null) {

                } else {
                    Picasso.get().load(userProfileClass.getProfileImage()).into(imageView);
                    addImageButton.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                addImageButton.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Toast.makeText(UserProfileEditActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
        hideLoadingProgressbar();
    }

    private void SaveDataInDatabase() {
        if (!firstNameField.getText().toString().isEmpty() && !lastNameField.getText().toString().isEmpty()) {
            Log.d(TAG, "SaveDataInDatabase: fields are valid");
            UploadProfileImageToFirebaseStorage();
        }
    }

    private void UploadInformationToDatabase(HashMap<String, Object> userProfileMap) {

        DocumentReference userRef = firestore.collection("Users").document(mAuth.getCurrentUser().getUid());


        userRef.update(userProfileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: profile is saved in firestore");
                    onBackPressed();
                    hideProgressBar();
                } else {
                    Log.d(TAG, "onComplete: unable to save profile in firestore");
                    hideProgressBar();
                }
            }
        });
    }


    private void UploadProfileImageToFirebaseStorage() {
        showProgressBar();
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
                                        HashMap<String, Object> userProfileMap = new HashMap<>();

                                        userProfileMap.put("firstName", firstNameField.getText().toString());
                                        userProfileMap.put("lastName", lastNameField.getText().toString());
                                        userProfileMap.put("email", emailField.getText().toString());
                                        userProfileMap.put("profileImage", downloadUri);
                                        UploadInformationToDatabase(userProfileMap);
                                    }
                                });
                            } else {
                                Log.d(TAG, "onComplete: failed to store profile image in storage " + task.getException());
                            }
                        }
                    });
        } else {
            HashMap<String, Object> userProfileMap = new HashMap<>();

            userProfileMap.put("firstName", firstNameField.getText().toString());
            userProfileMap.put("lastName", lastNameField.getText().toString());
            userProfileMap.put("email", emailField.getText().toString());
            UploadInformationToDatabase(userProfileMap);
            Log.d(TAG, "UploadProfileImageToFirebaseStorage: image not present");
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        saveButton.getBackground().setAlpha(255);
        saveButton.setEnabled(true);
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setText("");
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        saveButton.setText("Save");
    }

    public void showLoadingProgressbar() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        mainContentLayout.setVisibility(View.GONE);
    }

    public void hideLoadingProgressbar() {
        loadingProgressBar.setVisibility(View.GONE);
        mainContentLayout.setVisibility(View.VISIBLE);
    }

}