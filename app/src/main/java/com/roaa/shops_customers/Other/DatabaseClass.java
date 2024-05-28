package com.roaa.shops_customers.Other;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.roaa.shops_customers.ModelClasses.BookmarkClass;
import com.roaa.shops_customers.ModelClasses.TokenClass;
import com.roaa.shops_customers.ModelClasses.UserProfileClass;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class DatabaseClass {
    private static final String TAG = "DatabaseClass";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private List<BookmarkClass> bookmarkClassList = new ArrayList<>();
    private SharedPreferences userProfilePreference;
    private SharedPreferences.Editor userProfilePreferenceEditor;
    private List<String> nearbyShopIDList = new ArrayList<>();
    private Context mContext;

    public DatabaseClass() {
    }

    public DatabaseClass(Context mContext) {
        this.mContext = mContext;
        userProfilePreference = mContext.getSharedPreferences("userProfile", MODE_PRIVATE);
        userProfilePreferenceEditor = userProfilePreference.edit();
    }

    public void gettingUserProfileDataFromDatabaseWithoutCallback() {
        DocumentReference userStoreRef = firestore.collection("Users").document(mAuth.getCurrentUser().getUid());
        userStoreRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    UserProfileClass userProfileClass = documentSnapshot.toObject(UserProfileClass.class);
                    Gson gson = new Gson();
                    String json = gson.toJson(userProfileClass);
                    userProfilePreferenceEditor.putString("userProfileJSON", json);
                    userProfilePreferenceEditor.commit();
                } else {

                }
            }
        });
    }

    public void gettingUserProfileDataFromDatabaseWithCallback(FirestoreCallback firestoreCallback) {
        DocumentReference userStoreRef = firestore.collection("Users").document(mAuth.getCurrentUser().getUid());
        userStoreRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    UserProfileClass userProfileClass = documentSnapshot.toObject(UserProfileClass.class);
                    Gson gson = new Gson();
                    String json = gson.toJson(userProfileClass);
                    userProfilePreferenceEditor.putString("userProfileJSON", json);
                    userProfilePreferenceEditor.commit();
                    firestoreCallback.dataGetComplete();
                } else {

                }
            }
        });
    }


    public void gettingUserBookmarksDataFromDatabaseWithoutCallbacks() {

        Query query = firestore.collection("Users").document(mAuth.getCurrentUser().getUid())
                .collection("Bookmarks");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot querySnapshot = task.getResult();
                bookmarkClassList.clear();
                // Add all to your list
                for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                    BookmarkClass bookmarkClass = documentSnapshot.toObject(BookmarkClass.class);
                    bookmarkClassList.add(bookmarkClass);
                }
                Log.d(TAG, "onComplete: bookmark list" + bookmarkClassList);
                Gson gson = new Gson();
                String json = gson.toJson(bookmarkClassList);
                userProfilePreferenceEditor.putString("userBookmarksJSON", json);
                userProfilePreferenceEditor.commit();
            }
        });
    }

    public void gettingUserBookmarksDataFromDatabaseWithCallbacks(FirestoreCallback firestoreCallback) {
        Query query = firestore.collection("Users").document(mAuth.getCurrentUser().getUid())
                .collection("Bookmarks");

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                bookmarkClassList.clear();
                QuerySnapshot querySnapshot = task.getResult();
                // Add all to your list
                for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                    BookmarkClass bookmarkClass = documentSnapshot.toObject(BookmarkClass.class);
                    bookmarkClassList.add(bookmarkClass);
                }
                Log.d(TAG, "onComplete: bookmark list" + bookmarkClassList + " " + mContext);
                Gson gson = new Gson();
                String json = gson.toJson(bookmarkClassList);
                userProfilePreferenceEditor.putString("userBookmarksJSON", json);
                userProfilePreferenceEditor.commit();
                firestoreCallback.dataGetComplete();
            }
        });
    }

    public void savingUserTokenInDatabase() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    //saving this token in firestore

                    TokenClass tokenClass = new TokenClass(token);
                    firestore.collection("Customer Tokens").document(mAuth.getCurrentUser().getUid())
                            .set(tokenClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                            } else {
                                //nothin
                            }
                        }
                    });


                } else {

                }
            }
        });
    }

    public void gettingUserTokenFromDatabaseWithoutCallbacks() {
        String tokenId = mAuth.getCurrentUser().getUid();
        firestore.collection("Tokens").document(tokenId).get().addOnCompleteListener(
                new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            TokenClass tokenClass = documentSnapshot.toObject(TokenClass.class);
                            Gson gson = new Gson();
                            String json = gson.toJson(tokenClass);
                            userProfilePreferenceEditor.putString("userTokenJSON", json);
                            userProfilePreferenceEditor.commit();
                        } else {

                        }
                    }
                }
        );
    }

    public void gettingUserTokenFromDatabaseWithCallbacks(FirestoreCallback firestoreCallback) {
        String tokenId = mAuth.getCurrentUser().getUid();
        firestore.collection("Customer Tokens").document(tokenId).get().addOnCompleteListener(
                new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            TokenClass tokenClass = documentSnapshot.toObject(TokenClass.class);
                            Gson gson = new Gson();
                            String json = gson.toJson(tokenClass);
                            userProfilePreferenceEditor.putString("userTokenJSON", json);
                            userProfilePreferenceEditor.commit();
                            firestoreCallback.dataGetComplete();

                        } else {

                        }
                    }
                }
        );
    }

    public TokenClass gettingUserTokenFromDevice() {
        TokenClass tokenClass;
        Gson gson = new Gson();
        String json = userProfilePreference.getString("userTokenJSON", "");
        tokenClass = gson.fromJson(json, TokenClass.class);
        return tokenClass;
    }

    public void addNearbyStoresInDevice(List<String> nearbyShopIDListReceived) {
        nearbyShopIDList.clear();
        nearbyShopIDList.addAll(nearbyShopIDListReceived);
        Gson gson = new Gson();
        String json = gson.toJson(nearbyShopIDList);
        userProfilePreferenceEditor.putString("nearbyStoreJSON", json);
        userProfilePreferenceEditor.commit();
        Log.d(TAG, "addNearbyStoresInDevice: " + nearbyShopIDList);
    }

    public void saveAddressInDevice(String address) {
        userProfilePreferenceEditor.putString("userAddress", address);
        userProfilePreferenceEditor.commit();
    }

    public String getAddressFromDevice() {
        String address = userProfilePreference.getString("userAddress", "");
        return address;
    }

    public UserProfileClass gettingUserProfileDataFromDevice() {
        UserProfileClass userProfileClass;
        Gson gson = new Gson();
        String json = userProfilePreference.getString("userProfileJSON", "");
        userProfileClass = gson.fromJson(json, UserProfileClass.class);
        return userProfileClass;
    }

    public List<BookmarkClass> gettingUserBookmarksDataFromDevice() {
        BookmarkClass bookmarkClass;
        List<BookmarkClass> bookmarkClassList;
        Gson gson = new Gson();
        String json = userProfilePreference.getString("userBookmarksJSON", "");
        Type type = new TypeToken<List<BookmarkClass>>() {
        }.getType();
        bookmarkClassList = gson.fromJson(json, type);
        return bookmarkClassList;
    }

    public List<String> gettingNearbyStoreFromDevice() {
        List<String> shopIDList;
        Gson gson = new Gson();
        String json = userProfilePreference.getString("nearbyStoreJSON", "");
        Type type = new TypeToken<List<String>>() {
        }.getType();
        shopIDList = gson.fromJson(json, type);
        Log.d(TAG, "gettingNearbyStoreFromDevice: " + shopIDList);
        return shopIDList;
    }

    public void saveResponseOfUserLocationActivity(boolean response) {
        userProfilePreferenceEditor.putBoolean("userLocationResponse", response);
        userProfilePreferenceEditor.commit();
    }

    public boolean getResponseOfUserLocationActivity() {
        boolean response = userProfilePreference.getBoolean("userLocationResponse", false);
        return response;
    }

}
