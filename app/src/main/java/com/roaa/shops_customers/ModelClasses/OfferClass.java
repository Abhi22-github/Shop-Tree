package com.roaa.shops_customers.ModelClasses;

import java.io.Serializable;

public class OfferClass implements Serializable {
    private String offerText;
    private String validRange;
    private String offerID;

    public OfferClass(){}

    public OfferClass(String offerText, String validRange, String offerID) {
        this.offerText = offerText;
        this.validRange = validRange;
        this.offerID = offerID;
    }

    public String getOfferText() {
        return offerText;
    }

    public void setOfferText(String offerText) {
        this.offerText = offerText;
    }

    public String getValidRange() {
        return validRange;
    }

    public void setValidRange(String validRange) {
        this.validRange = validRange;
    }

    public String getOfferID() {
        return offerID;
    }

    public void setOfferID(String offerID) {
        this.offerID = offerID;
    }
}
