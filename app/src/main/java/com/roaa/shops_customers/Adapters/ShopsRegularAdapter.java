package com.roaa.shops_customers.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.roaa.shops_customers.ModelClasses.BookmarkClass;
import com.roaa.shops_customers.ModelClasses.OfferClass;
import com.roaa.shops_customers.ModelClasses.ShopProfileClass;
import com.roaa.shops_customers.Other.DatabaseClass;
import com.roaa.shops_customers.Other.FirestoreCallback;
import com.roaa.shops_customers.Other.ProductsCallback;
import com.roaa.shops_customers.R;
import com.roaa.shops_customers.ShopDetailsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ShopsRegularAdapter extends RecyclerView.Adapter<ShopsRegularAdapter.ViewHolder> {
    private static final String TAG = "ShopsRegularAdapter";

    private List<ShopProfileClass> shopProfileClassList = new ArrayList<>();
    private Context mContext;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private ProductsCallback productsCallback;
    private List<BookmarkClass> bookmarkClassList = new ArrayList<>();
    private DatabaseClass databaseClass;
    private NestedScrollView nestedScrollView;

    public ShopsRegularAdapter(List<ShopProfileClass> shopProfileClassList, Context mContext) {
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
        holder.shopCategory.setText(shopProfileClassList.get(position).getShopCategory());
        List<OfferClass> list = new ArrayList<>();
        CollectionReference collectionReference = firestore.collection("Shops").document(shopProfileClassList.get(position).getShopID())
                .collection("Offers");

   /*     try {

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
                deleteBookmark(holder, position, shopID);
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


    }

    private void deleteBookmark(ViewHolder holder, int position, String shopID) {
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
                            shopProfileClassList.remove(position);
                            notifyDataSetChanged();
                            //Log.d(TAG, "onComplete: new Id has been remove");
                            ///ShopSavedSnackbar("Removed From Saved");
                        }
                        // setupSaveButton(holder,position);
                    }

                }
            });
        } catch (Exception e) {

        }
    }

    private void setupSaveButton(ViewHolder holder, int position, String shopID) {
        //button selected and unselected states
        databaseClass.gettingUserBookmarksDataFromDatabaseWithCallbacks(new FirestoreCallback() {
            @Override
            public void dataGetComplete() {
                bookmarkClassList = databaseClass.gettingUserBookmarksDataFromDevice();
                if (bookmarkClassList.isEmpty()) {
                    holder.favouriteButton.setChecked(false);
                    // holder.favouriteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                } else {

                    for (BookmarkClass bookmarkClass : bookmarkClassList) {
                        Log.d(TAG, "dataGetComplete: id " + bookmarkClass.getShopID() + " position" + position);
                        if (shopID.equals(bookmarkClass.getShopID())) {
                            //means shop is bookmarked
                            holder.favouriteButton.setChecked(true);

                            break;
                        } else {
                            //shop is not bookmarked
                            holder.favouriteButton.setChecked(false);
                            /**
                             * VERY VERY VERY IMP DON'T FORGET OR CODE WILL CRASH
                             */

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


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            shopImage = itemView.findViewById(R.id.recycler_view_single_shop_image_view_shop_image);
            shopName = itemView.findViewById(R.id.single_shop_recycler_view_text_view_shop_name);
            shopAddress = itemView.findViewById(R.id.single_shop_recycler_view_text_view_shop_address);
            favouriteButton = itemView.findViewById(R.id.single_shop_recycler_view_image_button_shop_bookmark);
            // offerLinearLayout = itemView.findViewById(R.id.single_shop_recycler_view_linear_layout_offer_layout);
            imageProgressBar = itemView.findViewById(R.id.single_shop_recycler_view_progress_bar_image_progress);
            singleShopContainer = itemView.findViewById(R.id.recycler_view_single_shop_linear_layout_single_item_container);
            shopCategory = itemView.findViewById(R.id.single_shop_recycler_view_text_view_shop_category);
        }
    }

}
