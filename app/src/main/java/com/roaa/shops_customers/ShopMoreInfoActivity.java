package com.roaa.shops_customers;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.roaa.shops_customers.ModelClasses.RatingClass;
import com.roaa.shops_customers.ModelClasses.ShopProfileClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.willy.ratingbar.ScaleRatingBar;

public class ShopMoreInfoActivity extends AppCompatActivity {
    private static final String TAG = "ShopMoreInfoActivity";

    //widgets
    private RelativeLayout backButton;
    private ImageView shopImageView;
    private ProgressBar imageProgressBar;
    private TextView shopName, shopAddress;
    private TextView postButton, shopOpenStatus, shopTime;
    private ScaleRatingBar ratingBarShow, ratingBarEdit;
    private TextView reviewText, ratingNumber;

    //vars
    private ShopProfileClass shopProfileClass;
    private float rating;
    private int reviewCount;

    //Firebase vars
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_more_info);
        getDataFromIntent();
        initFields();

        initFirebase();
        setUpFields();

        backButton.setOnClickListener(v -> onBackPressed());

        postButton.setOnClickListener(v -> saveRatings());

    }

    private void getDataFromIntent() {
        try {
            shopProfileClass = (ShopProfileClass) getIntent().getSerializableExtra("shopProfile");
            rating = getIntent().getFloatExtra("ratings", 0);
            reviewCount = getIntent().getIntExtra("reviews", 0);
            //   Bundle data = getIntent().getBundleExtra("shopOffers");
            // offerClassList = (ArrayList<OfferClass>) data.getSerializable("offerList");
        } catch (Exception e) {

        }
    }

    private void initFields() {
        backButton = findViewById(R.id.more_info_relative_layout_back_button);
        shopImageView = findViewById(R.id.more_info_image_view_shop_image);
        imageProgressBar = findViewById(R.id.more_info_progress_bar_image_progress);
        shopName = findViewById(R.id.more_info_text_view_shop_name);
        shopAddress = findViewById(R.id.more_info_text_view_shop_address);
        ratingBarEdit = findViewById(R.id.simpleRatingBar);
        ratingBarShow = findViewById(R.id.simpleRatingBarEdit);
        postButton = findViewById(R.id.post_button);
        shopOpenStatus = findViewById(R.id.shop_open_status);
        shopTime = findViewById(R.id.opening_closing_time);
        ratingNumber = findViewById(R.id.rating_number);
        reviewText = findViewById(R.id.review_text);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    private void setUpFields() {
        shopName.setText(shopProfileClass.getShopName());
        shopAddress.setText(shopProfileClass.getShopAddress());
        imageProgressBar.setVisibility(View.VISIBLE);
        ratingBarShow.setRating(rating);
        ratingBarEdit.setRating(rating);
        shopTime.setText(shopProfileClass.getShopOpeningTime() + " - " + shopProfileClass.getShopClosingTime());
        ratingNumber.setText(Float.toString(rating));
        reviewText.setText("(" + reviewCount + " Reviews)");
        Picasso.get().load(shopProfileClass.getShopImage()).into(shopImageView, new Callback() {
            @Override
            public void onSuccess() {
                imageProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    private void saveRatings() {
        ratingBarShow.setRating(ratingBarEdit.getRating());
        float rating = ratingBarEdit.getRating();
        RatingClass ratingClass = new RatingClass();
        ratingClass.setRating(String.valueOf(rating));
        rootRef.child("Ratings").child(shopProfileClass.getShopID()).child(mAuth.getCurrentUser().getUid())
                .setValue(ratingClass)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: ratings saved succesffully");
                            postButton.setText("POSTED");
                            postButton.setTextColor(getColor(R.color.gray_icon));
                            postButton.setEnabled(false);
                        }
                    }
                });
    }
}