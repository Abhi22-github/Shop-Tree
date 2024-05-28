package com.roaa.shops_customers.Other;

import com.roaa.shops_customers.ModelClasses.ProductClass;

public interface ProductsCallback {
    void sendProductAndQuantity(ProductClass productClass, int Quantity,int action);
}
