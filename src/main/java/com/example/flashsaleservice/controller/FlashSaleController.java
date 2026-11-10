package com.example.flashsaleservice.controller;

import com.example.flashsaleservice.dto.FlashSaleOrderRequest;
import com.example.flashsaleservice.dto.FlashSaleSimulationResult;
import com.example.flashsaleservice.service.FlashSaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/v1/flashsale")
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    public FlashSaleController(FlashSaleService flashSaleService) {
        this.flashSaleService = flashSaleService;
    }

    // 1. API Đặt mua đơn lẻ cho từng khách hàng
    @PostMapping("/products/{id}/buy")
    public ResponseEntity<String> buySingleOrder(@PathVariable Long id, @RequestBody FlashSaleOrderRequest request) {
        String result = flashSaleService.buyProduct(id, request.getUserId(), request.getQuantity());
        return ResponseEntity.ok(result);
    }

    // 2. API Giả lập kịch bản Flash Sale: 100 người cùng bấm mua sản phẩm tại một thời điểm
    @PostMapping("/simulate-concurrency/{id}")
    public ResponseEntity<FlashSaleSimulationResult> simulateFlashSale(@PathVariable Long id) throws InterruptedException {
        int totalUsers = 100;
        ExecutorService executor = Executors.newFixedThreadPool(totalUsers);

        // CountDownLatch giúp tất cả 100 Thread chờ nhau và cùng xuất phát tại 1 thời điểm chính xác
        CountDownLatch readyLatch = new CountDownLatch(totalUsers);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalUsers);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger outOfStockCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= totalUsers; i++) {
            final String userId = "User-" + i;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await(); // Chờ hiệu lệnh xuất phát đồng loạt
                    String rs = flashSaleService.buyProduct(id, userId, 1);
                    if (rs.contains("thành công")) {
                        successCount.incrementAndGet();
                    } else if (rs.contains("hết hàng")) {
                        outOfStockCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Chờ tất cả 100 Thread sẵn sàng rồi mở cổng đồng loạt
        readyLatch.await();
        startLatch.countDown();

        // Chờ 100 Thread xử lý xong
        doneLatch.await();
        executor.shutdown();

        long duration = System.currentTimeMillis() - startTime;
        int currentStock = flashSaleService.getProductStock(id);

        FlashSaleSimulationResult summary = new FlashSaleSimulationResult(
                totalUsers,
                successCount.get(),
                outOfStockCount.get(),
                currentStock,
                duration
        );

        return ResponseEntity.ok(summary);
    }
}