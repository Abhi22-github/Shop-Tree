package com.roaa.shops_customers.ModelClasses;

public class ProductOrdersClass {

    private String productID;
    private String productQuantity;
    private String productPrice;
    private String productName;
    private Boolean isAvailable;

    public ProductOrdersClass() {
    }

    public ProductOrdersClass(String productID, String productQuantity,
                              String productPrice, String productName, Boolean isAvailable) {
        this.productID = productID;
        this.productQuantity = productQuantity;
        this.productPrice = productPrice;
        this.productName = productName;
        this.isAvailable = isAvailable;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(String productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }
}
