package com.roaa.shops_customers.ModelClasses;

import java.io.Serializable;

public class ShopProfileClass implements Serializable {
    private String shopID;
    private String shopName;
    private String shopAddress;
    private String shopCategory;
    private String shopImage;
    private String shopOpeningTime;
    private String shopClosingTime;
    private Boolean sunday;
    private Boolean monday;
    private Boolean tuesday;
    private Boolean wednesday;
    private Boolean thursday;
    private Boolean friday;
    private Boolean saturday;
    private String shopEmail;
    private String shopWebsite;

    public ShopProfileClass() {
    }

    public ShopProfileClass(String shopID, String shopName, String shopAddress, String shopCategory,
                            String shopImage, String shopOpeningTime, String shopClosingTime,
                            Boolean sunday, Boolean monday, Boolean tuesday, Boolean wednesday,
                            Boolean thursday, Boolean friday, Boolean saturday, String shopEmail,
                            String shopWebsite) {
        this.shopName = shopName;
        this.shopAddress = shopAddress;
        this.shopCategory = shopCategory;
        this.shopImage = shopImage;
        this.shopOpeningTime = shopOpeningTime;
        this.shopClosingTime = shopClosingTime;
        this.sunday = sunday;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.shopEmail = shopEmail;
        this.shopWebsite = shopWebsite;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
    }

    public String getShopCategory() {
        return shopCategory;
    }

    public void setShopCategory(String shopCategory) {
        this.shopCategory = shopCategory;
    }

    public String getShopImage() {
        return shopImage;
    }

    public void setShopImage(String shopImage) {
        this.shopImage = shopImage;
    }

    public String getShopOpeningTime() {
        return shopOpeningTime;
    }

    public void setShopOpeningTime(String shopOpeningTime) {
        this.shopOpeningTime = shopOpeningTime;
    }

    public String getShopClosingTime() {
        return shopClosingTime;
    }

    public void setShopClosingTime(String shopClosingTime) {
        this.shopClosingTime = shopClosingTime;
    }

    public Boolean getSunday() {
        return sunday;
    }

    public void setSunday(Boolean sunday) {
        this.sunday = sunday;
    }

    public Boolean getMonday() {
        return monday;
    }

    public void setMonday(Boolean monday) {
        this.monday = monday;
    }

    public Boolean getTuesday() {
        return tuesday;
    }

    public void setTuesday(Boolean tuesday) {
        this.tuesday = tuesday;
    }

    public Boolean getWednesday() {
        return wednesday;
    }

    public void setWednesday(Boolean wednesday) {
        this.wednesday = wednesday;
    }

    public Boolean getThursday() {
        return thursday;
    }

    public void setThursday(Boolean thursday) {
        this.thursday = thursday;
    }

    public Boolean getFriday() {
        return friday;
    }

    public void setFriday(Boolean friday) {
        this.friday = friday;
    }

    public Boolean getSaturday() {
        return saturday;
    }

    public void setSaturday(Boolean saturday) {
        this.saturday = saturday;
    }

    public String getShopEmail() {
        return shopEmail;
    }

    public void setShopEmail(String shopEmail) {
        this.shopEmail = shopEmail;
    }

    public String getShopWebsite() {
        return shopWebsite;
    }

    public void setShopWebsite(String shopWebsite) {
        this.shopWebsite = shopWebsite;
    }

    public String getShopID() {
        return shopID;
    }

    public void setShopID(String shopID) {
        this.shopID = shopID;
    }
}
