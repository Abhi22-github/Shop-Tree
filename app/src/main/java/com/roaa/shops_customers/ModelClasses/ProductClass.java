package com.roaa.shops_customers.ModelClasses;

import java.io.Serializable;

public class ProductClass implements Serializable {
    private String productName;
    private String productBrand;
    private String productCategory;
    private String productPrice;
    private String productDescription;
    private String shopID;
    private String productImage;
    private String productID;
    private Boolean isAvailable;

    public ProductClass() {
    }

    public ProductClass(String productName, String productBrand, String productCategory,
                        String productPrice, String productDescription, String shopID,
                        String productImage, String productID, Boolean isAvailable) {
        this.productName = productName;
        this.productBrand = productBrand;
        this.productCategory = productCategory;
        this.productPrice = productPrice;
        this.productDescription = productDescription;
        this.shopID = shopID;
        this.productImage = productImage;
        this.productID = productID;
        this.isAvailable = isAvailable;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductBrand() {
        return productBrand;
    }

    public void setProductBrand(String productBrand) {
        this.productBrand = productBrand;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getShopID() {
        return shopID;
    }

    public void setShopID(String shopID) {
        this.shopID = shopID;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }
}
