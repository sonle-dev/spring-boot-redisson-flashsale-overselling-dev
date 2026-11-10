package com.example.flashsaleservice.entity;

import javax.persistence.*;

@Entity
@Table(name = "flash_sale_products")
public class FlashSaleProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "flash_sale_price")
    private double flashSalePrice;

    @Column(name = "stock", nullable = false)
    private int stock;

    public FlashSaleProduct() {}

    public FlashSaleProduct(String productName, double flashSalePrice, int stock) {
        this.productName = productName;
        this.flashSalePrice = flashSalePrice;
        this.stock = stock;
    }

    public Long getId() { return id; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public double getFlashSalePrice() { return flashSalePrice; }
    public void setFlashSalePrice(double flashSalePrice) { this.flashSalePrice = flashSalePrice; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}