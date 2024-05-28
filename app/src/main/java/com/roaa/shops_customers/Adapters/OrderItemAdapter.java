package com.roaa.shops_customers.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.roaa.shops_customers.ModelClasses.OrderLink;
import com.roaa.shops_customers.ModelClasses.OrderProgressDetails;
import com.roaa.shops_customers.ModelClasses.ShopProfileClass;
import com.roaa.shops_customers.OrderDetailsActivity;
import com.roaa.shops_customers.Other.DatabaseClass;
import com.roaa.shops_customers.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {
    private static final String TAG = "OrderItemAdapter";

    private List<OrderProgressDetails> orderProgressDetailsList = new ArrayList<>();
    private List<OrderLink> orderLinkList = new ArrayList<>();
    private Context mContext;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private DatabaseReference rootRef;
    private DatabaseClass databaseClass;


    public OrderItemAdapter(List<OrderLink> orderLinkList, List<OrderProgressDetails> orderProgressDetailsList, Context mContext) {
        this.orderLinkList = orderLinkList;
        this.mContext = mContext;
        this.orderProgressDetailsList = orderProgressDetailsList;
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        databaseClass = new DatabaseClass(mContext);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.user_orders_recycler_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.orderCode.setText(orderProgressDetailsList.get(position).getOrderCode());
        final ShopProfileClass[] shopProfileClass = new ShopProfileClass[1];
        firestore.collection("Shops").document(orderProgressDetailsList.get(position).getShopID())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    shopProfileClass[0] = documentSnapshot.toObject(ShopProfileClass.class);

                    holder.shopName.setText(shopProfileClass[0].getShopName());
                    holder.shopAddress.setText(shopProfileClass[0].getShopAddress());

                } else {
                    //exception
                }
            }
        });

        holder.orderTime.setText(parseTimeOnly(orderProgressDetailsList.get(position).getOrderCreatedTime()));

        switch (orderProgressDetailsList.get(position).getOrderStatus()) {
            case 1:
                holder.orderStatus.setText("Pending");
                holder.orderStatusIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_order_statues_circle_pending));
                break;
            case 2:
            case 3:
                holder.orderStatus.setText("Processing");
                holder.orderStatusIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_order_statues_circle_processing));
                break;
            case 4:
                holder.orderStatus.setText("Completed");
                holder.orderStatusIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_order_statues_circle_completed));
                break;
            case 5:
                holder.orderStatus.setText("Cancelled");
                holder.orderStatusIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_order_statues_circle_cancelled));
                break;

        }

        //  holder.orderItemCount.setText(String.valueOf(orderProgressDetailsList.get(position).getTotalItemCount()) + " Items");
        holder.grandTotal.setText(orderProgressDetailsList.get(position).getOrderTotal());
        holder.holderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent orderDetailsIntent = new Intent(mContext, OrderDetailsActivity.class);
                orderDetailsIntent.putExtra("shopDetails", shopProfileClass[0]);
                orderDetailsIntent.putExtra("orderProgressDetails", orderProgressDetailsList.get(position));
                orderDetailsIntent.putExtra("orderLinkList", orderLinkList.get(position));
                mContext.startActivity(orderDetailsIntent);
            }
        });


    }

    public String parseTimeOnly(String time) {
        String inputPattern = "yyyy/MM/dd HH:mm:ss";
        String outputPattern = "h:mm a";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return str;
    }

    @Override
    public int getItemCount() {
        return orderProgressDetailsList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView shopName, shopAddress, orderItemCount, grandTotal, orderStatus, pickupTime, orderCode, orderTime;
        private LinearLayout holderLayout;
        private ImageView orderStatusIcon;
        // private RelativeLayout relativeLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            shopName = itemView.findViewById(R.id.user_order_recycler_view_text_view_shop_name);
            shopAddress = itemView.findViewById(R.id.user_order_recycler_view_text_view_shop_address);
            // orderItemCount = itemView.findViewById(R.id.user_order_recycler_view_text_view_order_item_count);
            grandTotal = itemView.findViewById(R.id.user_order_recycler_view_text_view_order_grand_total);
            orderTime = itemView.findViewById(R.id.user_order_recycler_view_text_view_order_time);
            orderStatus = itemView.findViewById(R.id.user_order_recycler_view_text_view_order_status);
            // pickupTime = itemView.findViewById(R.id.user_order_recycler_view_text_view_pickup_time);
            orderCode = itemView.findViewById(R.id.user_order_recycler_view_text_view_order_code);
            holderLayout = itemView.findViewById(R.id.user_order_recycler_view_linear_layout_item_holder);
            //relativeLayout = itemView.findViewById(R.id.relativeLayout);
            orderStatusIcon = itemView.findViewById(R.id.order_status_icon);
        }
    }
}
