package com.roaa.shops_customers.BottomNavigation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.roaa.shops_customers.ModelClasses.ShopProfileClass;
import com.roaa.shops_customers.Other.DatabaseClass;
import com.roaa.shops_customers.R;
import com.roaa.shops_customers.ShopDetailsActivity;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.roaa.shops_customers.Utils.Constants.ERROR_DIALOG_REQUEST;
import static com.roaa.shops_customers.Utils.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;


public class MapFragment extends Fragment {
    private static final String TAG = "MapFragment";

    //global vars
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 22;
    private static final float DEFAULT_ZOOM = 14f;
    public static LatLng currentLocationLatlng;

    //local vars
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mfusedLocationProviderClient;
    private int radius = 2;
    private String shopFoundID;
    private View view;
    private Boolean mapStatus = false;
    private HashMap<String, Marker> hashMapMarker = new HashMap<>();
    private List<String> nearbyStoresList = new ArrayList<>();
    private DatabaseClass databaseClass;
    private MaterialButton increaseButton, decreaseButton;


    //local fields:

    //Firebase vars
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private ShopProfileClass shopProfileClass;


    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_map, container, false);
        initFields();
        //initialize Firebase
        initFirebase();


        //check for whether map permission is given or not
        getLocationPermission();

        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radius++;
                if(radius<=5){
                    getLocationPermission();
                }else {
                    radius = 6;
                    Toast.makeText(getContext(), "Can't increase the radius", Toast.LENGTH_SHORT).show();
                }
            }
        });
        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radius--;
                if(radius>=2){
                    getLocationPermission();
                }else {
                    radius = 2;
                    Toast.makeText(getContext(), "Can't decrease the radius", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    /**
     * initialization methods
     */
    private void initFields() {
        databaseClass = new DatabaseClass(getContext());
        increaseButton = view.findViewById(R.id.map_fragment_button_increase);
        decreaseButton = view.findViewById(R.id.map_fragment_button_decrease);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                initMap();
            } else {
                getLocationPermission();
            }
        }
    }

    private void getLocationPermission() {
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        //check if both permission are granted
        if (ContextCompat.checkSelfPermission(getContext().getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getContext().getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;

                //if both permission are granted then initialize the map
                initMap();
            }
        } // if not ask for permissions dialogue
        else {
            ActivityCompat.requestPermissions(getActivity(),
                    permission,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void initMap() {
        //get the map fragment from xml file
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("LongLogTag")
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d(TAG, "onMapReady: map is ready");
                mMap = googleMap;
                //if all permissions are granted the get device current location
                if (mLocationPermissionGranted) {
                    Log.d(TAG, "onMapReady: displaying current location");
                    getDeviceLocation();
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        //display msg to user that you have to give permission to access map
                        return;
                    }
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().isZoomControlsEnabled();

                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            ShopProfileClass shopProfileClass;
                            shopProfileClass = (ShopProfileClass) marker.getTag();
                            Log.d(TAG, "onMarkerClick: " + shopProfileClass.getShopName());
                            // openBottomSheetDialogue(shopProfileClass);

                            Intent singlePageIntent = new Intent(getContext(), ShopDetailsActivity.class);
                            singlePageIntent.putExtra("shopDetails", shopProfileClass);
                            startActivity(singlePageIntent);
                        }
                    });


                }
            }
        });
    }

    /*
        private void openBottomSheetDialogue(ShopProfileClass shopProfileClass) {
            Log.d(TAG, "openBottomSheetDialogue: entering into bottomsheet method");
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
            bottomSheetDialog.setContentView(R.layout.map_bottom_sheet_dialogue);
            bottomSheetDialog.setCanceledOnTouchOutside(true);
            ImageView imgView = bottomSheetDialog.findViewById(R.id.map_shop_image);
            TextView name = bottomSheetDialog.findViewById(R.id.map_shop_name);
            TextView type = bottomSheetDialog.findViewById(R.id.map_shop_type);
            MaterialButton info = bottomSheetDialog.findViewById(R.id.map_shop_info_button);
            ImageButton cancelButton = bottomSheetDialog.findViewById(R.id.map_cancel);;
            //set up all fields
            name.setText(shopDetailsClass.getShop_name());
            type.setText(shopDetailsClass.getShop_category());
            try {
                Picasso.get().load(shopDetailsClass.getShop_cover_image()).into(imgView);
            } catch (Exception e) {
                Log.d(TAG, "openBottomSheetDialogue: " + e.toString());
            }
            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent singlePageIntent = new Intent(MapActivity.this, SingleShopDetails.class);
                    singlePageIntent.putExtra("object", shopDetailsClass);
                    startActivity(singlePageIntent);
                }
            });

            bottomSheetDialog.show();

            cancelButton.setOnClickListener(v -> bottomSheetDialog.cancel());

        }
    */
    private void getAllNearbyShopsLocations() {
        Log.d(TAG, "getAllNearbyShopsLocations: entering into the method");
        DatabaseReference shopReferences = FirebaseDatabase.getInstance().getReference().child("Shop Locations");

        GeoFire geoFire = new GeoFire(shopReferences);
        if (currentLocationLatlng != null) {
            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocationLatlng.latitude, currentLocationLatlng.longitude), radius);
            Log.d(TAG, "getAllNearbyShopsLocations: " + currentLocationLatlng.latitude + " " + currentLocationLatlng.longitude);
            geoQuery.removeAllListeners();
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    shopFoundID = key;
                    LatLng latLng = new LatLng(location.latitude, location.longitude);
                    addShopMarkerOnMap(shopFoundID, latLng);
                    Log.d(TAG, "onKeyEntered: ");

                }

                @Override
                public void onKeyExited(String key) {
                    Log.d(TAG, "onKeyExited: ");
                    removeShopMarkerOnShop(key);

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                    Log.d(TAG, "onKeyMoved: ");
                    shopFoundID = key;
                    LatLng latLng = new LatLng(location.latitude, location.longitude);
                    removeShopMarkerOnShop(key);
                    addShopMarkerOnMap(shopFoundID, latLng);
                }

                @Override
                public void onGeoQueryReady() {

                    Log.d(TAG, "onGeoQueryReady: ");
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                    Log.d(TAG, "onGeoQueryError: ");
                }
            });
        }
    }

    private void removeShopMarkerOnShop(String ShopID) {
        Marker marker = hashMapMarker.get(ShopID);
        marker.remove();
        hashMapMarker.remove(ShopID);
    }

    private void addShopMarkerOnMap(String shopFoundID, LatLng latLng) {
        DocumentReference shopRef = fStore.collection("Shops").document(shopFoundID);
        MarkerOptions markerOptions = new MarkerOptions();
        shopRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "onComplete: fields exists");
                        /*
                        if (documentSnapshot.get(getString(R.string.shop_name_device_database_string)) != null
                                && documentSnapshot.get(getString(R.string.shop_category_device_database_string)) != null
                                && documentSnapshot.get(getString(R.string.shop_address_device_database_string)) != null
                                && documentSnapshot.get(getString(R.string.shop_opening_time_device_database_string)) != null
                                && documentSnapshot.get(getString(R.string.shop_closing_time_device_database_string)) != null
                                && documentSnapshot.get(getString(R.string.shop_working_days_device_database_string)) != null
                                && documentSnapshot.get(getString(R.string.shop_delivery_available_device_database_string)) != null
                                && documentSnapshot.get(getString(R.string.shop_orders_available_device_database_string)) != null) {*/
                        Log.d(TAG, "onComplete: shop profile is completed");

                        ShopProfileClass shopProfileClass = documentSnapshot.toObject(ShopProfileClass.class);
                        markerOptions.title(documentSnapshot.getString("shopName"));
                        markerOptions.position(latLng);
                        Marker shopMarker = mMap.addMarker(markerOptions);
                        shopMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker));
                        shopMarker.setTag(shopProfileClass);
                        hashMapMarker.put(shopFoundID, shopMarker);
                        // forOpeningBottomSheetDialogueForParticularID(shopProfileClass);
                        Log.d(TAG, "onComplete: for marking shop marker");

                    }
                } else {

                }
            }
        });
    }


    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the current device current location");

        mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        try {
            if (mLocationPermissionGranted) {
                Task location = mfusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Log.d(TAG, "onComplete: location found");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())
                                    , DEFAULT_ZOOM);
                            currentLocationLatlng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                            CircleOptions circleOptions = new CircleOptions()
                                    .center(new LatLng(currentLocationLatlng.latitude, currentLocationLatlng.longitude))
                                    .radius(radius * 1000)
                                    .strokeColor(Color.BLUE)
                                    .strokeWidth(0)
                                    .fillColor(0x250195F7);
                            mMap.clear();
                            mMap.addCircle(circleOptions);
                            mapStatus = true;
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    getAllNearbyShopsLocations();
                                }
                            };
                            Thread thread = new Thread(runnable);
                            thread.start();
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(getContext(), "unable to get location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException" + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving camera to lat" + latLng.latitude + "lng" + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
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

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable((Activity) getContext());

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog((Activity) getContext(), available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(getContext(), "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            askUserToTurnOnLocation();
            return false;
        }
        return true;
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

    private void askUserToTurnOnLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(getContext()).checkLocationSettings(builder.build());


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
                                        (Activity) getContext(),
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

}