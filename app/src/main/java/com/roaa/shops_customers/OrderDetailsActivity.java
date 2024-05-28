package com.roaa.shops_customers;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.roaa.shops_customers.Adapters.OrderSummeryItemAdapter;
import com.roaa.shops_customers.ModelClasses.OrderLink;
import com.roaa.shops_customers.ModelClasses.OrderProgressDetails;
import com.roaa.shops_customers.ModelClasses.ProductOrdersClass;
import com.roaa.shops_customers.ModelClasses.ShopProfileClass;
import com.roaa.shops_customers.ModelClasses.TokenClass;
import com.roaa.shops_customers.ModelClasses.UserProfileClass;
import com.roaa.shops_customers.NotificationSetup.NotificationClass;
import com.roaa.shops_customers.Other.DatabaseClass;
import com.roaa.shops_customers.Other.FirestoreCallback;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDetailsActivity extends AppCompatActivity {
    private static final String TAG = "OrderDetailsActivity";
    private static final int CALL_PERMISSION_CODE = 12;

    //layout widgets
    private ImageButton backButton;
    private TextView shopName, shopAddress, grandTotal, orderDate;
    private RecyclerView recyclerView;
    private MaterialButton inquiryButton, cancelOrderButton;
    private ImageView orderPlacedImageView, orderPendingImageView, orderConfirmedImageView, orderProcessingImageView, orderCompletedImageView;
    private TextView orderPlacedText, orderPendingText, orderConfirmedText, orderProcessingText, orderCompletedText;
    private TextView orderPlacedHeader, orderPendingHeader, orderConfirmedHeader, orderProcessHeader, orderCompletedHeader;
    private RelativeLayout bottomLayout;

    //vars
    private OrderProgressDetails orderProgressDetails;
    private ShopProfileClass shopProfileClass;
    private OrderSummeryItemAdapter adapter;
    private List<ProductOrdersClass> productOrdersClassList = new ArrayList<>();
    private DatabaseClass databaseClass;
    private OrderLink orderLink;

    //firebase
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        getData();

        initFields();

        initFirebase();

        AdView mAdView = findViewById(R.id.adView1);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        AdView mAdViewBottom = findViewById(R.id.adView2);
        AdRequest adRequest1 = new AdRequest.Builder().build();
        mAdViewBottom.loadAd(adRequest1);

        setUpFields();

        backButton.setOnClickListener(v -> onBackPressed());

        backButton.setOnClickListener(v -> onBackPressed());
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                setUpRecyclerView();

                setUpOrderTimeLine();

            }
        });

        inquiryButton.setOnClickListener(v -> callShop());

        cancelOrderButton.setOnClickListener(v -> deleteOrder());

    }


    private void getData() {
        shopProfileClass = (ShopProfileClass) getIntent().getSerializableExtra("shopDetails");
        orderProgressDetails = (OrderProgressDetails) getIntent().getSerializableExtra("orderProgressDetails");
        orderLink = (OrderLink) getIntent().getSerializableExtra("orderLinkList");
    }

    private void initFields() {
        backButton = findViewById(R.id.order_details_activity_image_button_back_button);
        shopName = findViewById(R.id.order_details_activity_text_view_shop_name);
        shopAddress = findViewById(R.id.order_details_activity_text_view_shop_address);
        grandTotal = findViewById(R.id.order_details_activity_text_view_grand_total);
        recyclerView = findViewById(R.id.order_details_activity_recycler_view_order_list);
        inquiryButton = findViewById(R.id.order_details_activity_button_order_inquiry);
        cancelOrderButton = findViewById(R.id.order_details_activity_button_cancel_order);
        databaseClass = new DatabaseClass(OrderDetailsActivity.this);
        orderPlacedImageView = findViewById(R.id.order_placed_image_view);
        orderPendingImageView = findViewById(R.id.order_pending_image_view);
        orderConfirmedImageView = findViewById(R.id.order_confirmed_image_view);
        orderProcessingImageView = findViewById(R.id.order_processing_image_view);
        orderCompletedImageView = findViewById(R.id.order_completed_image_view);
        orderPlacedText = findViewById(R.id.order_places_text);
        orderPendingText = findViewById(R.id.order_pending_text);
        orderConfirmedText = findViewById(R.id.order_confirmed_text);
        orderProcessingText = findViewById(R.id.order_processing_text);
        orderCompletedText = findViewById(R.id.order_completed_text);
        orderPlacedHeader = findViewById(R.id.order_places_header_text);
        orderPendingHeader = findViewById(R.id.order_pending_header_text);
        orderConfirmedHeader = findViewById(R.id.order_confirmed_header_text);
        orderProcessHeader = findViewById(R.id.order_processing_header_text);
        orderCompletedHeader = findViewById(R.id.order_completed_header_text);
        bottomLayout = findViewById(R.id.relativeLayout);
        orderDate = findViewById(R.id.order_details_activity_text_view_date);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();
    }

    private void setUpFields() {
        shopName.setText(shopProfileClass.getShopName());
        shopAddress.setText(shopProfileClass.getShopAddress());
        grandTotal.setText(orderProgressDetails.getOrderTotal());
        orderPlacedText.setText("Your order #" + orderProgressDetails.getOrderCode() + " was placed.");
        orderDate.setText(parseTimeOnly(orderProgressDetails.getOrderCreatedTime()));
    }

    public String parseTimeOnly(String time) {
        String inputPattern = "yyyy/MM/dd HH:mm:ss";
        String outputPatternDate = "dd LLLL yyyy";
        String outputPatternTime = "h:mm a";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormatDate = new SimpleDateFormat(outputPatternDate);
        SimpleDateFormat outputFormatTime = new SimpleDateFormat(outputPatternTime);

        Date date = null;
        String dateString = null;
        String timeString = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            dateString = outputFormatDate.format(date);
            timeString = outputFormatTime.format(date);
            str = dateString + " at " + timeString;
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return str;
    }


    private void setUpRecyclerView() {
        productOrdersClassList.clear();

        rootRef.child("Orders List").child(orderProgressDetails.getOrderID()).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                ProductOrdersClass productOrdersClass = postSnapshot.getValue(ProductOrdersClass.class);
                                productOrdersClassList.add(productOrdersClass);
                            }

                            adapter = new OrderSummeryItemAdapter(productOrdersClassList, OrderDetailsActivity.this);
                            recyclerView.setLayoutManager(new LinearLayoutManager(OrderDetailsActivity.this));
                            recyclerView.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void callShop() {
        if (checkPermissions(
                Manifest.permission.CALL_PHONE,
                CALL_PERMISSION_CODE)) {
            Log.d(TAG, "callShop: 1");
            if (ContextCompat.checkSelfPermission(OrderDetailsActivity.this,
                    Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {

                // Permission is  granted
                Log.d(TAG, "callShop: 2");
                firestore.collection("Users").document(shopProfileClass.getShopID()).get().
                        addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    UserProfileClass userProfileClass = documentSnapshot.toObject(UserProfileClass.class);
                                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                                    callIntent.setData(Uri.parse("tel:" + userProfileClass.getPhoneNumber()));//change the number
                                    startActivity(callIntent);

                                }
                            }
                        });
            } else if (ContextCompat.checkSelfPermission(OrderDetailsActivity.this,
                    Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_DENIED) {
                checkPermissions(
                        Manifest.permission.CALL_PHONE,
                        CALL_PERMISSION_CODE);
                Log.d(TAG, "callShop: 3");
            }

        }


    }

    private void deleteOrder() {

        databaseClass.gettingUserTokenFromDatabaseWithCallbacks(new FirestoreCallback() {
            @Override
            public void dataGetComplete() {
                TokenClass tokenClass = databaseClass.gettingUserTokenFromDevice();
                NotificationClass notificationClass = new NotificationClass();
                Map<String, Object> orderStatus = new HashMap<>();

                orderStatus.put("orderStatus", 5);
                rootRef.child("Orders Details").child(orderLink.getOrderDetailID())
                        .updateChildren(orderStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "onComplete: order has been cancelled");
                        notificationClass.createMessage("Your order has been cancelled",
                                "Your order having number " + orderProgressDetails.getOrderCode()
                                        + " has been cancelled.", tokenClass.getToken());

                    }
                });

                DocumentReference tokenRef = firestore.collection("Seller Tokens").document(orderProgressDetails.getShopID());

                tokenRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            TokenClass tokenClass = documentSnapshot.toObject(TokenClass.class);
                            NotificationClass notificationClass = new NotificationClass();
                                    onBackPressed();
                                    Log.d(TAG, "onComplete: order has been cancelled");
                                    notificationClass.createMessage(" Order #" + orderProgressDetails.getOrderCode() + " has been cancelled",
                                            "Order having number #" + orderProgressDetails.getOrderCode()
                                                    + " has been cancelled.", tokenClass.getToken());

                        }
                    }
                });














            }
        });
    }
    // This function is called when user accept or decline the permission.
// Request Code is used to check which permission called this function.
// This request code is provided when user is prompt for permission.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == CALL_PERMISSION_CODE) {

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {

            }
        }

    }

    private boolean checkPermissions(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(OrderDetailsActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(OrderDetailsActivity.this,
                    new String[]{permission},
                    requestCode);
            return false;
        } else {
            return true;
        }
    }

    private void setUpOrderTimeLine() {
        switch (orderProgressDetails.getOrderStatus()) {
            case 1:
                //pending

                break;
            case 2:
                //Confirmed
                orderPendingImageView.setImageDrawable(getDrawable(R.drawable.ic_order_tracker_completed_state));
                break;
            case 3:
                //Processing
                orderPendingImageView.setImageDrawable(getDrawable(R.drawable.ic_order_tracker_completed_state));
                orderConfirmedImageView.setImageDrawable(getDrawable(R.drawable.ic_order_tracker_completed_state));
                break;
            case 4:
                //completed
                orderPendingImageView.setImageDrawable(getDrawable(R.drawable.ic_order_tracker_completed_state));
                orderConfirmedImageView.setImageDrawable(getDrawable(R.drawable.ic_order_tracker_completed_state));
                orderProcessingImageView.setImageDrawable(getDrawable(R.drawable.ic_order_tracker_completed_state));
                orderCompletedImageView.setImageDrawable(getDrawable(R.drawable.ic_order_tracker_completed_state));
                break;
            case 5:
                //cancelled
                //hiding buttons
                bottomLayout.setVisibility(View.GONE);
                //hiding others
                orderCompletedText.setVisibility(View.GONE);
                orderCompletedImageView.setVisibility(View.GONE);
                orderCompletedHeader.setVisibility(View.GONE);

                orderProcessHeader.setVisibility(View.GONE);
                orderProcessingImageView.setVisibility(View.GONE);
                orderProcessingText.setVisibility(View.GONE);

                orderConfirmedHeader.setVisibility(View.GONE);
                orderConfirmedText.setVisibility(View.GONE);
                orderConfirmedImageView.setVisibility(View.GONE);

                //setting cancel
                orderPendingHeader.setText("Order Cancelled");
                orderPendingText.setText("Your order #" + orderProgressDetails.getOrderCode() + " was cancelled.");
                orderPendingImageView.setImageDrawable(getDrawable(R.drawable.ic_order_tracker_cancelled_state));
                break;

        }
    }

}