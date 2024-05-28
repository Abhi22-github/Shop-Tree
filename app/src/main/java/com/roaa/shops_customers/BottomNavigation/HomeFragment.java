package com.roaa.shops_customers.BottomNavigation;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.roaa.shops_customers.Adapters.ShopsRegularAdapterHome;
import com.roaa.shops_customers.MainActivity;
import com.roaa.shops_customers.ModelClasses.BookmarkClass;
import com.roaa.shops_customers.ModelClasses.LocationClass;
import com.roaa.shops_customers.ModelClasses.OfferClass;
import com.roaa.shops_customers.ModelClasses.ShopProfileClass;
import com.roaa.shops_customers.Other.DatabaseClass;
import com.roaa.shops_customers.Other.FirestoreCallback;
import com.roaa.shops_customers.R;
import com.roaa.shops_customers.SearchFragment;
import com.roaa.shops_customers.ShopDetailsActivity;
import com.roaa.shops_customers.UserLocationActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    //layout widgets
    private RecyclerView recyclerView, fireStoreRecyclerView;
    // private RelativeLayout searchView;
    // private EditText searchViewText;

    private TextView addressText;
    private ImageButton searchButton, notificationButton, myLocationButton;
    private RelativeLayout noNearbyShopsLayout;
    private LinearLayout nearbyShopsLayout;
    private LinearLayout messageLayout;
    private TextView invite;
    //private TextView allShopText;
    //private View line;

    //vars
    private View view;
    private DatabaseClass databaseClass;
    private List<BookmarkClass> bookmarkClassList = new ArrayList<>();
    private List<String> nearbyShopIDList = new ArrayList<>();
    private List<ShopProfileClass> shopProfileClassList = new ArrayList<>();
    private List<ShopProfileClass> shopProfileClassGlobal = new ArrayList<>();
    private String address = "";
    private List<String> shopIDList = new ArrayList();
    private List<ShopProfileClass> shopProfileClassListAll = new ArrayList<>();
    private ShopsRegularAdapterHome adapter;
    private FirestoreCallback firestoreCallback;


    //firebase.
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);

        initFields();

        initFirebase();
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        //.addSharedElement(searchView, "search")
                        .replace(R.id.main_activity_frame_layout_fragment_container, new SearchFragment())
                        .addToBackStack(TAG)
                        .commit();
            }
        });

        setUpAddressText();
        Log.d(TAG, "onCreateView: ");

        myLocationButton.setOnClickListener(v -> sendUserToUserLocationActivity());

        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "TreeShop Seller");
                String shareMessage = "\nBuy things from your area at lowest price and save money\n\n";
                shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + "com.roaa.shops_seller" + "\n\n";
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "choose one"));
            }
        });

        return view;
    }

    private void setUpAddressText() {
        address = databaseClass.getAddressFromDevice();
        addressText.setText(address);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        try {
            nearbyShopIDList.clear();
            nearbyShopIDList = databaseClass.gettingNearbyStoreFromDevice();
        } catch (Exception e) {

        }
        setUpRecyclerView();
        checkForAddressChange();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");
        recyclerView.setAdapter(null);
        adapter = null;
        recyclerView = null;
    }

    private void checkForAddressChange() {
        try {
            if (databaseClass.getAddressFromDevice().equals(address)) {
                //do nothing
            } else {
                setUpAddressText();
            }
        } catch (Exception e) {
            setUpAddressText();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (shopProfileClassList.isEmpty()) {
            firestoreRecyclerAdapter.stopListening();
        }
    }

    private void initFields() {
        recyclerView = view.findViewById(R.id.home_recycler_view);
        databaseClass = new DatabaseClass(getContext());
        // searchView = view.findViewById(R.id.search_view_layout);
        //  searchViewText = view.findViewById(R.id.home_fragment_edit_text_search);
        // selectDeviceLocation = view.findViewById(R.id.home_fragment_relative_layout_select_location);
        addressText = view.findViewById(R.id.home_fragment_text_view_address);
        searchButton = view.findViewById(R.id.search);
        //notificationButton = view.findViewById(R.id.notification);
        fireStoreRecyclerView = view.findViewById(R.id.home_firestore_recycler_view);
        //allShopText = view.findViewById(R.id.all_shop_text);
        // line = view.findViewById(R.id.all_shop_line);
        noNearbyShopsLayout = view.findViewById(R.id.no_nearby_shops_illustrations);
        myLocationButton = view.findViewById(R.id.my_location);
        nearbyShopsLayout = view.findViewById(R.id.home_fragment_text_view_shops_around_text);
        messageLayout = view.findViewById(R.id.message_layout);
        invite = view.findViewById(R.id.invite);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    private void setUpRecyclerView() {
        //Query
        Query query = firestore.collection("Shops");
        //Recycler options

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                shopProfileClassList.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    try {
                        if (!nearbyShopIDList.isEmpty()) {
                            for (String shopID : nearbyShopIDList) {
                                ShopProfileClass shopProfileClass = document.toObject(ShopProfileClass.class);
                                Log.d(TAG, "onComplete: shopID" + shopID);
                                Log.d(TAG, "onComplete: shop profile class shop Id " + shopProfileClass.getShopID());
                                if (shopID.equals(shopProfileClass.getShopID())) {
                                    shopProfileClassList.add(shopProfileClass);
                                    shopIDList.add(shopProfileClass.getShopID());
                                }
                            }
                        }
                    } catch (Exception e) {

                    }
                }

                if (shopProfileClassList.isEmpty()) {
                    nearbyShopsLayout.setVisibility(View.GONE);

                } else {
                    nearbyShopsLayout.setVisibility(View.VISIBLE);
                }


                Log.d(TAG, "onComplete: shop profile class list " + shopProfileClassList.size());
                Log.d(TAG, "onComplete: shop profile class list all " + shopProfileClassListAll.size());

                if (shopProfileClassList.size() != shopProfileClassListAll.size()) {
                    adapter = new ShopsRegularAdapterHome(shopProfileClassList, getContext());
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(adapter);
                    shopProfileClassListAll = shopProfileClassList;
                } else {
                    //do nothing
                }
                if (adapter == null) {
                    adapter = new ShopsRegularAdapterHome(shopProfileClassList, getContext());
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(adapter);
                    shopProfileClassListAll = shopProfileClassList;
                }

                if (shopProfileClassList.isEmpty()) {
                    messageLayout.setVisibility(View.VISIBLE);
                    setUpFirebaseRecyclerView();
                    firestoreRecyclerAdapter.startListening();
                } else {
                    messageLayout.setVisibility(View.GONE);
                }
                ((MainActivity) getActivity()).dismissLoadingBar();
            }
        });

    }

    /*
        private void addAndDeleteBookmark(ShopViewHolder holder, ShopProfileClass model) {
            try {
                //creating database reference
                BookmarkClass bookmarkClass = new BookmarkClass();
                bookmarkClass.setShopID(model.getShopID());
                DocumentReference saveRef = firestore.collection("Users").document(mAuth.getCurrentUser().getUid())
                        .collection("Bookmarks").document(model.getShopID());

                //checking if selected id is already saved or not
                saveRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.get("shopID") != null) {
                                //means selected id is already saved
                                saveRef.delete();
                                Log.d(TAG, "onComplete: new Id has been remove");
                                ///ShopSavedSnackbar("Removed From Saved");
                            } else {
                                //it means user does not exist so save it
                                saveRef.set(bookmarkClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "onComplete: new id has been saved");
                                            //ShopSavedSnackbar("Added to Saved");
                                        }
                                    }
                                });
                            }
                            setupSaveButton(holder, model);
                        } else {
                            saveRef.set(bookmarkClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        Log.d(TAG, "onComplete: new id has been saved");
                                        //ShopSavedSnackbar("Added to Saved");
                                    }
                                }
                            });
                            setupSaveButton(holder, model);
                        }


                    }
                });
            } catch (Exception e) {

            }
        }

        private void setupSaveButton(ShopViewHolder holder, ShopProfileClass model) {
            //button selected and unselected states
            Drawable bookmarkSelected = getActivity().getDrawable(R.drawable.ic_baseline_bookmark_filled);
            Drawable bookmarkUnselected = getActivity().getDrawable(R.drawable.ic_outline_bookmark_border);

            databaseClass.gettingUserBookmarksDataFromDatabaseWithCallbacks(new FirestoreCallback() {
                @Override
                public void dataGetComplete() {

                    bookmarkClassList = databaseClass.gettingUserBookmarksDataFromDevice();

                    if (bookmarkClassList.isEmpty()) {
                        holder.bookmarkButton.setImageDrawable(bookmarkUnselected);
                        holder.bookmarkButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                    } else {

                        for (BookmarkClass bookmarkClass : bookmarkClassList) {
                            Log.d(TAG, "setupSaveButton: id---" + bookmarkClass.getShopID());
                            if (bookmarkClass.getShopID().equals(model.getShopID())) {
                                Log.d(TAG, "dataGetComplete: 1"+bookmarkClass.getShopID());
                                Log.d(TAG, "dataGetComplete: 2"+model.getShopID());
                                Log.d(TAG, "setupSaveButton: equals");
                                //means shop is bookmarked
                                holder.bookmarkButton.setImageDrawable(bookmarkSelected);
                                holder.bookmarkButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.sky_blue), android.graphics.PorterDuff.Mode.SRC_IN);

                            } else {
                                //shop is not bookmarked
                                holder.bookmarkButton.setImageDrawable(bookmarkUnselected);
                                holder.bookmarkButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                            }
                        }
                    }


                }
            });


        }*/
    private void sendUserToUserLocationActivity() {
        Intent userLocationIntent = new Intent(getContext(), UserLocationActivity.class);
        getActivity().startActivity(userLocationIntent);
    }

    private void setUpFirebaseRecyclerView() {
        //Query Firestore
        Query query = firestore.collection("Shops");

        //Recycler options
        FirestoreRecyclerOptions<ShopProfileClass> options = new FirestoreRecyclerOptions.Builder<ShopProfileClass>()
                .setQuery(query, ShopProfileClass.class)
                .build();

        final int count = options.getSnapshots().size();


        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<ShopProfileClass, ShopViewHolder>(options) {
            @NonNull
            @Override
            public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_single_shop_layout, parent, false);
                return new ShopViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ShopViewHolder holder, int position, @NonNull ShopProfileClass model) {

                String shopID = model.getShopID();
                holder.shopName.setText(model.getShopName());
                holder.shopAddress.setText(model.getShopAddress());
                List<OfferClass> list = new ArrayList<>();
              /*  CollectionReference collectionReference = firestore.collection("Shops").document(shopProfileClassList.get(position).getShopID())
                        .collection("Offers");
                */

                setupSaveButton(holder, model.getShopID());

                holder.favouriteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //add and delete Bookmarks
                        addAndDeleteBookmark(holder, model.getShopID());
                    }
                });
                try {

                    Picasso.get().load(shopProfileClassList.get(position).getShopImage()).into(holder.shopImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.imageProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                } catch (Exception e) {

                }

                holder.singleShopContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singleShopDetailsIntent = new Intent(getContext(), ShopDetailsActivity.class);
                        singleShopDetailsIntent.putExtra("shopDetails", shopProfileClassList.get(position));
                        //creating bundle to transfer shop offer list
                        Bundle data = new Bundle();
                        data.putSerializable("offerList", (Serializable) list);
                        singleShopDetailsIntent.putExtra("shopOffers", data);
                        getContext().startActivity(singleShopDetailsIntent);
                    }
                });

                holder.directionButton.setOnClickListener(v -> displayDirection(model.getShopID()));
            }

            private void ShopSavedSnackbar(String msg, ShopViewHolder holder, String shopid) {

                Snackbar snackbar = Snackbar.make(holder.relativeLayout, msg, Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                addAndDeleteBookmark(holder, shopid);
                            }
                        });
                View snackBarView = snackbar.getView();
                snackBarView.setTranslationY(-(convertDpToPixel(60, getActivity())));
                snackbar.show();
            }

            private void displayDirection(String shopID) {
                firestore.collection("Shop Locations").document(shopID).get()
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
                                        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                                            getContext().startActivity(intent);
                                        } else {
                                            Toast.makeText(getContext(), "Please install maps", Toast.LENGTH_SHORT).show();
                                        }

                                    }

                                }
                            }
                        });
            }


            public float convertDpToPixel(float dp, Context context) {
                return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
            }

            public float convertPixelsToDp(float px, Context context) {
                return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
            }

            private void addAndDeleteBookmark(ShopViewHolder holder, String shopID) {
                try {
                    //creating database reference
                    BookmarkClass bookmarkClass = new BookmarkClass();
                    bookmarkClass.setShopID(shopID);
                    DocumentReference saveRef = firestore.collection("Users").document(mAuth.getCurrentUser().getUid())
                            .collection("Bookmarks").document(shopID);

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
                                    ShopSavedSnackbar("Bookmark Removed", holder, shopID);
                                } else {
                                    //it means user does not exist so save it
                                    saveRef.set(bookmarkClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "onComplete: exist and added");
                                                ShopSavedSnackbar("Bookmark Added", holder, shopID);
                                            }
                                        }
                                    });
                                }
                                setupSaveButton(holder, shopID);
                            } else {
                                saveRef.set(bookmarkClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "onComplete: added");
                                            ShopSavedSnackbar("Bookmark Added", holder, shopID);
                                        }
                                    }
                                });
                                setupSaveButton(holder, shopID);
                            }


                        }
                    });
                } catch (Exception e) {

                }
            }

            private void setupSaveButton(ShopViewHolder holder, String shopID) {
                //button selected and unselected states

                bookmarkClassList.clear();
                databaseClass.gettingUserBookmarksDataFromDatabaseWithCallbacks(new FirestoreCallback() {
                    @Override
                    public void dataGetComplete() {

                        bookmarkClassList = databaseClass.gettingUserBookmarksDataFromDevice();


                        if (bookmarkClassList.isEmpty()) {
                            //  holder.favouriteButton.setImageDrawable(bookmarkUnselected);

                        } else {

                            for (int i = 0; i < bookmarkClassList.size(); i++) {
                                if (shopID.equals(bookmarkClassList.get(i).getShopID())) {
                                    //means shop is bookmarked
                                    //   holder.favouriteButton.setImageDrawable(bookmarkSelected);
                                    holder.favouriteButton.setChecked(true);
                                    break;
                                } else {
                                    //shop is not bookmarked
                                    holder.favouriteButton.setChecked(false);
                                    //  holder.favouriteButton.setImageDrawable(bookmarkUnselected);
                                }
                            }
                        }


                    }
                });


            }

        };

        fireStoreRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fireStoreRecyclerView.setAdapter(firestoreRecyclerAdapter);
    }

    private class ShopViewHolder extends RecyclerView.ViewHolder {
        private ImageView shopImage;
        private TextView shopName, shopAddress;
        private CheckBox favouriteButton;
        //private LinearLayout offerLinearLayout;
        private ProgressBar imageProgressBar;
        private LinearLayout singleShopContainer;
        private ImageButton directionButton;
        private RelativeLayout relativeLayout;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            shopImage = itemView.findViewById(R.id.recycler_view_single_shop_image_view_shop_image);
            shopName = itemView.findViewById(R.id.single_shop_recycler_view_text_view_shop_name);
            shopAddress = itemView.findViewById(R.id.single_shop_recycler_view_text_view_shop_address);
            favouriteButton = itemView.findViewById(R.id.single_shop_recycler_view_image_button_shop_bookmark);
            // offerLinearLayout = itemView.findViewById(R.id.single_shop_recycler_view_linear_layout_offer_layout);
            imageProgressBar = itemView.findViewById(R.id.single_shop_recycler_view_progress_bar_image_progress);
            singleShopContainer = itemView.findViewById(R.id.recycler_view_single_shop_linear_layout_single_item_container);
            directionButton = itemView.findViewById(R.id.single_shop_recycler_view_image_button_shop_direction);
            relativeLayout = itemView.findViewById(R.id.rel_layout);
        }
    }

}