package com.roaa.shops_customers.ModelClasses;

import com.google.firebase.firestore.GeoPoint;

public class LocationClass {
    private GeoPoint shop_location;

    public LocationClass() {
    }

    public LocationClass(GeoPoint shop_location) {
        this.shop_location = shop_location;
    }

    public GeoPoint getShop_location() {
        return shop_location;
    }

    public void setShop_location(GeoPoint shop_location) {
        this.shop_location = shop_location;
    }
}
