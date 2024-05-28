package com.roaa.shops_customers.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.roaa.shops_customers.ModelClasses.CategoryClass;
import com.roaa.shops_customers.ModelClasses.ProductClass;
import com.roaa.shops_customers.ModelClasses.ShopProfileClass;
import com.roaa.shops_customers.Other.FirestoreCallback;
import com.roaa.shops_customers.Other.ProductsCallback;
import com.roaa.shops_customers.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class GroupItemAdapter extends RecyclerView.Adapter<GroupItemAdapter.ViewHolder> {

    private List<ProductClass> productClassList = new ArrayList<>();
    private Context mContext;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private CategoryClass model;
    private ProductsCallback productsCallback;
    private ShopProfileClass shopProfileClass;

    public GroupItemAdapter(CategoryClass model, ShopProfileClass shopProfileClass, Context mContext) {
        this.mContext = mContext;
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        this.model = model;
        this.productsCallback = ((ProductsCallback) mContext);
        this.shopProfileClass = shopProfileClass;
    }

    public void getProductData(FirestoreCallback firestoreCallback) {
        Query query = firestore.collection("Products").whereEqualTo("shopID", shopProfileClass.getShopID())
                .whereEqualTo("productCategory", model.getCategoryName());
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange doc : value.getDocumentChanges()) {
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        ProductClass productClass = doc.getDocument().toObject(ProductClass.class);
                        productClassList.add(productClass);
                        firestoreCallback.dataGetComplete();
                    }
                }
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.product_group_item_recycler_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int[] SINGLE_SHOP_QUANTITY = {1};
        holder.productName.setText(productClassList.get(position).getProductName());
        holder.productPrice.setText("Rs " + productClassList.get(position).getProductPrice().trim());
        try {
            if (productClassList.get(position).getProductImage() == null) {
                holder.imageLayout.setVisibility(View.GONE);
            } else {
                holder.imageProgressBar.setVisibility(View.VISIBLE);
                Picasso.get().load(productClassList.get(position).getProductImage()).into(holder.productImage,
                        new Callback() {
                            @Override
                            public void onSuccess() {
                                holder.imageProgressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(Exception e) {
                                holder.imageProgressBar.setVisibility(View.GONE);
                            }
                        });
            }
        } catch (Exception e) {

        }
        try {
            if (productClassList.get(position).getProductBrand().isEmpty()) {
                holder.productBrand.setVisibility(View.GONE);
            } else {
                holder.productBrand.setVisibility(View.VISIBLE);
                holder.productBrand.setText("Brand : " + productClassList.get(position).getProductBrand().trim());
            }

        } catch (Exception e) {
            holder.productBrand.setVisibility(View.GONE);
        }

        if (productClassList.get(position).getAvailable()) {
            holder.productStatusImage.setImageDrawable(mContext.getDrawable(R.drawable.ic_available_icon));
        } else {
            holder.productStatusImage.setImageDrawable(mContext.getDrawable(R.drawable.ic_not_availble_icon));
        }

        holder.addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.addToCartButton.setVisibility(View.INVISIBLE);
                holder.numberPickerLayout.setVisibility(View.VISIBLE);
                productsCallback.sendProductAndQuantity(productClassList.get(position), 1, 0);
            }
        });

        holder.plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SINGLE_SHOP_QUANTITY[0]++;
                holder.quantityText.setText(String.valueOf(SINGLE_SHOP_QUANTITY[0]));
                productsCallback.sendProductAndQuantity(productClassList.get(position), 1, 1);
            }
        });
        holder.minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SINGLE_SHOP_QUANTITY[0]--;
                holder.quantityText.setText(String.valueOf(SINGLE_SHOP_QUANTITY[0]));
                productsCallback.sendProductAndQuantity(productClassList.get(position), 1, 2);
                if (SINGLE_SHOP_QUANTITY[0] == 0) {
                    holder.numberPickerLayout.setVisibility(View.INVISIBLE);
                    holder.addToCartButton.setVisibility(View.VISIBLE);
                    SINGLE_SHOP_QUANTITY[0] = 1;
                    holder.quantityText.setText(String.valueOf(SINGLE_SHOP_QUANTITY[0]));
                    productsCallback.sendProductAndQuantity(productClassList.get(position), 0, 3);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return productClassList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView productImage;
        private TextView productName;
        private TextView productBrand;
        private TextView productPrice;
        private ProgressBar imageProgressBar;
        private MaterialButton addToCartButton;
        private ConstraintLayout numberPickerLayout;
        private TextView plusButton;
        private TextView minusButton;
        private EditText quantityText;
        private RelativeLayout imageLayout;
        private ImageView productStatusImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.group_item_recycler_view_image_view_product_image);
            productName = itemView.findViewById(R.id.group_item_rv_product_name);
            productBrand = itemView.findViewById(R.id.group_item_rv_product_brand);
            productPrice = itemView.findViewById(R.id.group_item_rv_product_price);
            imageProgressBar = itemView.findViewById(R.id.group_item_recycler_view_image_progress_bar_progress);
            addToCartButton = itemView.findViewById(R.id.product_item_recycler_view_button_add_button);
            numberPickerLayout = itemView.findViewById(R.id.product_item_recycler_view_constraints_layout_number_picker);
            quantityText = itemView.findViewById(R.id.product_item_recycler_edit_text_quantity);
            plusButton = itemView.findViewById(R.id.product_item_recycler_view_button_plus_button);
            minusButton = itemView.findViewById(R.id.product_item_recycler_view_button_minus_button);
            imageLayout = itemView.findViewById(R.id.product_item_recycler_relative_layout_image_holder);
            productStatusImage = itemView.findViewById(R.id.product_status_icon);
        }
    }
}
