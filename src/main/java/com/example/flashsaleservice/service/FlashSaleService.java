package com.example.flashsaleservice.service;

import com.example.flashsaleservice.entity.FlashSaleProduct;
import com.example.flashsaleservice.repository.FlashSaleProductRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class FlashSaleService {

    private static final Logger log = LoggerFactory.getLogger(FlashSaleService.class);

    private final FlashSaleProductRepository productRepository;
    private final RedissonClient redissonClient;

    public FlashSaleService(FlashSaleProductRepository productRepository, RedissonClient redissonClient) {
        this.productRepository = productRepository;
        this.redissonClient = redissonClient;
    }

    public String buyProduct(Long productId, String userId, int quantity) {
        // Yêu cầu kỹ thuật 1: Tạo khóa phân tán theo key lock:product:{id}
        String lockKey = "lock:product:" + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // Yêu cầu kỹ thuật 2: Phải giành được khóa mới kiểm tra tồn kho và trừ số lượng
            // Chờ tối đa 3 giây để lấy khóa, giữ khóa tối đa 5 giây
            boolean isLocked = lock.tryLock(3, 5, TimeUnit.SECONDS);

            if (!isLocked) {
                return "Hệ thống đang quá tải, vui lòng thử lại!";
            }

            // Đọc dữ liệu mới nhất từ Database
            FlashSaleProduct product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                return "Sản phẩm không tồn tại!";
            }

            // Kiểm tra tồn kho thực tế
            if (product.getStock() >= quantity) {
                // Trừ tồn kho và lưu lại DB
                product.setStock(product.getStock() - quantity);
                productRepository.save(product);

                log.info(">>> [User: {}] MUA THÀNH CÔNG! Tồn kho còn lại: {}", userId, product.getStock());
                return "Đặt hàng thành công sản phẩm: " + product.getProductName();
            } else {
                log.warn(">>> [User: {}] Sản phẩm đã hết hàng!", userId);
                return "Sản phẩm đã hết hàng";
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Giao dịch bị gián đoạn!";
        } finally {
            // Yêu cầu kỹ thuật 3: Đảm bảo giải phóng khóa (Unlock) trong khối finally
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public int getProductStock(Long productId) {
        return productRepository.findById(productId)
                .map(FlashSaleProduct::getStock)
                .orElse(0);
    }
}