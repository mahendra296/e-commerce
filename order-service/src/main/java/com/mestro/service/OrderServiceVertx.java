package com.mestro.service;

import com.mestro.client.ProductWebClient;
import com.mestro.common.dto.ApiResponse;
import com.mestro.common.dto.InventoryResponse;
import com.mestro.common.dto.PageResponseDTO;
import com.mestro.common.dto.ProductResponse;
import com.mestro.common.enums.CommonErrorCode;
import com.mestro.common.exception.BusinessException;
import com.mestro.common.exception.ResourceNotFoundException;
import com.mestro.common.utils.GeneralUtils;
import com.mestro.dto.OrderDTO;
import com.mestro.dto.OrderItemDTO;
import com.mestro.enums.OrderErrorCode;
import com.mestro.enums.OrderStatus;
import com.mestro.model.Order;
import com.mestro.model.OrderItem;
import com.mestro.repository.OrderRepository;
import io.vertx.core.Future;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceVertx {

    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final ProductWebClient productWebClient;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Transactional
    public Future<OrderDTO> createOrder(OrderDTO orderDTO) {
        log.info("Creating new order for customer: {}", orderDTO.getCustomerId());

        if (orderDTO.getOrderItems() == null || orderDTO.getOrderItems().isEmpty()) {
            return Future.failedFuture(
                    new BusinessException(CommonErrorCode.VALIDATION_ERROR, "Order must contain at least one item"));
        }

        // Validate all products + inventory first, then reserve, then persist
        return validateProductsAndInventory(orderDTO.getOrderItems()).compose(ignored -> {
            Order order = Order.builder()
                    .customerId(orderDTO.getCustomerId())
                    .status(OrderStatus.PENDING)
                    .shippingAddress(orderDTO.getShippingAddress())
                    .billingAddress(orderDTO.getBillingAddress())
                    .notes(orderDTO.getNotes())
                    .build();

            for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {
                order.addOrderItem(OrderItem.builder()
                        .productId(itemDTO.getProductId())
                        .warehouseId(itemDTO.getWarehouseId())
                        .productName(itemDTO.getProductName())
                        .quantity(itemDTO.getQuantity())
                        .unitPrice(itemDTO.getUnitPrice())
                        .build());
            }

            order.calculateTotalAmount();
            Order savedOrder = orderRepository.save(order);
            log.info("Order persisted with ID: {}", savedOrder.getId());

            // Reserve inventory then return the DTO
            return reserveInventoryForOrder(orderDTO.getOrderItems()).map(v -> {
                log.info("Order created successfully with ID: {}", savedOrder.getId());
                return convertToDTO(savedOrder);
            });
        });
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Future<OrderDTO> getOrderById(Long orderId) {
        log.info("Fetching order with ID: {}", orderId);
        return orderRepository
                .findById(orderId)
                .map(order -> Future.succeededFuture(convertToDTO(order)))
                .orElseGet(() -> Future.failedFuture(new ResourceNotFoundException(
                        OrderErrorCode.ORDER_NOT_FOUND, "Order not found with ID: " + orderId)));
    }

    @Transactional(readOnly = true)
    public Future<PageResponseDTO<OrderDTO>> getAllOrders(Pageable pageable) {
        log.info("Fetching all orders");
        Page<Order> pageOrders = orderRepository.findAll(pageable);
        List<OrderDTO> list =
                pageOrders.getContent().stream().map(this::convertToDTO).toList();
        return Future.succeededFuture(GeneralUtils.pageableResponse(
                list,
                pageOrders.getNumber(),
                pageOrders.getSize(),
                pageOrders.getTotalElements(),
                pageOrders.getTotalPages(),
                pageOrders.isFirst(),
                pageOrders.isLast(),
                pageable));
    }

    @Transactional(readOnly = true)
    public Future<PageResponseDTO<OrderDTO>> getOrdersByCustomerId(Long customerId, Pageable pageable) {
        log.info("Fetching orders for customer ID: {}", customerId);
        Page<Order> pageOrders = orderRepository.findByCustomerId(customerId, pageable);
        List<OrderDTO> list =
                pageOrders.getContent().stream().map(this::convertToDTO).toList();
        return Future.succeededFuture(GeneralUtils.pageableResponse(
                list,
                pageOrders.getNumber(),
                pageOrders.getSize(),
                pageOrders.getTotalElements(),
                pageOrders.getTotalPages(),
                pageOrders.isFirst(),
                pageOrders.isLast(),
                pageable));
    }

    @Transactional(readOnly = true)
    public Future<List<OrderDTO>> getOrdersByStatus(OrderStatus status) {
        log.info("Fetching orders with status: {}", status);
        List<OrderDTO> list = orderRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return Future.succeededFuture(list);
    }

    @Transactional(readOnly = true)
    public Future<List<OrderDTO>> getOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching orders between {} and {}", startDate, endDate);
        List<OrderDTO> list = orderRepository.findOrdersBetweenDates(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return Future.succeededFuture(list);
    }

    @Transactional(readOnly = true)
    public Future<Long> getOrderCountByCustomerId(Long customerId) {
        log.info("Counting orders for customer ID: {}", customerId);
        return Future.succeededFuture(orderRepository.countByCustomerId(customerId));
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Transactional
    public Future<OrderDTO> updateOrder(Long orderId, OrderDTO orderDTO) {
        log.info("Updating order with ID: {}", orderId);

        Order existingOrder;
        try {
            existingOrder = orderRepository
                    .findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            OrderErrorCode.ORDER_NOT_FOUND, "Order not found with ID: " + orderId));
        } catch (ResourceNotFoundException e) {
            return Future.failedFuture(e);
        }

        if (existingOrder.getStatus() == OrderStatus.DELIVERED || existingOrder.getStatus() == OrderStatus.CANCELLED) {
            return Future.failedFuture(new BusinessException(
                    OrderErrorCode.ORDER_CANNOT_BE_UPDATED,
                    "Cannot update order with status: " + existingOrder.getStatus()));
        }

        existingOrder.setShippingAddress(orderDTO.getShippingAddress());
        existingOrder.setBillingAddress(orderDTO.getBillingAddress());
        existingOrder.setNotes(orderDTO.getNotes());

        if (orderDTO.getOrderItems() != null && !orderDTO.getOrderItems().isEmpty()) {
            existingOrder.getOrderItems().clear();
            for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {
                existingOrder.addOrderItem(OrderItem.builder()
                        .productId(itemDTO.getProductId())
                        .warehouseId(itemDTO.getWarehouseId())
                        .productName(itemDTO.getProductName())
                        .quantity(itemDTO.getQuantity())
                        .unitPrice(itemDTO.getUnitPrice())
                        .build());
            }
            existingOrder.calculateTotalAmount();
        }

        Order updatedOrder = orderRepository.save(existingOrder);
        log.info("Order updated successfully: {}", orderId);
        return Future.succeededFuture(convertToDTO(updatedOrder));
    }

    @Transactional
    public Future<OrderDTO> updateOrderStatus(Long orderId, OrderStatus status) {
        log.info("Updating order status for ID: {} to {}", orderId, status);

        Order order;
        try {
            order = orderRepository
                    .findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            OrderErrorCode.ORDER_NOT_FOUND, "Order not found with ID: " + orderId));
        } catch (ResourceNotFoundException e) {
            return Future.failedFuture(e);
        }

        try {
            validateStatusTransition(order.getStatus(), status);
        } catch (BusinessException e) {
            return Future.failedFuture(e);
        }

        if (status == OrderStatus.CANCELLED) {
            // Release inventory async, then update status
            return releaseInventoryForOrder(order).compose(ignored -> {
                order.setStatus(status);
                Order updated = orderRepository.save(order);
                log.info("Order status updated to CANCELLED: {}", orderId);
                return Future.succeededFuture(convertToDTO(updated));
            });
        }

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated successfully: {}", orderId);
        return Future.succeededFuture(convertToDTO(updatedOrder));
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @Transactional
    public Future<Void> deleteOrder(Long orderId) {
        log.info("Deleting order with ID: {}", orderId);

        Order order;
        try {
            order = orderRepository
                    .findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            OrderErrorCode.ORDER_NOT_FOUND, "Order not found with ID: " + orderId));
        } catch (ResourceNotFoundException e) {
            return Future.failedFuture(e);
        }

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CANCELLED) {
            return Future.failedFuture(new BusinessException(
                    OrderErrorCode.ORDER_CANNOT_BE_DELETED, "Can only delete orders with PENDING or CANCELLED status"));
        }

        orderRepository.delete(order);
        log.info("Order deleted successfully: {}", orderId);
        return Future.succeededFuture();
    }

    // -------------------------------------------------------------------------
    // Inventory – validation (async, sequential per item)
    // -------------------------------------------------------------------------

    /**
     * Validates all items sequentially using Future.compose chaining.
     * Fails fast on the first invalid product or insufficient stock.
     */
    private Future<Void> validateProductsAndInventory(List<OrderItemDTO> orderItems) {
        Future<Void> chain = Future.succeededFuture();

        for (OrderItemDTO item : orderItems) {
            chain = chain
                    // Step 1: validate product exists and is active
                    .compose(ignored -> productWebClient
                            .getProductById(item.getProductId())
                            .compose(response -> {
                                if (!response.isSuccess() || response.getData() == null) {
                                    return Future.failedFuture(new BusinessException(
                                            CommonErrorCode.VALIDATION_ERROR,
                                            "Product not found with ID: " + item.getProductId()));
                                }
                                ProductResponse product = response.getData();
                                if (!product.getIsActive()) {
                                    return Future.failedFuture(new BusinessException(
                                            CommonErrorCode.VALIDATION_ERROR,
                                            "Product is not active: " + product.getName()));
                                }
                                if (item.getProductName() == null
                                        || item.getProductName().isBlank()) {
                                    item.setProductName(product.getName());
                                }
                                return Future.<Void>succeededFuture();
                            })
                            .recover(err -> {
                                if (err instanceof BusinessException) return Future.failedFuture(err);
                                log.error("Error validating product ID: {}", item.getProductId(), err);
                                return Future.failedFuture(new BusinessException(
                                        CommonErrorCode.VALIDATION_ERROR,
                                        "Unable to validate product with ID: " + item.getProductId()
                                                + ". Product service may be unavailable."));
                            }))
                    // Step 2: check inventory
                    .compose(ignored -> {
                        Future<Void> inventoryCheck;
                        if (item.getWarehouseId() != null) {
                            inventoryCheck = productWebClient
                                    .getInventoryByProductAndWarehouse(item.getProductId(), item.getWarehouseId())
                                    .compose(inv -> checkWarehouseStock(inv, item));
                        } else {
                            inventoryCheck = productWebClient
                                    .getTotalAvailableQuantity(item.getProductId())
                                    .compose(inv -> checkTotalStock(inv, item));
                        }
                        return inventoryCheck.recover(err -> {
                            if (err instanceof BusinessException) return Future.failedFuture(err);
                            log.error("Error checking inventory for product ID: {}", item.getProductId(), err);
                            return Future.failedFuture(new BusinessException(
                                    CommonErrorCode.VALIDATION_ERROR,
                                    "Unable to check inventory for product ID: " + item.getProductId()
                                            + ". Product service may be unavailable."));
                        });
                    });
        }

        return chain;
    }

    private Future<Void> checkWarehouseStock(ApiResponse<InventoryResponse> response, OrderItemDTO item) {
        if (response.isSuccess() && response.getData() != null) {
            int available = response.getData().getQuantityAvailable();
            if (available < item.getQuantity()) {
                return Future.failedFuture(new BusinessException(
                        CommonErrorCode.VALIDATION_ERROR,
                        "Insufficient stock for product ID: " + item.getProductId()
                                + " at warehouse ID: " + item.getWarehouseId()
                                + ". Available: " + available + ", Requested: " + item.getQuantity()));
            }
        }
        return Future.succeededFuture();
    }

    private Future<Void> checkTotalStock(ApiResponse<Integer> response, OrderItemDTO item) {
        if (response.isSuccess() && response.getData() != null) {
            int available = response.getData();
            if (available < item.getQuantity()) {
                return Future.failedFuture(new BusinessException(
                        CommonErrorCode.VALIDATION_ERROR,
                        "Insufficient stock for product ID: " + item.getProductId() + ". Available: " + available
                                + ", Requested: " + item.getQuantity()));
            }
        }
        return Future.succeededFuture();
    }

    // -------------------------------------------------------------------------
    // Inventory – reserve (async, with rollback on failure)
    // -------------------------------------------------------------------------

    private Future<Void> reserveInventoryForOrder(List<OrderItemDTO> orderItems) {
        List<OrderItemDTO> reserved = new ArrayList<>();
        Future<Void> chain = Future.succeededFuture();

        for (OrderItemDTO item : orderItems) {
            chain = chain.compose(ignored -> {
                Future<ApiResponse<InventoryResponse>> reserveFuture = item.getWarehouseId() != null
                        ? productWebClient.reserveByProductAndWarehouse(
                                item.getProductId(), item.getWarehouseId(), item.getQuantity())
                        : productWebClient.reserveByProductId(item.getProductId(), item.getQuantity());

                return reserveFuture
                        .onSuccess(v -> {
                            reserved.add(item);
                            log.info(
                                    "Inventory reserved for product ID: {}, warehouse ID: {}, quantity: {}",
                                    item.getProductId(),
                                    item.getWarehouseId(),
                                    item.getQuantity());
                        })
                        .mapEmpty(); // Future<ApiResponse<...>> → Future<Void>
            });
        }

        // On any reservation failure, rollback all already-reserved items
        return chain.recover(err -> {
            log.error("Error reserving inventory, rolling back {} reservations", reserved.size(), err);
            return rollbackReservations(reserved)
                    .transform(v -> Future.failedFuture(new BusinessException(
                            CommonErrorCode.INTERNAL_SERVER_ERROR,
                            "Failed to reserve inventory: " + err.getMessage())));
        });
    }

    private Future<Object> rollbackReservations(List<OrderItemDTO> reserved) {
        Future<Object> rollback = Future.succeededFuture();
        for (OrderItemDTO item : reserved) {
            rollback = rollback.compose(ignored -> {
                Future<ApiResponse<InventoryResponse>> releaseFuture = item.getWarehouseId() != null
                        ? productWebClient.releaseByProductAndWarehouse(
                                item.getProductId(), item.getWarehouseId(), item.getQuantity())
                        : productWebClient.releaseByProductId(item.getProductId(), item.getQuantity());

                return releaseFuture.mapEmpty().recover(rollbackErr -> {
                    log.error(
                            "Failed to rollback reservation for product ID: {}, warehouse ID: {}",
                            item.getProductId(),
                            item.getWarehouseId(),
                            rollbackErr);
                    return Future.succeededFuture(); // best-effort, don't fail the chain
                });
            });
        }
        return rollback;
    }

    // -------------------------------------------------------------------------
    // Inventory – release on cancellation (async, best-effort per item)
    // -------------------------------------------------------------------------

    private Future<Object> releaseInventoryForOrder(Order order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return Future.succeededFuture();
        }

        Future<Object> chain = Future.succeededFuture();
        for (OrderItem item : order.getOrderItems()) {
            chain = chain.compose(ignored -> {
                Future<ApiResponse<InventoryResponse>> releaseFuture = item.getWarehouseId() != null
                        ? productWebClient.releaseByProductAndWarehouse(
                                item.getProductId(), item.getWarehouseId(), item.getQuantity())
                        : productWebClient.releaseByProductId(item.getProductId(), item.getQuantity());

                return releaseFuture
                        .onSuccess(v -> log.info(
                                "Inventory released for product ID: {}, warehouse ID: {}, quantity: {}",
                                item.getProductId(),
                                item.getWarehouseId(),
                                item.getQuantity()))
                        .mapEmpty()
                        .recover(err -> {
                            // Log but do not fail — manual intervention handles edge cases
                            log.error(
                                    "Failed to release inventory for product ID: {}, warehouse ID: {}."
                                            + " Manual intervention may be required.",
                                    item.getProductId(),
                                    item.getWarehouseId(),
                                    err);
                            return Future.succeededFuture();
                        });
            });
        }
        return chain;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

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
