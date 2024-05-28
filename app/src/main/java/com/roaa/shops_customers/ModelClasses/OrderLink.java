package com.roaa.shops_customers.ModelClasses;

import java.io.Serializable;

public class OrderLink implements Serializable,Comparable<OrderLink> {
    private String orderDetailID;
    private String currentTime;

    public OrderLink(){}

    public OrderLink(String orderDetailID, String currentTime) {
        this.orderDetailID = orderDetailID;
        this.currentTime = currentTime;
    }

    public String getOrderDetailID() {
        return orderDetailID;
    }

    public void setOrderDetailID(String orderDetailID) {
        this.orderDetailID = orderDetailID;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    @Override
    public int compareTo(OrderLink o) {
        return getCurrentTime().compareTo(o.getCurrentTime());
    }
}
