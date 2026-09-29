package com.example.electronicsgadgets.models;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String cartItemId;
    private String productId;
    private String productName;
    private String productBrand;
    private double price;
    private String imageUrl;
    private int quantity;
    private int stockQuantity;

    public CartItem() {
    }

    public CartItem(String cartItemId, String productId, String productName, String productBrand,
                    double price, String imageUrl, int quantity, int stockQuantity) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.productName = productName;
        this.productBrand = productBrand;
        this.price = price;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.stockQuantity = stockQuantity;
    }

    public String getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(String cartItemId) {
        this.cartItemId = cartItemId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public double getTotalPrice() {
        return price * quantity;
    }
}
