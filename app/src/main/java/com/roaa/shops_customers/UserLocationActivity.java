package com.roaa.shops_customers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.roaa.shops_customers.Other.DatabaseClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.roaa.shops_customers.Utils.Constants.DEFAULT_ZOOM;
import static com.roaa.shops_customers.Utils.Constants.ERROR_DIALOG_REQUEST;
import static com.roaa.shops_customers.Utils.Constants.LOCATION_PERMISSION_REQUEST_CODE;
import static com.roaa.shops_customers.Utils.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class UserLocationActivity extends AppCompatActivity {
    private static final String TAG = "UserLocationActivity";

    //global vars
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static Boolean IS_LOCATION_ENABLED = false;

    //local vars
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private int radius = 3;
    private FusedLocationProviderClient mfusedLocationProviderClient;
    private String from;
    private Context context;
    private DatabaseClass databaseClass;
    private List<String> nearbyShopIDList = new ArrayList<>();

    //layout widgets
    private MaterialButton nextButton;
    private TextView addressText;
    private ImageButton backButton;
    private ProgressBar progressBar;

    //Firebase vars
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_location);
        initFields();

        initFirebase();

        backButton.setOnClickListener(v -> onBackPressed());

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                nextButton.setEnabled(false);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        getAllNearbyShopsLocations();
                    }
                });

            }
        });

        //check for whether map permission is given or not
        getLocationPermission();
    }

    private void initFields() {
        nextButton = findViewById(R.id.shop_location_activity_button_next_button);
        backButton = findViewById(R.id.shop_location_activity_image_button_back_button);
        mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        databaseClass = new DatabaseClass(UserLocationActivity.this);
        addressText = findViewById(R.id.user_location_activity_address_field);
        progressBar = findViewById(R.id.shop_location_activity_progress_bar_progress);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
    }

    private void getAllNearbyShopsLocations() {
        Log.d(TAG, "getAllNearbyShopsLocations: entering into the method");
        DatabaseReference shopReferences = FirebaseDatabase.getInstance().getReference().child("Shop Locations");

        LatLng currentLocationLatlng = mMap.getCameraPosition().target;
        GeoFire geoFire = new GeoFire(shopReferences);
        if (currentLocationLatlng != null) {
            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocationLatlng.latitude, currentLocationLatlng.longitude), radius);
            Log.d(TAG, "getAllNearbyShopsLocations: " + currentLocationLatlng.latitude + " " + currentLocationLatlng.longitude);
            geoQuery.removeAllListeners();
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {

                    nearbyShopIDList.add(key);
                }

                @Override
                public void onKeyExited(String key) {
                    Log.d(TAG, "onKeyExited: ");
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
                    databaseClass.saveAddressInDevice(addressText.getText().toString());
                    databaseClass.saveResponseOfUserLocationActivity(true);
                    sendUserToShopMainActivity();
                    progressBar.setVisibility(View.GONE);
                    nextButton.setEnabled(false);
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                    Log.d(TAG, "onGeoQueryError: ");
                }
            });
        }
    }

    /*
        private void saveGeoPointInDatabase() {
            showProgressDialog();
            //getting marker co-ordinate
            LatLng midLatLng = mMap.getCameraPosition().target;
            Location shopLocation = new Location("text");
            //converting latlng co-ordinate to location co-ordinate
            shopLocation.setLatitude(midLatLng.latitude);
            shopLocation.setLongitude(midLatLng.longitude);
            //converting location co-ordinate to geo-point co-ordinate
            GeoPoint geoPoint = new GeoPoint(shopLocation.getLatitude(), shopLocation.getLongitude());
            LocationClass location = new LocationClass();
            //setting geo point with location class
            location.setShop_location(geoPoint);
            //creating hashmap for shop details
            //getting current user id
            String currentUserID = mAuth.getUid().toString();
            DocumentReference shopRef = fStore.collection("Shop Locations").document(currentUserID);
            shopRef.set(location).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        //location is saved in shop details also
                        Log.d(TAG, "onComplete: location is saved in shop details also");

                        //code for geo fire
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Shop Locations");
                        GeoFire geoFire = new GeoFire(reference);
                        geoFire.setLocation(currentUserID, new GeoLocation(shopLocation.getLatitude(), shopLocation.getLongitude()));

                        sendUserToShopDetailsActivity();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressDialog();
                            }
                        }, 300);

                    } else {
                        hideProgressDialog();
                        //error while saving location in location
                    }
                }
            });


        }
    */
    private void getAddressFromLatlng() {
        LatLng currentLocationLatlng = mMap.getCameraPosition().target;
        double latitude = currentLocationLatlng.latitude;
        double longitude = currentLocationLatlng.longitude;
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String removePart = "Unnamed Road,";
            String output = "";
            if (address.toLowerCase().contains(removePart.toLowerCase())) {
                Log.d(TAG, "getAddressFromLatlng: +++++" + address);
                output = address.substring(address.indexOf(',') + 1);
            } else {
                output = address;
            }


            addressText.setText(output);
        } catch (Exception e) {

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
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())
                                    , DEFAULT_ZOOM);
                            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            //getAllNearbyShopsLocations();

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving camera to lat" + latLng.latitude + "lng" + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initMap() {
        //get the map fragment from xml file
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("LongLogTag")
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                //if all permissions are granted the get device current location
                if (mLocationPermissionGranted) {
                    Log.d(TAG, "onMapReady: displaying current location");
                    getDeviceLocation();
                    if (ActivityCompat.checkSelfPermission(UserLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserLocationActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        //display msg to user that you have to give permission to access map
                        return;
                    }
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    mMap.getUiSettings().isCompassEnabled();
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().isZoomControlsEnabled();
                    mMap.getUiSettings().setAllGesturesEnabled(true);

                    mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                        @Override
                        public void onCameraIdle() {
                            getAddressFromLatlng();
                        }
                    });
                    mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                        @Override
                        public void onCameraMoveStarted(int i) {

                        }
                    });
                    mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                        @Override
                        public void onCameraMove() {

                        }
                    });
                    mMap.setOnCameraMoveCanceledListener(new GoogleMap.OnCameraMoveCanceledListener() {
                        @Override
                        public void onCameraMoveCanceled() {

                        }
                    });


                }
            }
        });
    }


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
                initMap();


            }
        } // if not ask for permissions dialogue
        else {
            ActivityCompat.requestPermissions(this,
                    permission,
                    LOCATION_PERMISSION_REQUEST_CODE);


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            //if LOCATION_PERMISSION_REQUEST_CODE is equal to requestcode
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    // check if both permissions are granted
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    // if both permissions are granted initialize our map
                    initMap();
                }
            }
        }
    }


    /**
     * intent methods
     */
    private void sendUserToShopMainActivity() {
        Intent MainActivityIntent = new Intent(UserLocationActivity.this, MainActivity.class);
        MainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(MainActivityIntent);
        finish();
    }

    /*
        private void showProgressDialog() {
            progressBar.setVisibility(View.VISIBLE);
            nextButton.setText("");
            nextButton.setEnabled(false);
        }

        private void hideProgressDialog() {
            progressBar.setVisibility(View.GONE);
            nextButton.setText("Next");
            nextButton.setEnabled(true);
        }
    */
    private void askUserToTurnOnLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(UserLocationActivity.this).checkLocationSettings(builder.build());


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
                                        UserLocationActivity.this,
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
                    initMap();
                } else {
                    getLocationPermission();
                }
            }
        }
        getLocationPermission();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                initMap();
            } else {
                getLocationPermission();
            }
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

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(UserLocationActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occurred but we can resolve it
            Log.d(TAG, "isServicesOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(UserLocationActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            askUserToTurnOnLocation();
            return false;
        }
        return true;
    }
}