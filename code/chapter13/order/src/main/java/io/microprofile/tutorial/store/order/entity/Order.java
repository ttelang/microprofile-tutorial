package io.microprofile.tutorial.store.order.entity;

import java.math.BigDecimal;

/**
 * Simplified Order entity reused from the tutorial's store application.
 */
public class Order {
    
    private Long orderId;
    private Long userId;
    private BigDecimal totalPrice;
    private OrderStatus status = OrderStatus.CREATED;
    
    public Order() {
    }
    
    public Order(Long orderId, Long userId, BigDecimal totalPrice, OrderStatus status) {
        this.orderId = orderId;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.status = status;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", userId=" + userId +
                ", totalPrice=" + totalPrice +
                ", status=" + status +
                '}';
    }
}
