package com.mestro.service;

import com.mestro.dto.OrderDTO;
import com.mestro.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    OrderDTO createOrder(OrderDTO orderDTO);

    OrderDTO getOrderById(Long orderId);

    List<OrderDTO> getAllOrders();

    List<OrderDTO> getOrdersByCustomerId(Long customerId);

    List<OrderDTO> getOrdersByStatus(OrderStatus status);

    List<OrderDTO> getOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    OrderDTO updateOrder(Long orderId, OrderDTO orderDTO);

    OrderDTO updateOrderStatus(Long orderId, OrderStatus status);

    void deleteOrder(Long orderId);

    Long getOrderCountByCustomerId(Long customerId);
}
