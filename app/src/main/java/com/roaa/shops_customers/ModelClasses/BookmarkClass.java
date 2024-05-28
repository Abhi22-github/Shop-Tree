package com.roaa.shops_customers.ModelClasses;

public class BookmarkClass {
    private String shopID;

    public BookmarkClass(){

    }

    public BookmarkClass(String shopID) {
        this.shopID = shopID;
    }

    public String getShopID() {
        return shopID;
    }

    public void setShopID(String shopID) {
        this.shopID = shopID;
    }
}
