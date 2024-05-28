package com.roaa.shops_customers.ModelClasses;

public class CatalogImageClass {
    private String catalogImage;
    private String catalogImageDate;

    public CatalogImageClass() {
    }

    public CatalogImageClass(String catalogImage, String catalogImageDate) {
        this.catalogImage = catalogImage;
        this.catalogImageDate = catalogImageDate;
    }

    public String getCatalogImage() {
        return catalogImage;
    }

    public void setCatalogImage(String catalogImage) {
        this.catalogImage = catalogImage;
    }

    public String getCatalogImageDate() {
        return catalogImageDate;
    }

    public void setCatalogImageDate(String catalogImageDate) {
        this.catalogImageDate = catalogImageDate;
    }
}
