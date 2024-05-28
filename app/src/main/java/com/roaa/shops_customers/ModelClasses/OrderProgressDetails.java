package com.roaa.shops_customers.ModelClasses;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;

public class OrderProgressDetails implements Serializable,Comparable<OrderProgressDetails>{
    @ServerTimestamp
    private String userID;
    private String shopID;
    private Integer orderStatus;
    private Boolean isModified;
    private String orderID;
    private String orderTotal;
    private String orderCreatedTime;
    private Integer totalItemCount;
    private String orderCode;

    public OrderProgressDetails() {
    }

    public OrderProgressDetails(String userID, String shopID, Integer orderStatus,
                                Boolean isModified, String orderID, String orderTotal,
                                String orderCreatedTime, Integer totalItemCount, String orderCode) {
        this.userID = userID;
        this.shopID = shopID;
        this.orderStatus = orderStatus;
        this.isModified = isModified;
        this.orderID = orderID;
        this.orderTotal = orderTotal;
        this.orderCreatedTime = orderCreatedTime;
        this.totalItemCount = totalItemCount;
        this.orderCode = orderCode;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getShopID() {
        return shopID;
    }

    public void setShopID(String shopID) {
        this.shopID = shopID;
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Boolean getModified() {
        return isModified;
    }

    public void setModified(Boolean modified) {
        isModified = modified;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(String orderTotal) {
        this.orderTotal = orderTotal;
    }

    public String getOrderCreatedTime() {
        return orderCreatedTime;
    }

    public void setOrderCreatedTime(String orderCreatedTime) {
        this.orderCreatedTime = orderCreatedTime;
    }

    public Integer getTotalItemCount() {
        return totalItemCount;
    }

    public void setTotalItemCount(Integer totalItemCount) {
        this.totalItemCount = totalItemCount;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    @Override
    public int compareTo(OrderProgressDetails o) {
        return getOrderCreatedTime().compareTo(o.getOrderCreatedTime());
    }
}
