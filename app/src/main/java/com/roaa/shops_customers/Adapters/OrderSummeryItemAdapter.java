package com.roaa.shops_customers.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.roaa.shops_customers.ModelClasses.ProductOrdersClass;
import com.roaa.shops_customers.R;

import java.util.ArrayList;
import java.util.List;

public class OrderSummeryItemAdapter extends RecyclerView.Adapter<OrderSummeryItemAdapter.ViewHolder> {

    private List<ProductOrdersClass> ProductsList = new ArrayList<>();
    private Context mContext;


    public OrderSummeryItemAdapter(List<ProductOrdersClass> ProductsList, Context mContext) {
        this.mContext = mContext;
        this.ProductsList = ProductsList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.cart_order_item_recycler_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.productName.setText(ProductsList.get(position).getProductName());
        holder.productPrice.setText("Rs " + ProductsList.get(position).getProductPrice().trim());
        holder.productQuantity.setText(ProductsList.get(position).getProductQuantity().trim());
        double TotalPrice = Double.parseDouble(ProductsList.get(position).getProductPrice().trim()) *
                Integer.parseInt(ProductsList.get(position).getProductQuantity().trim());

        holder.productTotalPrice.setText("Rs " + String.format("%.2f", TotalPrice));
        if (ProductsList.get(position).getAvailable()) {
            holder.productStatusImage.setImageDrawable(mContext.getDrawable(R.drawable.ic_available_icon));
        } else {
            holder.productStatusImage.setImageDrawable(mContext.getDrawable(R.drawable.ic_not_availble_icon));
        }
    }

    @Override
    public int getItemCount() {
        return ProductsList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView productName, productPrice, productQuantity, productTotalPrice;
        private ImageView productStatusImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.cart_order_item_recycler_product_name);
            productPrice = itemView.findViewById(R.id.cart_order_item_recycler_single_product_price);
            productQuantity = itemView.findViewById(R.id.cart_order_item_recycler_product_quantity);
            productTotalPrice = itemView.findViewById(R.id.cart_order_item_recycler_product_total_price);
            productStatusImage = itemView.findViewById(R.id.product_status_icon);
        }
    }
}
