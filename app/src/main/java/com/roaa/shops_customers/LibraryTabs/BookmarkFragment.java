package com.roaa.shops_customers.LibraryTabs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.roaa.shops_customers.Adapters.ShopsRegularAdapter;
import com.roaa.shops_customers.ModelClasses.BookmarkClass;
import com.roaa.shops_customers.ModelClasses.ShopProfileClass;
import com.roaa.shops_customers.Other.DatabaseClass;
import com.roaa.shops_customers.Other.FirestoreCallback;
import com.roaa.shops_customers.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class BookmarkFragment extends Fragment {
    private static final String TAG = "BookmarkFragment";

    //layout widgets
    private RecyclerView recyclerView;
    private ImageView noBookmarkImage;
    private LinearLayout illustrationMsg;
    private ProgressBar progressBar;

    //vars
    private View view;
    private DatabaseClass databaseClass;
    private List<BookmarkClass> bookmarkList = new ArrayList<>();
    private List<ShopProfileClass> shopProfileClassList = new ArrayList<>();
    private ImageButton backButton;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    public BookmarkFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_bookmark, container, false);
        getActivity().findViewById(R.id.main_activity_bottom_navigation_fragment_holder).setVisibility(View.GONE);

        initFields();

        initFirebase();
        backButton.setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        showProgressBar();
        setUpRecyclerView();
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().findViewById(R.id.main_activity_bottom_navigation_fragment_holder).setVisibility(View.VISIBLE);
    }

    private void initFields() {
        databaseClass = new DatabaseClass(getContext());
        recyclerView = view.findViewById(R.id.bookmark_fragment_recycler_view_bookmarks);
        databaseClass.gettingUserBookmarksDataFromDatabaseWithoutCallbacks();
        backButton = view.findViewById(R.id.bookmark_fragment_image_button_back_button);
        noBookmarkImage = view.findViewById(R.id.no_order_illustrations);
        illustrationMsg = view.findViewById(R.id.no_order_illustrations_msg);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    private void setUpRecyclerView() {
        databaseClass.gettingUserBookmarksDataFromDatabaseWithCallbacks(new FirestoreCallback() {
            @Override
            public void dataGetComplete() {
                bookmarkList.clear();
                bookmarkList = databaseClass.gettingUserBookmarksDataFromDevice();

                Query query = firestore.collection("Shops");


                query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        shopProfileClassList.clear();
                        for (DocumentChange doc : value.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                // get all ids
                                String postId = doc.getDocument().getId();
                                Log.d(TAG, "onEvent: post id " + postId);
                                for (int i = 0; i < bookmarkList.size(); i++) {
                                    if (bookmarkList.get(i).getShopID().equals(postId)) {
                                        ShopProfileClass shopProfileClass = doc.getDocument().toObject(ShopProfileClass.class);
                                        shopProfileClassList.add(shopProfileClass);
                                    }
                                }
                            }
                        }
                        Log.d(TAG, "onEvent: size " + shopProfileClassList.size());
                        ShopsRegularAdapter adapter = new ShopsRegularAdapter(shopProfileClassList, getContext());
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        recyclerView.setAdapter(adapter);
                        hideProgressBar();
                    }
                });
                hideProgressBar();
            }
        });
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        illustrationMsg.setVisibility(View.GONE);
        noBookmarkImage.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        if (bookmarkList.isEmpty()) {
            noBookmarkImage.setVisibility(View.VISIBLE);
            illustrationMsg.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noBookmarkImage.setVisibility(View.GONE);
            illustrationMsg.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}