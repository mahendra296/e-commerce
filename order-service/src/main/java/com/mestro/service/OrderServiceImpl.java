package com.mestro.service;

import com.mestro.common.client.ProductServiceClient;
import com.mestro.common.dto.ApiResponse;
import com.mestro.common.dto.InventoryResponse;
import com.mestro.common.dto.ProductResponse;
import com.mestro.common.enums.CommonErrorCode;
import com.mestro.common.exception.BusinessException;
import com.mestro.common.exception.ResourceNotFoundException;
import com.mestro.dto.OrderDTO;
import com.mestro.dto.OrderItemDTO;
import com.mestro.enums.OrderErrorCode;
import com.mestro.enums.OrderStatus;
import com.mestro.model.Order;
import com.mestro.model.OrderItem;
import com.mestro.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final ProductServiceClient productServiceClient;

    @Override
    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        log.info("Creating new order for customer: {}", orderDTO.getCustomerId());

        // Validate order items
        if (orderDTO.getOrderItems() == null || orderDTO.getOrderItems().isEmpty()) {
            throw new BusinessException(CommonErrorCode.VALIDATION_ERROR, "Order must contain at least one item");
        }

        // Validate products and check inventory via Feign
        validateProductsAndInventory(orderDTO.getOrderItems());

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
                    .warehouseId(itemDTO.getWarehouseId())
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

        // Reserve inventory for each order item
        reserveInventoryForOrder(orderDTO.getOrderItems());

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
                        OrderErrorCode.ORDER_NOT_FOUND, "Order not found with ID: " + orderId));
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
                        OrderErrorCode.ORDER_NOT_FOUND, "Order not found with ID: " + orderId));

        // Check if order can be updated
        if (existingOrder.getStatus() == OrderStatus.DELIVERED || existingOrder.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException(
                    OrderErrorCode.ORDER_CANNOT_BE_UPDATED,
                    "Cannot update order with status: " + existingOrder.getStatus());
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
                        .warehouseId(itemDTO.getWarehouseId())
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
                        OrderErrorCode.ORDER_NOT_FOUND, "Order not found with ID: " + orderId));

        // Validate status transition
        validateStatusTransition(order.getStatus(), status);

        // Release inventory when order is cancelled
        if (status == OrderStatus.CANCELLED) {
            releaseInventoryForOrder(order);
        }

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
                        OrderErrorCode.ORDER_NOT_FOUND, "Order not found with ID: " + orderId));

        // Only allow deletion of pending orders
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CANCELLED) {
            throw new BusinessException(
                    OrderErrorCode.ORDER_CANNOT_BE_DELETED, "Can only delete orders with PENDING or CANCELLED status");
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

    // Feign integration methods
    private void validateProductsAndInventory(List<OrderItemDTO> orderItems) {
        for (OrderItemDTO item : orderItems) {
            // Validate product exists
            try {
                ApiResponse<ProductResponse> response = productServiceClient.getProductById(item.getProductId());
                if (!response.isSuccess() || response.getData() == null) {
                    throw new BusinessException(
                            CommonErrorCode.VALIDATION_ERROR, "Product not found with ID: " + item.getProductId());
                }

                ProductResponse product = response.getData();
                if (!product.getIsActive()) {
                    throw new BusinessException(
                            CommonErrorCode.VALIDATION_ERROR, "Product is not active: " + product.getName());
                }

                // Set product name from product service if not provided
                if (item.getProductName() == null || item.getProductName().isBlank()) {
                    item.setProductName(product.getName());
                }
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.error("Error validating product ID: {}", item.getProductId(), e);
                throw new BusinessException(
                        CommonErrorCode.VALIDATION_ERROR,
                        "Unable to validate product with ID: " + item.getProductId()
                                + ". Product service may be unavailable.");
            }

            // Check inventory availability
            try {
                if (item.getWarehouseId() != null) {
                    // Check stock at specific warehouse
                    ApiResponse<InventoryResponse> inventoryResponse =
                            productServiceClient.getInventoryByProductAndWarehouse(
                                    item.getProductId(), item.getWarehouseId());
                    if (inventoryResponse.isSuccess() && inventoryResponse.getData() != null) {
                        int available = inventoryResponse.getData().getQuantityAvailable();
                        if (available < item.getQuantity()) {
                            throw new BusinessException(
                                    CommonErrorCode.VALIDATION_ERROR,
                                    "Insufficient stock for product ID: " + item.getProductId()
                                            + " at warehouse ID: " + item.getWarehouseId()
                                            + ". Available: " + available + ", Requested: " + item.getQuantity());
                        }
                    }
                } else {
                    // Check total stock across all warehouses
                    ApiResponse<Integer> inventoryResponse =
                            productServiceClient.getTotalAvailableQuantity(item.getProductId());
                    if (inventoryResponse.isSuccess() && inventoryResponse.getData() != null) {
                        int available = inventoryResponse.getData();
                        if (available < item.getQuantity()) {
                            throw new BusinessException(
                                    CommonErrorCode.VALIDATION_ERROR,
                                    "Insufficient stock for product ID: " + item.getProductId() + ". Available: "
                                            + available + ", Requested: " + item.getQuantity());
                        }
                    }
                }
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.error("Error checking inventory for product ID: {}", item.getProductId(), e);
                throw new BusinessException(
                        CommonErrorCode.VALIDATION_ERROR,
                        "Unable to check inventory for product ID: " + item.getProductId()
                                + ". Product service may be unavailable.");
            }
        }
    }

    private void reserveInventoryForOrder(List<OrderItemDTO> orderItems) {
        List<OrderItemDTO> reserved = new ArrayList<>();
        try {
            for (OrderItemDTO item : orderItems) {
                if (item.getWarehouseId() != null) {
                    productServiceClient.reserveByProductAndWarehouse(
                            item.getProductId(), item.getWarehouseId(), item.getQuantity());
                } else {
                    productServiceClient.reserveByProductId(item.getProductId(), item.getQuantity());
                }
                reserved.add(item);
                log.info(
                        "Inventory reserved for product ID: {}, warehouse ID: {}, quantity: {}",
                        item.getProductId(), item.getWarehouseId(), item.getQuantity());
            }
        } catch (Exception e) {
            // Rollback already reserved inventory
            log.error("Error reserving inventory, rolling back reservations", e);
            for (OrderItemDTO reservedItem : reserved) {
                try {
                    if (reservedItem.getWarehouseId() != null) {
                        productServiceClient.releaseByProductAndWarehouse(
                                reservedItem.getProductId(), reservedItem.getWarehouseId(), reservedItem.getQuantity());
                    } else {
                        productServiceClient.releaseByProductId(reservedItem.getProductId(), reservedItem.getQuantity());
                    }
                } catch (Exception rollbackEx) {
                    log.error(
                            "Failed to rollback inventory reservation for product ID: {}, warehouse ID: {}",
                            reservedItem.getProductId(), reservedItem.getWarehouseId(),
                            rollbackEx);
                }
            }
            throw new BusinessException(
                    CommonErrorCode.INTERNAL_SERVER_ERROR, "Failed to reserve inventory: " + e.getMessage());
        }
    }

    private void releaseInventoryForOrder(Order order) {
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                try {
                    if (item.getWarehouseId() != null) {
                        productServiceClient.releaseByProductAndWarehouse(
                                item.getProductId(), item.getWarehouseId(), item.getQuantity());
                    } else {
                        productServiceClient.releaseByProductId(item.getProductId(), item.getQuantity());
                    }
                    log.info(
                            "Inventory released for product ID: {}, warehouse ID: {}, quantity: {}",
                            item.getProductId(), item.getWarehouseId(),
                            item.getQuantity());
                } catch (Exception e) {
                    log.error(
                            "Failed to release inventory for product ID: {}, warehouse ID: {}. Manual intervention may be required.",
                            item.getProductId(), item.getWarehouseId(),
                            e);
                }
            }
        }
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
            throw new BusinessException(
                    OrderErrorCode.INVALID_ORDER_STATUS, "Cannot change status from " + currentStatus);
        }

        if (currentStatus == OrderStatus.SHIPPED && newStatus == OrderStatus.PENDING) {
            throw new BusinessException(
                    OrderErrorCode.INVALID_ORDER_STATUS, "Cannot move from SHIPPED back to PENDING");
        }
    }
}
