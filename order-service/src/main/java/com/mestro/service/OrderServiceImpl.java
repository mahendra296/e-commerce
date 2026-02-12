package com.mestro.service;

import com.mestro.dto.OrderDTO;
import com.mestro.dto.OrderItemDTO;
import com.mestro.enums.ErrorCode;
import com.mestro.enums.OrderStatus;
import com.mestro.exceptions.BusinessException;
import com.mestro.exceptions.ResourceNotFoundException;
import com.mestro.model.Order;
import com.mestro.model.OrderItem;
import com.mestro.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        log.info("Creating new order for customer: {}", orderDTO.getCustomerId());

        // Validate order items
        if (orderDTO.getOrderItems() == null || orderDTO.getOrderItems().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Order must contain at least one item");
        }

        // Create order entity
        Order order = Order.builder()
                .customerId(orderDTO.getCustomerId())
                .status(OrderStatus.PENDING)
                .shippingAddress(orderDTO.getShippingAddress())
                .billingAddress(orderDTO.getBillingAddress())
                .notes(orderDTO.getNotes())
                .build();

        // Add order items
        for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .productId(itemDTO.getProductId())
                    .productName(itemDTO.getProductName())
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(itemDTO.getUnitPrice())
                    .build();
            order.addOrderItem(orderItem);
        }

        // Calculate total amount
        order.calculateTotalAmount();

        // Save order
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());

        return convertToDTO(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId) {
        log.info("Fetching order with ID: {}", orderId);
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.ORDER_NOT_FOUND, "Order not found with ID: " + orderId));
        return convertToDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        log.info("Fetching all orders");
        return orderRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByCustomerId(Long customerId) {
        log.info("Fetching orders for customer ID: {}", customerId);
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        log.info("Fetching orders with status: {}", status);
        return orderRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching orders between {} and {}", startDate, endDate);
        return orderRepository.findOrdersBetweenDates(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDTO updateOrder(Long orderId, OrderDTO orderDTO) {
        log.info("Updating order with ID: {}", orderId);

        Order existingOrder = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.ORDER_NOT_FOUND, "Order not found with ID: " + orderId));

        // Check if order can be updated
        if (existingOrder.getStatus() == OrderStatus.DELIVERED || existingOrder.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException(
                    ErrorCode.ORDER_CANNOT_BE_UPDATED, "Cannot update order with status: " + existingOrder.getStatus());
        }

        // Update order fields
        existingOrder.setShippingAddress(orderDTO.getShippingAddress());
        existingOrder.setBillingAddress(orderDTO.getBillingAddress());
        existingOrder.setNotes(orderDTO.getNotes());

        // Update order items if provided
        if (orderDTO.getOrderItems() != null && !orderDTO.getOrderItems().isEmpty()) {
            existingOrder.getOrderItems().clear();

            for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {
                OrderItem orderItem = OrderItem.builder()
                        .productId(itemDTO.getProductId())
                        .productName(itemDTO.getProductName())
                        .quantity(itemDTO.getQuantity())
                        .unitPrice(itemDTO.getUnitPrice())
                        .build();
                existingOrder.addOrderItem(orderItem);
            }

            existingOrder.calculateTotalAmount();
        }

        Order updatedOrder = orderRepository.save(existingOrder);
        log.info("Order updated successfully: {}", orderId);

        return convertToDTO(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus status) {
        log.info("Updating order status for ID: {} to {}", orderId, status);

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.ORDER_NOT_FOUND, "Order not found with ID: " + orderId));

        // Validate status transition
        validateStatusTransition(order.getStatus(), status);

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated successfully: {}", orderId);

        return convertToDTO(updatedOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        log.info("Deleting order with ID: {}", orderId);

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.ORDER_NOT_FOUND, "Order not found with ID: " + orderId));

        // Only allow deletion of pending orders
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CANCELLED) {
            throw new BusinessException(
                    ErrorCode.ORDER_CANNOT_BE_DELETED, "Can only delete orders with PENDING or CANCELLED status");
        }

        orderRepository.delete(order);
        log.info("Order deleted successfully: {}", orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getOrderCountByCustomerId(Long customerId) {
        log.info("Counting orders for customer ID: {}", customerId);
        return orderRepository.countByCustomerId(customerId);
    }

    // Helper methods
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = modelMapper.map(order, OrderDTO.class);

        if (order.getOrderItems() != null) {
            List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                    .map(item -> modelMapper.map(item, OrderItemDTO.class))
                    .collect(Collectors.toList());
            dto.setOrderItems(itemDTOs);
        }

        return dto;
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Add business logic for valid status transitions
        if (currentStatus == OrderStatus.DELIVERED || currentStatus == OrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS, "Cannot change status from " + currentStatus);
        }

        if (currentStatus == OrderStatus.SHIPPED && newStatus == OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS, "Cannot move from SHIPPED back to PENDING");
        }
    }
}
