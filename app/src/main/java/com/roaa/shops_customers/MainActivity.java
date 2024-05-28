package com.roaa.shops_customers;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.roaa.shops_customers.BottomNavigation.AccountFragment;
import com.roaa.shops_customers.BottomNavigation.HomeFragment;
import com.roaa.shops_customers.BottomNavigation.MapFragment;
import com.roaa.shops_customers.BottomNavigation.OrderFragment;
import com.roaa.shops_customers.Other.DatabaseClass;
import com.roaa.shops_customers.VerificationActivities.RegisterActivity;

import java.util.ArrayList;
import java.util.List;

import static com.roaa.shops_customers.Utils.Constants.ERROR_DIALOG_REQUEST;
import static com.roaa.shops_customers.Utils.Constants.LOCATION_PERMISSION_REQUEST_CODE;
import static com.roaa.shops_customers.Utils.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    //global vars
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static Boolean IS_LOCATION_ENABLED = false;

    //widgets
    public BottomNavigationView bottomNavigationView;
    //  private ProgressBar loadingProgressBar;
    private ConstraintLayout coordinatorLayout;
    private FrameLayout mainContentLayout;
    private ProgressBar progressBar;

    //vars
    private SharedPreferences userProfilePreference;
    private SharedPreferences.Editor userProfilePreferenceEditor;
    private Fragment active, inActive;
    public final FragmentManager fm = getSupportFragmentManager();
    private DatabaseClass databaseClass;
    private List<String> nearbyShopIDList = new ArrayList<>();
    private Boolean doubleBackToExitPressedOnce = false;
    private Boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mfusedLocationProviderClient;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore rootRef;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFields();
        initFirebase();

        progressBar.setVisibility(View.VISIBLE);
        mainContentLayout.setVisibility(View.GONE);

        checkUser();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (checkMapServices()) {
                    if (mLocationPermissionGranted) {
                        getDeviceLocation();
                    } else {
                        getLocationPermission();
                    }
                }
            }
        });
        bottomNavigationBarSetup();

        //checkingDataAndSettingUpBottomNavigationBar();

    }

    private void initFields() {
        bottomNavigationView = findViewById(R.id.main_activity_bottom_navigation_fragment_holder);
        userProfilePreference = getSharedPreferences("userProfile", MODE_PRIVATE);
        userProfilePreferenceEditor = userProfilePreference.edit();
        databaseClass = new DatabaseClass(MainActivity.this);
        //   loadingProgressBar = findViewById(R.id.main_activity_progress_bar_loading_progress);
        // mainContentLayout = findViewById(R.id.main_activity_frame_layout_fragment_container);
        mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        coordinatorLayout = findViewById(R.id.coordinate_layout);
        mainContentLayout = findViewById(R.id.main_activity_frame_layout_fragment_container);
        progressBar = findViewById(R.id.main_progress_bar);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseFirestore.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (isOnline()) {
            databaseClass.gettingUserProfileDataFromDatabaseWithoutCallback();
            databaseClass.savingUserTokenInDatabase();
        } else {
            NoInternetConnectionSnackbar();
        }
        if (databaseClass.getResponseOfUserLocationActivity()) {
            dismissLoadingBar();
        } else {
            sendUserToLocationActivity();
        }
    }

    private void NoInternetConnectionSnackbar() {
        Snackbar snackbar = Snackbar.make(mainContentLayout, "No internet connection", Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(getResources().getColor(R.color.green))
                .setAction("Refresh", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isOnline()) {
                            checkUser();
                        } else {
                            NoInternetConnectionSnackbar();
                        }
                    }
                });
        View snackBarView = snackbar.getView();
        snackBarView.setTranslationY(-(convertDpToPixel(60, MainActivity.this)));
        snackbar.show();
    }

    public float convertDpToPixel(float dp, Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

   /* private void checkingDataAndSettingUpBottomNavigationBar() {
        Log.d(TAG, "checkingDataAndSettingUpBottomNavigationBar: ");
        // showLoadingProgressbar();
        if (mAuth.getCurrentUser() != null) {
            DocumentReference userStoreRef = firestore.collection("Users").document(mAuth.getCurrentUser().getUid());
            userStoreRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            databaseClass.gettingUserProfileDataFromDatabaseWithCallback(new FirestoreCallback() {
                                @Override
                                public void dataGetComplete() {
                                    try {
                                        nearbyShopIDList = databaseClass.gettingNearbyStoreFromDevice();
                                        String address = databaseClass.getAddressFromDevice();
                                        try {
                                            if (nearbyShopIDList.isEmpty() || address.isEmpty()) {
                                                Log.d(TAG, "dataGetComplete: empty");
                                                sendUserToLocationActivity();
                                            } else {
                                                Log.d(TAG, "dataGetComplete: not empty");
                                                bottomNavigationBarSetup();
                                                //  hideLoadingProgressbar();
                                            }
                                        } catch (Exception e) {
                                            Log.d(TAG, "dataGetComplete: null");
                                            sendUserToLocationActivity();
                                        }
                                    } catch (Exception e) {

                                    }
                                }
                            });
                        } else {

                        }
                    } else {
                        Log.d(TAG, "Failed with: ", task.getException());
                    }
                }
            });
        }

    }*/


    //checking if internet is on or not
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    private void bottomNavigationBarSetup() {

        final Fragment homeFragment = new HomeFragment();
        final Fragment orderFragment = new OrderFragment();
        final Fragment accountFragment = new AccountFragment();
        active = homeFragment;

        fm.beginTransaction().add(R.id.main_activity_frame_layout_fragment_container, accountFragment, "4").hide(accountFragment).commit();
        fm.beginTransaction().add(R.id.main_activity_frame_layout_fragment_container, orderFragment, "2").hide(orderFragment).commit();
        fm.beginTransaction().add(R.id.main_activity_frame_layout_fragment_container, homeFragment, "1").commit();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.bottom_navigation_home:
                        fm.beginTransaction().hide(active).show(homeFragment).commit();
                        homeFragment.onStart();
                        active = homeFragment;


                        break;
                    case R.id.bottom_navigation_library:
                        fm.beginTransaction().hide(active).show(orderFragment).commit();
                        active = orderFragment;
                        orderFragment.onStart();

                        break;
                    case R.id.bottom_navigation_map:
                        inActive = new MapFragment();
                        fm.beginTransaction().hide(active).add(R.id.main_activity_frame_layout_fragment_container, inActive).commit();
                        active = inActive;
                        break;
                    case R.id.bottom_navigation_account:
                        fm.beginTransaction().hide(active).show(accountFragment).commit();
                        active = accountFragment;
                        accountFragment.onStart();
                        break;

                }
                return true;
            }
        });
    }


    /**
     * intent related activities
     */


    private void sendUserToLocationActivity() {
        Intent locationActivityIntent = new Intent(MainActivity.this, UserLocationActivity.class);
        locationActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(locationActivityIntent);
        finish();
    }

    /*
        public void showLoadingProgressbar() {
            loadingProgressBar.setVisibility(View.VISIBLE);
            //   mainContentLayout.setVisibility(View.GONE);
        }

        public void hideLoadingProgressbar() {
            loadingProgressBar.setVisibility(View.GONE);
            // mainContentLayout.setVisibility(View.VISIBLE);
        }
    */
    @Override
    public void onBackPressed() {
        if (fm.getBackStackEntryCount() > 0) {
            Log.i("MainActivity", "popping backstack");
            fm.popBackStack();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                MainActivity.this.finish();
                System.exit(0);
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Tap again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);

        }
    }

    private void verifyUserExistence() {
        String currentUserID = mAuth.getCurrentUser().getUid();
        DocumentReference userStoreRef = firestore.collection("Users").document(currentUserID);
        userStoreRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        try {
                            databaseClass.gettingUserProfileDataFromDatabaseWithoutCallback();
                            databaseClass.gettingUserBookmarksDataFromDatabaseWithoutCallbacks();
                            databaseClass.savingUserTokenInDatabase();
                            onStart();
                        } catch (Exception e) {
                            sendUserToUserNameActivity();
                        }

                    } else {
                        sendUserToUserNameActivity();

                    }
                } else {
                    Toast.makeText(MainActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });

    }


    private void sendUserToRegisterOTPActivity() {
        Intent registerOTPActivity = new Intent(MainActivity.this, RegisterActivity.class);
        registerOTPActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(registerOTPActivity);
        finish();
    }


    private void sendUserToUserNameActivity() {
        Intent userNameIntent = new Intent(MainActivity.this, UserProfileSetupActivity.class);
        userNameIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        userNameIntent.putExtra("from", "MainActivity");
        startActivity(userNameIntent);
        finish();
    }

    private void checkUser() {
        if (isOnline()) {
            Log.d(TAG, "onStart: ");
            //showLoadingProgressbar();
            if (mAuth.getCurrentUser() == null) {
                //if not present then create account
                sendUserToRegisterOTPActivity();

            } else {
                Log.d(TAG, "onStart: checking if profile is completed");
                //else check if profile progress is completed or not
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        verifyUserExistence();
                    }
                };

                Thread thread = new Thread(runnable);
                thread.start();
            }
        }
    }

    /**
     * location methods
     */


    private void getLocationPermission() {
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        Log.d(TAG, "getLocationPermission: ask");
        //check if both permission are granted
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                //if both permission are granted then initialize the map
                //and location is enable
                Log.d(TAG, "getLocationPermission:perform ");
                getDeviceLocation();


            }
        } // if not ask for permissions dialogue
        else {
            ActivityCompat.requestPermissions(this,
                    permission,
                    LOCATION_PERMISSION_REQUEST_CODE);


        }
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the current device current location");

        try {
            if (mLocationPermissionGranted) {


                Task location = mfusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.getResult() != null) {
                            Log.d(TAG, "onComplete: location found");
                            Location currentLocation = (Location) task.getResult();
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    getAllNearbyShopsLocations(currentLocation);
                                }
                            });


                        } else {
                            requestNewLocation();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException" + e.getMessage());
        }
    }

    private void requestNewLocation() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest
                = new LocationRequest();
        mLocationRequest.setPriority(
                LocationRequest
                        .PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mfusedLocationProviderClient
                = LocationServices
                .getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }
        mfusedLocationProviderClient
                .requestLocationUpdates(
                        mLocationRequest,
                        mLocationCallback,
                        Looper.myLooper());

    }


    private LocationCallback
            mLocationCallback
            = new LocationCallback() {

        @Override
        public void onLocationResult(
                LocationResult locationResult) {
            Location mLastLocation
                    = locationResult
                    .getLastLocation();
            getDeviceLocation();
        }
    };

    private void getAllNearbyShopsLocations(Location currentLocation) {
        Log.d(TAG, "getAllNearbyShopsLocations: entering into the method");
        DatabaseReference shopReferences = FirebaseDatabase.getInstance().getReference().child("Shop Locations");

        LatLng currentLocationLatlng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        GeoFire geoFire = new GeoFire(shopReferences);
        if (currentLocationLatlng != null) {
            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocationLatlng.latitude, currentLocationLatlng.longitude), 3);
            Log.d(TAG, "getAllNearbyShopsLocations: " + currentLocationLatlng.latitude + " " + currentLocationLatlng.longitude);
            geoQuery.removeAllListeners();
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {

                    nearbyShopIDList.add(key);
                    Log.d(TAG, "onKeyEntered: " + key);
                }

                @Override
                public void onKeyExited(String key) {
                    Log.d(TAG, "onKeyExited: " + key);
                    nearbyShopIDList.remove(key);
                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                    Log.d(TAG, "onKeyMoved: ");

                }

                @Override
                public void onGeoQueryReady() {
                    Log.d(TAG, "onGeoQueryReady: ");
                    databaseClass.addNearbyStoresInDevice(nearbyShopIDList);
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                    Log.d(TAG, "onGeoQueryError: ");
                }
            });
        }
    }

    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occurred but we can resolve it
            Log.d(TAG, "isServicesOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showLocationDialog();
            // askUserToTurnOnLocation();
            return false;
        }
        return true;
    }

    private void askUserToTurnOnLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(MainActivity.this).checkLocationSettings(builder.build());


        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        MainActivity.this,
                                        LocationRequest.PRIORITY_HIGH_ACCURACY);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (mLocationPermissionGranted) {
                    getDeviceLocation();
                } else {
                    getLocationPermission();
                }
            }
        }
        getLocationPermission();
    }

    private void showLocationDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.location_alert_dialog, null);

        MaterialButton turnOnLocationButton = view.findViewById(R.id.turn_on_device_location);
        MaterialButton selectLocationManually = view.findViewById(R.id.select_location_manually);


        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();
        alertDialog.show();
        turnOnLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askUserToTurnOnLocation();
                alertDialog.dismiss();
            }
        });

        selectLocationManually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendToMapActivity = new Intent(MainActivity.this, UserLocationActivity.class);
                startActivity(sendToMapActivity);
            }
        });
    }


    public void dismissLoadingBar() {
        if (databaseClass.getResponseOfUserLocationActivity()) {
            bottomNavigationView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            mainContentLayout.setVisibility(View.VISIBLE);
        } else {
            sendUserToLocationActivity();
        }


    }

}