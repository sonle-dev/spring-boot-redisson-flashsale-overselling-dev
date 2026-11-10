package com.example.flashsaleservice.runner;

import com.example.flashsaleservice.entity.FlashSaleProduct;
import com.example.flashsaleservice.repository.FlashSaleProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FlashSaleDataSeeder implements CommandLineRunner {

    private final FlashSaleProductRepository productRepository;

    public FlashSaleDataSeeder(FlashSaleProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        FlashSaleProduct product = productRepository.findById(1L).orElse(null);
        if (product == null) {
            product = new FlashSaleProduct(
                    "Dược phẩm quý đông trùng hạ thảo (Flash Sale 1k)",
                    1000.0,
                    1 // Tồn kho ban đầu duy nhất: 1 món
            );
        } else {
            product.setStock(1); // Reset lại stock về 1 mỗi lần restart để test
        }
        productRepository.save(product);
        System.out.println(">>> [Seeder] Đã chuẩn bị sản phẩm Flash Sale (ID: 1) với tồn kho duy nhất: 1 món.");
    }
}