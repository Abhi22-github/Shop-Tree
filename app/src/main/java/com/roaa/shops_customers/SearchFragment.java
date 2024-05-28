package com.roaa.shops_customers;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import com.roaa.shops_customers.Adapters.ShopsRegularAdapterSearch;
import com.roaa.shops_customers.ModelClasses.ShopProfileClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class SearchFragment extends Fragment {
    private static final String TAG = "SearchFragment";

    //layout widgets
    private TextInputEditText searchText;
    private RecyclerView recyclerView;
    private ImageButton backButton;

    //vars
    private View view;
    private List<ShopProfileClass> shopProfileClassList = new ArrayList<>();
    private ShopsRegularAdapterSearch adapter;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setSharedElementEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
        view = inflater.inflate(R.layout.fragment_search, container, false);
        initFields();
        searchText.requestFocus();
        showKeyboard();
        initFirebase();
        getActivity().findViewById(R.id.main_activity_bottom_navigation_fragment_holder).setVisibility(View.GONE);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        if(searchText.getText().toString().isEmpty()){
            recyclerView.setVisibility(View.GONE);
        }else {
            recyclerView.setVisibility(View.VISIBLE);
        }

        setUpRecyclerView();
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()>0) {
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.getFilter().filter(s);
                }else {
                    recyclerView.setVisibility(View.GONE);
                    adapter.getFilter().filter(s);
                }
            }
        });

        return view;

    }

    private void initFields() {
        searchText = view.findViewById(R.id.search_fragment_edit_text_search);
        recyclerView = view.findViewById(R.id.search_fragment_recycler_view_search_recycler);
        backButton = view.findViewById(R.id.search_fragment_image_button_back_button);

    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    @Override
    public void onStop() {
        super.onStop();
        //closeKeyboard();
        getActivity().findViewById(R.id.main_activity_bottom_navigation_fragment_holder).setVisibility(View.VISIBLE);
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
                    ShopProfileClass shopProfileClass = document.toObject(ShopProfileClass.class);
                    shopProfileClassList.add(shopProfileClass);

                }
                adapter = new ShopsRegularAdapterSearch(shopProfileClassList, getContext());
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(adapter);
            }
        });

    }

}