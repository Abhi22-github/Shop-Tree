package com.roaa.shops_customers.ModelClasses;

public class CategoryClass {
    private String categoryName;

    public CategoryClass(){}

    public CategoryClass(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
