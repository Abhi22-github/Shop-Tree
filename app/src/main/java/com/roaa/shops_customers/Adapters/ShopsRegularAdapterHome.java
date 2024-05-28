package com.roaa.shops_customers.Adapters;

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
import androidx.recyclerview.widget.RecyclerView;

import com.roaa.shops_customers.ModelClasses.BookmarkClass;
import com.roaa.shops_customers.ModelClasses.LocationClass;
import com.roaa.shops_customers.ModelClasses.OfferClass;
import com.roaa.shops_customers.ModelClasses.ShopProfileClass;
import com.roaa.shops_customers.Other.DatabaseClass;
import com.roaa.shops_customers.Other.FirestoreCallback;
import com.roaa.shops_customers.Other.ProductsCallback;
import com.roaa.shops_customers.R;
import com.roaa.shops_customers.ShopDetailsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ShopsRegularAdapterHome extends RecyclerView.Adapter<ShopsRegularAdapterHome.ViewHolder> {
    private static final String TAG = "ShopsRegularAdapterHome";

    private List<ShopProfileClass> shopProfileClassList = new ArrayList<>();
    private Context mContext;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private ProductsCallback productsCallback;
    private List<BookmarkClass> bookmarkClassList = new ArrayList<>();
    private DatabaseClass databaseClass;


    public ShopsRegularAdapterHome(List<ShopProfileClass> shopProfileClassList, Context mContext) {
        this.mContext = mContext;
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        this.shopProfileClassList = shopProfileClassList;
        databaseClass = new DatabaseClass(mContext);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.recycler_view_single_shop_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String shopID = shopProfileClassList.get(position).getShopID();
        holder.shopName.setText(shopProfileClassList.get(position).getShopName());
        holder.shopAddress.setText(shopProfileClassList.get(position).getShopAddress());
       // categoryList.add("Brand Speciality Store");
     //   categoryList.add("Dairy Store");
      //  categoryList.add("Daily Essentials");
      //  categoryList.add("Chemist & Medicals");
       // categoryList.add("Fruits & Vegetable");
       // categoryList.add("Grocery & General Store");
      //  categoryList.add("Meat, Egg & Fish");
       // categoryList.add("Mobile Accessories and Electronics");
      //  categoryList.add("Organic Shop");
       // categoryList.add("Stationary & Gifts");
       // categoryList.add("SuperMarket");
        //categoryList.add("Sweets, Bakery & Dry Fruits");

        holder.shopCategory.setText(shopProfileClassList.get(position).getShopCategory());
        List<OfferClass> list = new ArrayList<>();
        Log.d(TAG, "onBindViewHolder: ****************************************");
        CollectionReference collectionReference = firestore.collection("Shops").document(shopProfileClassList.get(position).getShopID())
                .collection("Offers");

     /*   try {

            collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot.isEmpty()) {
                            holder.offerLinearLayout.setVisibility(View.GONE);
                        } else {
                            holder.offerLinearLayout.setVisibility(View.VISIBLE);
                            List<OfferClass> types = querySnapshot.toObjects(OfferClass.class);
                            // Add all to your list
                            list.addAll(types);
                            holder.shopOffer.setText(list.get(0).getOfferText());
                        }
                    }
                }
            });
        } catch (Exception e) {

        }*/

        setupSaveButton(holder, position, shopID);

        holder.favouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add and delete Bookmarks
                addAndDeleteBookmark(holder, position, shopID);
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
                Intent singleShopDetailsIntent = new Intent(mContext, ShopDetailsActivity.class);
                singleShopDetailsIntent.putExtra("shopDetails", shopProfileClassList.get(position));
                //creating bundle to transfer shop offer list
                Bundle data = new Bundle();
                data.putSerializable("offerList", (Serializable) list);
                singleShopDetailsIntent.putExtra("shopOffers", data);
                mContext.startActivity(singleShopDetailsIntent);
            }
        });

        holder.directionButton.setOnClickListener(v -> displayDirection(position));

    }

    private void ShopSavedSnackbar(String msg, ViewHolder holder, int position, String shopID) {
        Snackbar snackbar = Snackbar.make(holder.relativeLayout, msg, Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addAndDeleteBookmark(holder, position, shopID);
                    }
                });
        View snackBarView = snackbar.getView();
        snackBarView.setTranslationY(-(convertDpToPixel(60, mContext)));
        snackbar.show();
    }

    public float convertDpToPixel(float dp, Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public float convertPixelsToDp(float px, Context context) {
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    private void displayDirection(int position) {
        firestore.collection("Shop Locations").document(shopProfileClassList.get(position).getShopID()).get()
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
                                if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                                    mContext.startActivity(intent);
                                } else {
                                    Toast.makeText(mContext, "Please install maps", Toast.LENGTH_SHORT).show();
                                }

                            }

                        }
                    }
                });
    }


    private void addAndDeleteBookmark(ViewHolder holder, int position, String shopID) {
        try {
            //creating database reference
            BookmarkClass bookmarkClass = new BookmarkClass();
            bookmarkClass.setShopID(shopProfileClassList.get(position).getShopID());
            DocumentReference saveRef = firestore.collection("Users").document(mAuth.getCurrentUser().getUid())
                    .collection("Bookmarks").document(shopProfileClassList.get(position).getShopID());

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
                            ShopSavedSnackbar("Bookmark Removed", holder, position, shopID);
                        } else {
                            //it means user does not exist so save it
                            saveRef.set(bookmarkClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "onComplete: exist and added");
                                        ShopSavedSnackbar("Bookmark Added", holder, position, shopID);
                                    }
                                }
                            });
                        }
                        setupSaveButton(holder, position, shopID);
                    } else {
                        saveRef.set(bookmarkClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: added");
                                    ShopSavedSnackbar("Bookmark Added", holder, position, shopID);
                                }
                            }
                        });
                        setupSaveButton(holder, position, shopID);
                    }


                }
            });
        } catch (Exception e) {

        }
    }

    private void setupSaveButton(ViewHolder holder, int position, String shopID) {
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


    @Override
    public int getItemCount() {
        return shopProfileClassList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView shopImage;
        private TextView shopName, shopAddress,shopCategory;
        private CheckBox favouriteButton;
        //private LinearLayout offerLinearLayout;
        private ProgressBar imageProgressBar;
        private LinearLayout singleShopContainer;
        private ImageButton directionButton;
        private RelativeLayout relativeLayout;


        public ViewHolder(@NonNull View itemView) {
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
            shopCategory = itemView.findViewById(R.id.single_shop_recycler_view_text_view_shop_category);
        }
    }
}
