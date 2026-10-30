package com.example.order_service.service;

import com.example.order_service.client.UserClient;
import com.example.order_service.entity.Order;
import com.example.order_service.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;

    public OrderService(OrderRepository orderRepository,UserClient userClient) {
        this.orderRepository = orderRepository;
        this.userClient = userClient;
    }

    public Order createOrder(Order order) {

        if (order.getUserId() == null
                || order.getUserId() <= 0
                || order.getProductName() == null
                || order.getProductName().isBlank()
                || order.getQuantity() == null
                || order.getQuantity() <= 0
                || order.getUnitPrice() == null
                || order.getUnitPrice().signum() < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provide a valid userId, productName, quantity and unitPrice"
            );
        }

        userClient.validateUser(order.getUserId());

        BigDecimal totalAmount = order.getUnitPrice()
                .multiply(BigDecimal.valueOf(order.getQuantity()));

        order.setTotalAmount(totalAmount);

        return orderRepository.save(order);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found with id: " + id
                ));
    }
}