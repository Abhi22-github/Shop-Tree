package com.roaa.shops_customers.BottomNavigation;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.roaa.shops_customers.Adapters.OrderItemAdapter;
import com.roaa.shops_customers.ModelClasses.OrderLink;
import com.roaa.shops_customers.ModelClasses.OrderProgressDetails;
import com.roaa.shops_customers.Other.DatabaseClass;
import com.roaa.shops_customers.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class OrderFragment extends Fragment {
    private static final String TAG = "OrderFragment";

    //layout widgets
    private RecyclerView recyclerView;
    private ImageView noOrderIllustrations;
    private LinearLayout illustrationMsg;
    private ProgressBar progressBar;

    //vars
    private View view;
    private List<OrderLink> orderLinkList = new ArrayList<>();
    private List<OrderProgressDetails> orderProgressDetailsList = new ArrayList<>();
    private List<OrderProgressDetails> orderProgressDetailsListALL = new ArrayList<>();
    private OrderItemAdapter orderItemAdapter;
    private DatabaseClass databaseClass;

    //firebase
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    public OrderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_order, container, false);

        initFields();

        initFirebase();


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        showProgressBar();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                gettingOrderLinkFromDatabase();
            }
        });

    }

    private void initFields() {
        recyclerView = view.findViewById(R.id.order_fragment_recycler_view_order_list);
        noOrderIllustrations = view.findViewById(R.id.no_order_illustrations);
        databaseClass = new DatabaseClass(getContext());
        illustrationMsg = view.findViewById(R.id.illustration_message);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");
        recyclerView.setAdapter(null);
        orderItemAdapter = null;
        orderItemAdapter = null;
    }

    private void gettingOrderLinkFromDatabase() {
        rootRef.child("User Orders Details Link").child(mAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderLinkList.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                OrderLink orderLink = postSnapshot.getValue(OrderLink.class);
                                orderLinkList.add(orderLink);
                            }
                            //quering data
                            Query query = rootRef.child("Orders Details");
                            query.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    orderProgressDetailsList.clear();
                                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                        for (OrderLink orderLink : orderLinkList) {
                                            if (orderLink.getOrderDetailID().equals(postSnapshot.getKey())) {
                                                OrderProgressDetails orderProgressDetails = postSnapshot.getValue(OrderProgressDetails.class);
                                                orderProgressDetailsList.add(orderProgressDetails);
                                            }
                                        }
                                    }
                                    if (orderProgressDetailsList.size() != orderProgressDetailsListALL.size()) {
                                        Collections.sort(orderProgressDetailsList);
                                        setUpOrderRecyclerView();
                                        orderProgressDetailsListALL = orderProgressDetailsList;
                                    } else {
                                        //do nothing
                                    }

                                    if (orderItemAdapter == null) {
                                        Collections.sort(orderProgressDetailsList);
                                        setUpOrderRecyclerView();
                                        orderProgressDetailsListALL = orderProgressDetailsList;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                        hideProgressBar();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        hideProgressBar();
                    }
                });
    }

    private void setUpOrderRecyclerView() {
        //original adapter
        Collections.sort(orderProgressDetailsList);
        Collections.reverse(orderProgressDetailsList);
        Collections.sort(orderLinkList);
        Collections.reverse(orderLinkList);
        Log.d(TAG, "setUpOrderRecyclerView: order progress list" + orderProgressDetailsList);
        orderItemAdapter = new OrderItemAdapter(orderLinkList, orderProgressDetailsList, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(orderItemAdapter);

    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        illustrationMsg.setVisibility(View.GONE);
        noOrderIllustrations.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        if (orderLinkList.isEmpty()) {
            noOrderIllustrations.setVisibility(View.VISIBLE);
            illustrationMsg.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noOrderIllustrations.setVisibility(View.GONE);
            illustrationMsg.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

}