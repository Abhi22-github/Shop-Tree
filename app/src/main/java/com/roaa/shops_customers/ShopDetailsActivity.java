package com.roaa.shops_customers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.roaa.shops_customers.Adapters.CartOrderItemAdapter;
import com.roaa.shops_customers.Adapters.GroupItemAdapter;
import com.roaa.shops_customers.ModelClasses.BookmarkClass;
import com.roaa.shops_customers.ModelClasses.CatalogImageClass;
import com.roaa.shops_customers.ModelClasses.CategoryClass;
import com.roaa.shops_customers.ModelClasses.LocationClass;
import com.roaa.shops_customers.ModelClasses.OrderLink;
import com.roaa.shops_customers.ModelClasses.OrderProgressDetails;
import com.roaa.shops_customers.ModelClasses.ProductClass;
import com.roaa.shops_customers.ModelClasses.ProductOrdersClass;
import com.roaa.shops_customers.ModelClasses.RatingClass;
import com.roaa.shops_customers.ModelClasses.ShopProfileClass;
import com.roaa.shops_customers.ModelClasses.TokenClass;
import com.roaa.shops_customers.ModelClasses.UserProfileClass;
import com.roaa.shops_customers.NotificationSetup.NotificationClass;
import com.roaa.shops_customers.Other.DatabaseClass;
import com.roaa.shops_customers.Other.FirestoreCallback;
import com.roaa.shops_customers.Other.ProductsCallback;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.willy.ratingbar.ScaleRatingBar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ShopDetailsActivity extends AppCompatActivity implements ProductsCallback {
    private static final String TAG = "ShopDetailsActivity";

    //layout widgets
    private ImageButton backButton;
    private TextView shopName, shopCategory, shopAddress;
    private RecyclerView groupRecyclerView, cartRecyclerView, catalogRecyclerView;
    //private LinearLayout offerLinearLayout;
    private BottomSheetBehavior bottomSheetBehavior;
    private ConstraintLayout bottomSheetLayout;
    private CoordinatorLayout bottomSheetContainer;
    private NestedScrollView nestedScrollView;
    private TextView grandTotal;
    private MaterialButton placeOrder, discardCart;
    private AdView mAdView;
    private ImageButton directionButton;
    private TextView detailsText;
    private ImageButton catalogImageDropDownButton;
    private MaterialButton bookmarkButton, callButton;
    private ProgressBar progressBar;
    private LinearLayout mainContentLayout;
    private RelativeLayout relativeLayout;
    private TextView moreInfoButton;
    private ScaleRatingBar ratingBar;
    private TextView reviewText, ratingNumber;
    private LinearLayout catalogLayout;

    //vars
    private ShopProfileClass shopProfileClass;
    //private List<OfferClass> offerClassList;
    private Map<ProductClass, Integer> cartMap = new HashMap<>();
    private List<ProductOrdersClass> cartList = new ArrayList<>();
    private CartOrderItemAdapter cartOrderItemAdapter;
    private HashMap<String, ProductOrdersClass> productOrdersMap = new HashMap<>();
    private int ITEM_TOTAL;
    private DatabaseClass databaseClass;
    private InterstitialAd mInterstitialAd;
    private List<BookmarkClass> bookmarkClassList = new ArrayList<>();
    private static final int CALL_PERMISSION_CODE = 12;
    private List<RatingClass> ratingClassList = new ArrayList<>();

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private DatabaseReference rootRef;
    private FirestoreRecyclerAdapter firestoreProductGroupRecyclerAdapter, firestoreCatalogRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        getDataFromIntent();

        initFields();

        initFirebase();
        progressBar.setVisibility(View.VISIBLE);

        setUpFields();

        backButton.setOnClickListener(v -> onBackPressed());

        setUpCatalogRecyclerView();

        setUpGroupRecyclerView();

        catalogImageDropDownButton.setOnClickListener(v -> setCatalogDropDown());

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        discardCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                bottomSheetContainer.setVisibility(View.GONE);
            }
        });

        placeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveOrderOnDatabase();
            }
        });

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

       /* mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7612815003689147/5279681992");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                // mInterstitialAd.loadAd(new AdRequest.Builder().build());
                onBackPressed();
            }

        });*/
        directionButton.setOnClickListener(v -> displayDirection());

        setupSaveButton(shopProfileClass.getShopID());

        bookmarkButton.setOnClickListener(v -> addAndDeleteBookmark(shopProfileClass.getShopID()));

        callButton.setOnClickListener(v -> callShop());
        moreInfoButton.setOnClickListener(v -> sendUserToMoreDetailsActivity());
        firestoreProductGroupRecyclerAdapter.startListening();
        firestoreCatalogRecyclerAdapter.startListening();
    }

    private void displayDirection() {
        firestore.collection("Shop Locations").document(shopProfileClass.getShopID()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                LocationClass locationClass = documentSnapshot.toObject(LocationClass.class);
                                GeoPoint geoPoint = locationClass.getShop_location();
                                double latitude = geoPoint.getLatitude();
                                double longitude = geoPoint.getLongitude();

                                Intent intent = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("google.navigation:q=" + latitude + "," + longitude));
                                intent.setPackage("com.google.android.apps.maps");
                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(ShopDetailsActivity.this, "Please install maps", Toast.LENGTH_SHORT).show();
                                }

                            }

                        }
                    }
                });
    }


    private void getDataFromIntent() {
        try {
            shopProfileClass = (ShopProfileClass) getIntent().getSerializableExtra("shopDetails");
            //   Bundle data = getIntent().getBundleExtra("shopOffers");
            // offerClassList = (ArrayList<OfferClass>) data.getSerializable("offerList");
        } catch (Exception e) {

        }
    }

    private void initFields() {
        backButton = findViewById(R.id.shop_details_activity_image_button_back_button);
        shopName = findViewById(R.id.shop_details_activity_text_view_shop_name);
        shopCategory = findViewById(R.id.shop_details_activity_text_view_shop_category);
        shopAddress = findViewById(R.id.shop_details_activity_text_view_shop_address);
        groupRecyclerView = findViewById(R.id.shop_details_activity_recycler_view_shop_products);
        //offerLinearLayout = findViewById(R.id.shop_details_activity_linear_layout_offer_layout);
        bottomSheetLayout = findViewById(R.id.bottom_sheet_layout);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        cartRecyclerView = findViewById(R.id.bottom_sheet_layout_order_list_recycler_view);
        bottomSheetContainer = findViewById(R.id.shop_details_activity_coordinate_layout_bottom_sheet_container);
        nestedScrollView = findViewById(R.id.shop_details_activity_nested_scroll_view_scroll_view);
        grandTotal = findViewById(R.id.bottom_sheet_layout_cart_text_view_grand_total);
        placeOrder = findViewById(R.id.bottom_sheet_layout_cart_button_place_order_button);
        discardCart = findViewById(R.id.bottom_sheet_layout_cart_button_discard_button);
        databaseClass = new DatabaseClass(ShopDetailsActivity.this);
        directionButton = findViewById(R.id.shop_details_activity_image_button_direction_button);
        detailsText = findViewById(R.id.surface_details);
        catalogRecyclerView = findViewById(R.id.shop_details_activity_recycler_view_catalog_recycler_view);
        catalogImageDropDownButton = findViewById(R.id.shop_details_activity_image_button_catalog_drop_down);
        bookmarkButton = findViewById(R.id.bookmark_button);
        callButton = findViewById(R.id.call_button);
        mainContentLayout = findViewById(R.id.main_content_linear_layout);
        progressBar = findViewById(R.id.progress_bar);
        relativeLayout = findViewById(R.id.rel_layout);
        moreInfoButton = findViewById(R.id.more_info_button);
        ratingBar = findViewById(R.id.ratings);
        ratingNumber = findViewById(R.id.rating_number);
        reviewText = findViewById(R.id.review_text);
        catalogLayout = findViewById(R.id.shop_details_activity_relative_layout_catalog_layout);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
    }


    private void setUpFields() {
        try {
            shopName.setText(shopProfileClass.getShopName());
            shopAddress.setText(shopProfileClass.getShopAddress());
            shopCategory.setText(shopProfileClass.getShopCategory());


            calculateRatings();
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong ", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateRatings() {
        rootRef.child("Ratings").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ratingClassList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    RatingClass ratingClass = postSnapshot.getValue(RatingClass.class);
                    ratingClassList.add(ratingClass);
                }
                float rating = 0;
                for (RatingClass ratingClass : ratingClassList) {
                    rating = rating + Float.parseFloat(ratingClass.getRating());
                }
                ratingBar.setRating(rating);
                ratingNumber.setText(Float.toString(rating));
                reviewText.setText("("+ratingClassList.size()+" Reviews)");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();

    }



    private void setUpGroupRecyclerView() {
        //Query
        Query query = firestore.collection("Shops").document(shopProfileClass.getShopID())
                .collection("Category");
        //Recycler options
        FirestoreRecyclerOptions<CategoryClass> options = new FirestoreRecyclerOptions.Builder<CategoryClass>()
                .setQuery(query, CategoryClass.class)
                .build();

        firestoreProductGroupRecyclerAdapter = new FirestoreRecyclerAdapter<CategoryClass, CategoryViewHolder>(options) {
            @NonNull
            @Override
            public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_group_recycler_view, parent, false);
                return new CategoryViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CategoryViewHolder holder, int position, @NonNull CategoryClass model) {
                holder.groupName.setText(model.getCategoryName());

                if (holder.recyclerLayout.getVisibility() == View.VISIBLE) {
                    holder.dropDownButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_keyboard_arrow_up));
                } else {
                    holder.dropDownButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_keyboard_arrow_down));
                }

                holder.dropDownButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (holder.recyclerLayout.getVisibility() == View.GONE) {
                            holder.recyclerLayout.setVisibility(View.VISIBLE);
                            holder.dropDownButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_keyboard_arrow_up));
                        } else {
                            holder.recyclerLayout.setVisibility(View.GONE);
                            holder.dropDownButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_keyboard_arrow_down));
                        }
                    }
                });
                holder.dropDownLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (holder.recyclerLayout.getVisibility() == View.GONE) {
                            holder.recyclerLayout.setVisibility(View.VISIBLE);
                            holder.dropDownButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_keyboard_arrow_up));
                        } else {
                            holder.recyclerLayout.setVisibility(View.GONE);
                            holder.dropDownButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_keyboard_arrow_down));
                        }
                    }
                });


                //setting up recycler view
                Log.d(TAG, "onEvent: creating inside recycler view");
                Object productCallback;
                GroupItemAdapter adapter = new GroupItemAdapter(model, shopProfileClass, ShopDetailsActivity.this);
                adapter.getProductData(new FirestoreCallback() {
                    @Override
                    public void dataGetComplete() {
                        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
                        holder.groupItemRecyclerView.setLayoutManager(staggeredGridLayoutManager);
                        holder.groupItemRecyclerView.setAdapter(adapter);
                    }
                });
            }
        };

        groupRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupRecyclerView.setAdapter(firestoreProductGroupRecyclerAdapter);

    }

    @Override
    public void sendProductAndQuantity(ProductClass productClass, int Quantity, int action) {
        // showBottomSheet();

        if (action == 0) {
            ProductOrdersClass productOrdersClass = new ProductOrdersClass();
            productOrdersClass.setProductID(productClass.getProductID());
            productOrdersClass.setProductPrice(productClass.getProductPrice());
            productOrdersClass.setProductQuantity(String.valueOf(1));
            productOrdersClass.setProductName(productClass.getProductName());
            productOrdersClass.setAvailable(productClass.getAvailable());
            cartList.add(productOrdersClass);
        }
        if (action == 1) {
            for (int i = 0; i < cartList.size(); i++) {
                if (cartList.get(i).getProductID().equals(productClass.getProductID())) {
                    cartList.get(i).setProductQuantity(String.valueOf(Integer.parseInt(cartList.get(i).getProductQuantity()) + 1));
                }
            }
            cartOrderItemAdapter.notifyDataSetChanged();
        }
        if (action == 2) {
            for (int i = 0; i < cartList.size(); i++) {
                if (cartList.get(i).getProductID().equals(productClass.getProductID())) {
                    cartList.get(i).setProductQuantity(String.valueOf(Integer.parseInt(cartList.get(i).getProductQuantity()) - 1));
                }
            }
            cartOrderItemAdapter.notifyDataSetChanged();
        }
        if (action == 3) {
            for (int i = 0; i < cartList.size(); i++) {
                if (cartList.get(i).getProductID().equals(productClass.getProductID())) {
                    cartList.remove(i);
                }
            }
            cartOrderItemAdapter.notifyDataSetChanged();

        }


        if (cartList.isEmpty()) {
            bottomSheetContainer.setVisibility(View.GONE);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) nestedScrollView.getLayoutParams();
            layoutParams.setMargins(0, 0, 0, 0);
            nestedScrollView.setLayoutParams(layoutParams);

        } else {
            bottomSheetContainer.setVisibility(View.VISIBLE);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) nestedScrollView.getLayoutParams();
            layoutParams.setMargins(0, 0, 0, 140);
            nestedScrollView.setLayoutParams(layoutParams);
            cartOrderItemAdapter = new CartOrderItemAdapter(cartList, ShopDetailsActivity.this);
            setUpCartRecyclerView();
        }

        double TOTAL_PRICE = 0.00;
        int ITEM_TOTAL_TEMP = 0;

        for (int i = 0; i < cartList.size(); i++) {

            TOTAL_PRICE = TOTAL_PRICE + Double.parseDouble(cartList.get(i).getProductPrice().trim()) * Integer.parseInt(cartList.get(i).getProductQuantity().trim());
            ITEM_TOTAL_TEMP = ITEM_TOTAL_TEMP + Integer.parseInt(cartList.get(i).getProductQuantity());

        }
        grandTotal.setText("Rs " + String.format("%.2f", TOTAL_PRICE));
        ITEM_TOTAL = ITEM_TOTAL_TEMP;
        detailsText.setText(cartList.size() + " Items | Rs " + String.format("%.2f", TOTAL_PRICE));

    }

    private class CategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView groupName;
        private ImageButton dropDownButton;
        private RecyclerView groupItemRecyclerView;
        private RelativeLayout recyclerLayout;
        private RelativeLayout dropDownLayout;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.product_group_recycler_group_name);
            dropDownButton = itemView.findViewById(R.id.product_group_recycler_group_drop_down_icon);
            groupItemRecyclerView = itemView.findViewById(R.id.product_item_recycler_view_group_items);
            recyclerLayout = itemView.findViewById(R.id.product_item_relative_layout_group_items);
            dropDownLayout = itemView.findViewById(R.id.product_group_recycler_relative_layout_drop_down);
        }
    }

    private void setCatalogDropDown() {
        if (catalogRecyclerView.getVisibility() == View.VISIBLE) {
            catalogImageDropDownButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_keyboard_arrow_down));
            catalogRecyclerView.setVisibility(View.GONE);
        } else {
            catalogImageDropDownButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_keyboard_arrow_up));
            catalogRecyclerView.setVisibility(View.VISIBLE);

        }
    }

    private void setUpCartRecyclerView() {
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(ShopDetailsActivity.this));
        cartRecyclerView.setAdapter(cartOrderItemAdapter);

    }

    private void saveOrderOnDatabase() {
        for (ProductOrdersClass productOrdersClass : cartList) {
            productOrdersMap.put(productOrdersClass.getProductID(), productOrdersClass);
        }
        //getting unique id of reference of products order list
        String randomIDInOrderList = rootRef.child("Orders List").push().getKey();

        rootRef.child("Orders List").child(randomIDInOrderList).setValue(productOrdersMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //cretating order details object
                            OrderProgressDetails orderProgressDetails = new OrderProgressDetails();
                            orderProgressDetails.setUserID(mAuth.getCurrentUser().getUid());
                            orderProgressDetails.setOrderID(randomIDInOrderList);
                            orderProgressDetails.setShopID(shopProfileClass.getShopID());
                            orderProgressDetails.setModified(false);
                            orderProgressDetails.setOrderTotal(grandTotal.getText().toString());
                            orderProgressDetails.setTotalItemCount(ITEM_TOTAL);
                            orderProgressDetails.setOrderStatus(1);
                            Random random = new Random();
                            orderProgressDetails.setOrderCode(String.format("%04d", random.nextInt(10000)));
                            //getting date
                            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            Date date = new Date();
                            String time = dateFormat.format(date).toString();
                            orderProgressDetails.setOrderCreatedTime(time);
                            String randomIDInOrderDetails = rootRef.child("Orders Details").push().getKey();
                            rootRef.child("Orders Details").child(randomIDInOrderDetails).setValue(orderProgressDetails)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            OrderLink orderLink = new OrderLink();
                                            orderLink.setOrderDetailID(randomIDInOrderDetails);
                                            orderLink.setCurrentTime(time);

                                            rootRef.child("User Orders Details Link").child(mAuth.getCurrentUser().getUid()).
                                                    child(randomIDInOrderDetails).setValue(orderLink)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            rootRef.child("Shop Orders Details Link").child(shopProfileClass.getShopID()).
                                                                    child(randomIDInOrderDetails).setValue(orderLink)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            Log.d(TAG, "onComplete: data has been saved successfully");

                                                                            sendingNotificationToShop(orderProgressDetails);

                                                                            sendingNotificationToUser(orderProgressDetails);

                                                                           // if (mInterstitialAd.isLoaded()) {
                                                                          //      mInterstitialAd.show();
                                                                          //  } else {
                                                                                Log.d("TAG", "The interstitial wasn't loaded yet.");
                                                                                onBackPressed();
                                                                         //   }

                                                                        }
                                                                    });


                                                        }
                                                    });

                                        }
                                    });
                        } else {

                        }
                    }
                });


    }

    private void sendingNotificationToShop(OrderProgressDetails orderProgressDetails) {

        databaseClass.gettingUserProfileDataFromDatabaseWithCallback(new FirestoreCallback() {
            @Override
            public void dataGetComplete() {
                UserProfileClass userProfileClass = databaseClass.gettingUserProfileDataFromDevice();

                firestore.collection("Seller Tokens").document(shopProfileClass.getShopID()).get().addOnCompleteListener(
                        new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    TokenClass tokenClass = documentSnapshot.toObject(TokenClass.class);

                                    NotificationClass notificationClass = new NotificationClass();
                                    notificationClass.createMessage("Order Request Arrived",
                                            "Mr. " + userProfileClass.getFirstName() + " " + userProfileClass.getLastName() + " has " +
                                                    "placed an order for " + orderProgressDetails.getTotalItemCount() + " items from your shop",
                                            tokenClass.getToken());
                                } else {

                                }
                            }
                        }
                );


            }
        });
    }

    private void sendingNotificationToUser(OrderProgressDetails orderProgressDetails) {
        databaseClass.gettingUserTokenFromDatabaseWithCallbacks(new FirestoreCallback() {
            @Override
            public void dataGetComplete() {
                TokenClass tokenClass = databaseClass.gettingUserTokenFromDevice();
                NotificationClass notificationClass = new NotificationClass();
                notificationClass.createMessage("Your order has been placed",
                        "Your order of " + orderProgressDetails.getTotalItemCount() + " Items has been placed.",
                        tokenClass.getToken());
            }
        });
    }

    private void setUpCatalogRecyclerView() {
        //Query
        Query query = firestore.collection("Shops").document(shopProfileClass.getShopID())
                .collection("Catalog").orderBy("catalogImageDate");
        //Recycler options
        FirestoreRecyclerOptions<CatalogImageClass> options = new FirestoreRecyclerOptions.Builder<CatalogImageClass>()
                .setQuery(query, CatalogImageClass.class)
                .build();

        if(options.getSnapshots().size() == 0){
            catalogLayout.setVisibility(View.GONE);
        }else {
            catalogLayout.setVisibility(View.VISIBLE);
        }

        firestoreCatalogRecyclerAdapter = new FirestoreRecyclerAdapter<CatalogImageClass, CatalogViewHolder>(options) {
            @NonNull
            @Override
            public CatalogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.catalog_item_recycler_view, parent, false);
                return new CatalogViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CatalogViewHolder holder, int position, @NonNull CatalogImageClass model) {
                try {
                    holder.progressBar.setVisibility(View.VISIBLE);
                    Picasso.get().load(model.getCatalogImage()).into(holder.catalogImage,
                            new Callback() {
                                @Override
                                public void onSuccess() {
                                    holder.progressBar.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError(Exception e) {
                                    holder.progressBar.setVisibility(View.GONE);
                                }
                            });
                } catch (Exception e) {

                }

                final ImagePopup imagePopup = new ImagePopup(ShopDetailsActivity.this);
                imagePopup.setFullScreen(true); // Optional
                imagePopup.setHideCloseIcon(true);  // Optional
                imagePopup.setImageOnClickClose(true);  // Optional
                imagePopup.setBackgroundColor(getColor(R.color.transparent));

                imagePopup.initiatePopupWithPicasso(model.getCatalogImage()); // Load Image from Drawable

                holder.catalogImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /** Initiate Popup view **/
                        imagePopup.viewPopup();

                    }
                });


            }
        };
        LinearLayoutManager layoutManager = new LinearLayoutManager(ShopDetailsActivity.this, LinearLayoutManager.HORIZONTAL, false);
        catalogRecyclerView.setLayoutManager(layoutManager);
        catalogRecyclerView.setAdapter(firestoreCatalogRecyclerAdapter);
    }

    private class CatalogViewHolder extends RecyclerView.ViewHolder {
        private ImageView catalogImage;
        private ProgressBar progressBar;

        public CatalogViewHolder(@NonNull View itemView) {
            super(itemView);
            catalogImage = itemView.findViewById(R.id.catalog_recycler_image);
            progressBar = itemView.findViewById(R.id.catalog_image_progress_bar);
        }
    }


    private void addAndDeleteBookmark(String shopID) {
        try {
            //creating database reference
            BookmarkClass bookmarkClass = new BookmarkClass();
            bookmarkClass.setShopID(shopProfileClass.getShopID());
            DocumentReference saveRef = firestore.collection("Users").document(mAuth.getCurrentUser().getUid())
                    .collection("Bookmarks").document(shopProfileClass.getShopID());

            //checking if selected id is already saved or not
            saveRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        if (documentSnapshot.get("shopID") != null) {
                            //means selected id is already saved
                            saveRef.delete();
                            Log.d(TAG, "onComplete: deleteed");
                            ShopSavedSnackbar("Bookmark Removed");
                        } else {
                            //it means user does not exist so save it
                            saveRef.set(bookmarkClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "onComplete: exist and added");
                                        ShopSavedSnackbar("Bookmark Removed");
                                    }
                                }
                            });
                        }
                        setupSaveButton(shopID);
                    } else {
                        saveRef.set(bookmarkClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: added");
                                    ShopSavedSnackbar("Bookmark Added");
                                }
                            }
                        });
                        setupSaveButton(shopID);
                    }


                }
            });
        } catch (Exception e) {

        }
    }

    private void setupSaveButton(String shopID) {
        //button selected and unselected states

        bookmarkClassList.clear();
        databaseClass.gettingUserBookmarksDataFromDatabaseWithCallbacks(new FirestoreCallback() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void dataGetComplete() {

                bookmarkClassList = databaseClass.gettingUserBookmarksDataFromDevice();

                if (bookmarkClassList.isEmpty()) {
                    //  holder.favouriteButton.setImageDrawable(bookmarkUnselected);
                    bookmarkButton.setStrokeColor(ColorStateList.valueOf(getColor(R.color.light_gray)));
                    bookmarkButton.setText("Bookmark");
                    bookmarkButton.setBackgroundColor(getColor(R.color.white));
                    bookmarkButton.setTextColor(getColor(R.color.gray_icon));
                } else {
                    for (int i = 0; i < bookmarkClassList.size(); i++) {
                        if (shopID.equals(bookmarkClassList.get(i).getShopID())) {
                            //means shop is bookmarked
                            bookmarkButton.setStrokeColor(ColorStateList.valueOf(getColor(R.color.sky_blue)));
                            bookmarkButton.setText("Bookmarked");
                            bookmarkButton.setTextColor(getColor(R.color.sky_blue));
                            bookmarkButton.setBackgroundColor(getColor(R.color.sky_blue_faint));
                            break;
                        } else {
                            //shop is not bookmarked
                            bookmarkButton.setStrokeColor(ColorStateList.valueOf(getColor(R.color.light_gray)));
                            bookmarkButton.setText("Bookmark");
                            bookmarkButton.setTextColor(getColor(R.color.gray_icon));
                            bookmarkButton.setBackgroundColor(getColor(R.color.white));
                        }
                    }
                }

                progressBar.setVisibility(View.GONE);
                mainContentLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void callShop() {
        if (checkPermissions(
                Manifest.permission.CALL_PHONE,
                CALL_PERMISSION_CODE)) {
            Log.d(TAG, "callShop: 1");
            if (ContextCompat.checkSelfPermission(ShopDetailsActivity.this,
                    Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {

                // Permission is  granted
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
            } else if (ContextCompat.checkSelfPermission(ShopDetailsActivity.this,
                    Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_DENIED) {
                checkPermissions(
                        Manifest.permission.CALL_PHONE,
                        CALL_PERMISSION_CODE);
                Log.d(TAG, "callShop: 3");
            }

        }
    }

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
                callShop();
            } else {

            }
        }

    }

    private boolean checkPermissions(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(ShopDetailsActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(ShopDetailsActivity.this,
                    new String[]{permission},
                    requestCode);
            return false;
        } else {
            return true;
        }
    }

    private void ShopSavedSnackbar(String msg) {
        Snackbar snackbar = Snackbar.make(relativeLayout, msg, Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addAndDeleteBookmark(shopProfileClass.getShopID());
                    }
                });
        snackbar.show();
    }

    private void sendUserToMoreDetailsActivity() {
        Intent moreIntent = new Intent(this, ShopMoreInfoActivity.class);
        moreIntent.putExtra("shopProfile", shopProfileClass);
        moreIntent.putExtra("ratings", ratingBar.getRating());
        moreIntent.putExtra("reviews", ratingClassList.size());
        startActivity(moreIntent);
    }
}