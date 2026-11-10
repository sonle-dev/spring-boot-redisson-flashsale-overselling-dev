package com.example.flashsaleservice.dto;

public class FlashSaleSimulationResult {
    private int totalRequests;
    private int successOrders;
    private int outOfStockOrders;
    private int finalStockInDb;
    private long executionTimeMs;

    public FlashSaleSimulationResult() {}

    public FlashSaleSimulationResult(int totalRequests, int successOrders, int outOfStockOrders, int finalStockInDb, long executionTimeMs) {
        this.totalRequests = totalRequests;
        this.successOrders = successOrders;
        this.outOfStockOrders = outOfStockOrders;
        this.finalStockInDb = finalStockInDb;
        this.executionTimeMs = executionTimeMs;
    }

    public int getTotalRequests() { return totalRequests; }
    public int getSuccessOrders() { return successOrders; }
    public int getOutOfStockOrders() { return outOfStockOrders; }
    public int getFinalStockInDb() { return finalStockInDb; }
    public long getExecutionTimeMs() { return executionTimeMs; }
}