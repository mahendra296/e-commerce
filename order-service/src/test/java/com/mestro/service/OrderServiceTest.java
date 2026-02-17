package com.mestro.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mestro.common.client.ProductServiceClient;
import com.mestro.common.dto.ApiResponse;
import com.mestro.common.dto.InventoryResponse;
import com.mestro.common.dto.PageResponseDTO;
import com.mestro.common.dto.ProductResponse;
import com.mestro.common.exception.BusinessException;
import com.mestro.common.exception.ResourceNotFoundException;
import com.mestro.dto.OrderDTO;
import com.mestro.dto.OrderItemDTO;
import com.mestro.enums.OrderStatus;
import com.mestro.model.Order;
import com.mestro.model.OrderItem;
import com.mestro.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private OrderService orderService;

    // ── Fixtures ────────────────────────────────────────────────────────────
    private OrderItemDTO sampleItemDTO;
    private OrderDTO sampleOrderDTO;
    private Order sampleOrder;
    private OrderItem sampleOrderItem;
    private ProductResponse activeProduct;

    @BeforeEach
    void setUp() {
        sampleItemDTO = OrderItemDTO.builder()
                .productId(101L)
                .warehouseId(1L)
                .productName("Laptop Pro X")
                .quantity(2)
                .unitPrice(new BigDecimal("999.99"))
                .build();

        sampleOrderDTO = OrderDTO.builder()
                .customerId(500L)
                .status(OrderStatus.PENDING)
                .shippingAddress("123 Main St")
                .billingAddress("123 Main St")
                .notes("Fragile")
                .orderItems(List.of(sampleItemDTO))
                .build();

        sampleOrderItem = OrderItem.builder()
                .id(1L)
                .productId(101L)
                .warehouseId(1L)
                .productName("Laptop Pro X")
                .quantity(2)
                .unitPrice(new BigDecimal("999.99"))
                .build();

        sampleOrder = Order.builder()
                .id(1L)
                .customerId(500L)
                .status(OrderStatus.PENDING)
                .shippingAddress("123 Main St")
                .billingAddress("123 Main St")
                .notes("Fragile")
                .orderItems(new ArrayList<>(List.of(sampleOrderItem)))
                .totalAmount(new BigDecimal("1999.98"))
                .build();

        activeProduct = ProductResponse.builder()
                .id(101L)
                .name("Laptop Pro X")
                .isActive(true)
                .build();
    }

    // ─────────────────────────────────────────────
    // createOrder
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("createOrder()")
    class CreateOrderTests {

        @Test
        @DisplayName("Should save order and reserve inventory when all validations pass")
        void createOrder_ValidRequest_ReturnsSavedOrder() {
            // Arrange — product & inventory stubs
            ApiResponse<ProductResponse> productResp = ApiResponse.success("ok", activeProduct);
            InventoryResponse inventoryResp =
                    InventoryResponse.builder().quantityAvailable(10).build();
            ApiResponse<InventoryResponse> invResp = ApiResponse.success("ok", inventoryResp);

            when(productServiceClient.getProductById(101L)).thenReturn(productResp);
            when(productServiceClient.getInventoryByProductAndWarehouse(101L, 1L))
                    .thenReturn(invResp);
            when(productServiceClient.reserveByProductAndWarehouse(101L, 1L, 2))
                    .thenReturn(ApiResponse.success("ok", inventoryResp));
            when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

            OrderDTO expectedDTO = OrderDTO.builder().id(1L).customerId(500L).build();
            when(modelMapper.map(sampleOrder, OrderDTO.class)).thenReturn(expectedDTO);
            when(modelMapper.map(any(OrderItem.class), eq(OrderItemDTO.class))).thenReturn(sampleItemDTO);

            // Act
            OrderDTO result = orderService.createOrder(sampleOrderDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(orderRepository, times(1)).save(any(Order.class));
            verify(productServiceClient, times(1)).reserveByProductAndWarehouse(101L, 1L, 2);
        }

        @Test
        @DisplayName("Should throw BusinessException when orderItems list is null")
        void createOrder_NullOrderItems_ThrowsBusinessException() {
            OrderDTO orderWithNoItems =
                    OrderDTO.builder().customerId(500L).orderItems(null).build();

            assertThatThrownBy(() -> orderService.createOrder(orderWithNoItems))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Order must contain at least one item");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessException when orderItems list is empty")
        void createOrder_EmptyOrderItems_ThrowsBusinessException() {
            OrderDTO orderWithNoItems =
                    OrderDTO.builder().customerId(500L).orderItems(List.of()).build();

            assertThatThrownBy(() -> orderService.createOrder(orderWithNoItems))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Order must contain at least one item");
        }

        @Test
        @DisplayName("Should throw BusinessException when product is not active")
        void createOrder_InactiveProduct_ThrowsBusinessException() {
            ProductResponse inactiveProduct = ProductResponse.builder()
                    .id(101L)
                    .name("Old Widget")
                    .isActive(false)
                    .build();

            when(productServiceClient.getProductById(101L)).thenReturn(ApiResponse.success("ok", inactiveProduct));

            assertThatThrownBy(() -> orderService.createOrder(sampleOrderDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Product is not active");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessException when product service is unavailable")
        void createOrder_ProductServiceDown_ThrowsBusinessException() {
            when(productServiceClient.getProductById(101L)).thenThrow(new RuntimeException("Connection refused"));

            assertThatThrownBy(() -> orderService.createOrder(sampleOrderDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Unable to validate product");
        }

        @Test
        @DisplayName("Should throw BusinessException when inventory is insufficient")
        void createOrder_InsufficientInventory_ThrowsBusinessException() {
            InventoryResponse lowStock =
                    InventoryResponse.builder().quantityAvailable(1).build();
            when(productServiceClient.getProductById(101L)).thenReturn(ApiResponse.success("ok", activeProduct));
            when(productServiceClient.getInventoryByProductAndWarehouse(101L, 1L))
                    .thenReturn(ApiResponse.success("ok", lowStock));

            assertThatThrownBy(() -> orderService.createOrder(sampleOrderDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Insufficient stock");
        }

        @Test
        @DisplayName("Should rollback inventory reservations when reservation fails mid-way")
        void createOrder_InventoryReservationFails_RollsBack() {
            // Two items; second reservation fails
            OrderItemDTO item1 = OrderItemDTO.builder()
                    .productId(101L)
                    .warehouseId(1L)
                    .quantity(1)
                    .unitPrice(BigDecimal.TEN)
                    .productName("P1")
                    .build();
            OrderItemDTO item2 = OrderItemDTO.builder()
                    .productId(102L)
                    .warehouseId(1L)
                    .quantity(1)
                    .unitPrice(BigDecimal.TEN)
                    .productName("P2")
                    .build();

            OrderDTO twoItemOrder = OrderDTO.builder()
                    .customerId(500L)
                    .orderItems(List.of(item1, item2))
                    .build();

            ProductResponse p2 =
                    ProductResponse.builder().id(102L).name("P2").isActive(true).build();
            InventoryResponse inv =
                    InventoryResponse.builder().quantityAvailable(10).build();

            when(productServiceClient.getProductById(101L)).thenReturn(ApiResponse.success("ok", activeProduct));
            when(productServiceClient.getProductById(102L)).thenReturn(ApiResponse.success("ok", p2));
            when(productServiceClient.getInventoryByProductAndWarehouse(anyLong(), anyLong()))
                    .thenReturn(ApiResponse.success("ok", inv));

            // First reserve succeeds; second throws
            when(productServiceClient.reserveByProductAndWarehouse(101L, 1L, 1))
                    .thenReturn(ApiResponse.success("ok", inv));
            when(productServiceClient.reserveByProductAndWarehouse(102L, 1L, 1))
                    .thenThrow(new RuntimeException("Inventory service error"));
            when(productServiceClient.releaseByProductAndWarehouse(101L, 1L, 1))
                    .thenReturn(ApiResponse.success("ok", inv));

            Order savedOrder = Order.builder()
                    .id(1L)
                    .customerId(500L)
                    .orderItems(new ArrayList<>())
                    .build();
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            assertThatThrownBy(() -> orderService.createOrder(twoItemOrder))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Failed to reserve inventory");

            // Verify rollback was attempted for item1
            verify(productServiceClient, times(1)).releaseByProductAndWarehouse(101L, 1L, 1);
        }

        @Test
        @DisplayName("Should use total stock check when warehouseId is null")
        void createOrder_NoWarehouseId_ChecksTotalStock() {
            OrderItemDTO noWarehouseItem = OrderItemDTO.builder()
                    .productId(101L)
                    .warehouseId(null)
                    .quantity(2)
                    .unitPrice(BigDecimal.TEN)
                    .productName("Laptop")
                    .build();

            OrderDTO order = OrderDTO.builder()
                    .customerId(500L)
                    .orderItems(List.of(noWarehouseItem))
                    .build();

            when(productServiceClient.getProductById(101L)).thenReturn(ApiResponse.success("ok", activeProduct));
            when(productServiceClient.getTotalAvailableQuantity(101L)).thenReturn(ApiResponse.success("ok", 10));
            when(productServiceClient.reserveByProductId(101L, 2))
                    .thenReturn(ApiResponse.success(
                            "ok", InventoryResponse.builder().build()));
            when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
            when(modelMapper.map(sampleOrder, OrderDTO.class)).thenReturn(sampleOrderDTO);
            when(modelMapper.map(any(OrderItem.class), eq(OrderItemDTO.class))).thenReturn(noWarehouseItem);

            OrderDTO result = orderService.createOrder(order);

            assertThat(result).isNotNull();
            verify(productServiceClient, times(1)).getTotalAvailableQuantity(101L);
            verify(productServiceClient, times(1)).reserveByProductId(101L, 2);
        }
    }

    // ─────────────────────────────────────────────
    // getOrderById
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("getOrderById()")
    class GetOrderByIdTests {

        @Test
        @DisplayName("Should return mapped DTO when order is found")
        void getOrderById_ExistingId_ReturnsOrderDTO() {
            OrderDTO expectedDTO = OrderDTO.builder().id(1L).customerId(500L).build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
            when(modelMapper.map(sampleOrder, OrderDTO.class)).thenReturn(expectedDTO);
            when(modelMapper.map(any(OrderItem.class), eq(OrderItemDTO.class))).thenReturn(sampleItemDTO);

            OrderDTO result = orderService.getOrderById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getCustomerId()).isEqualTo(500L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when order not found")
        void getOrderById_NonExistingId_ThrowsResourceNotFoundException() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Order not found with ID: 999");
        }
    }

    // ─────────────────────────────────────────────
    // getAllOrders
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("getAllOrders()")
    class GetAllOrdersTests {

        @Test
        @DisplayName("Should return paginated result with mapped content")
        void getAllOrders_ReturnsPageResponseDTO() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> orderPage = new PageImpl<>(List.of(sampleOrder), pageable, 1);

            when(orderRepository.findAll(pageable)).thenReturn(orderPage);
            when(modelMapper.map(sampleOrder, OrderDTO.class)).thenReturn(sampleOrderDTO);
            when(modelMapper.map(any(OrderItem.class), eq(OrderItemDTO.class))).thenReturn(sampleItemDTO);

            PageResponseDTO<OrderDTO> result = orderService.getAllOrders(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should return empty page when no orders exist")
        void getAllOrders_NoOrders_ReturnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(orderRepository.findAll(pageable)).thenReturn(emptyPage);

            PageResponseDTO<OrderDTO> result = orderService.getAllOrders(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    // ─────────────────────────────────────────────
    // getOrdersByCustomerId
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("getOrdersByCustomerId()")
    class GetOrdersByCustomerIdTests {

        @Test
        @DisplayName("Should return paginated orders for the given customer")
        void getOrdersByCustomerId_ValidCustomer_ReturnsPageResponse() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> page = new PageImpl<>(List.of(sampleOrder), pageable, 1);

            when(orderRepository.findByCustomerId(500L, pageable)).thenReturn(page);
            when(modelMapper.map(sampleOrder, OrderDTO.class)).thenReturn(sampleOrderDTO);
            when(modelMapper.map(any(OrderItem.class), eq(OrderItemDTO.class))).thenReturn(sampleItemDTO);

            PageResponseDTO<OrderDTO> result = orderService.getOrdersByCustomerId(500L, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCustomerId()).isEqualTo(500L);
        }
    }

    // ─────────────────────────────────────────────
    // getOrdersByStatus
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("getOrdersByStatus()")
    class GetOrdersByStatusTests {

        @Test
        @DisplayName("Should return list of orders with matching status")
        void getOrdersByStatus_ValidStatus_ReturnsList() {
            when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of(sampleOrder));
            when(modelMapper.map(sampleOrder, OrderDTO.class)).thenReturn(sampleOrderDTO);
            when(modelMapper.map(any(OrderItem.class), eq(OrderItemDTO.class))).thenReturn(sampleItemDTO);

            List<OrderDTO> result = orderService.getOrdersByStatus(OrderStatus.PENDING);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("Should return empty list when no orders with given status")
        void getOrdersByStatus_NoMatching_ReturnsEmptyList() {
            when(orderRepository.findByStatus(OrderStatus.CANCELLED)).thenReturn(List.of());

            List<OrderDTO> result = orderService.getOrdersByStatus(OrderStatus.CANCELLED);

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────
    // getOrdersBetweenDates
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("getOrdersBetweenDates()")
    class GetOrdersBetweenDatesTests {

        @Test
        @DisplayName("Should return orders within the date range")
        void getOrdersBetweenDates_ValidRange_ReturnsList() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59);

            when(orderRepository.findOrdersBetweenDates(start, end)).thenReturn(List.of(sampleOrder));
            when(modelMapper.map(sampleOrder, OrderDTO.class)).thenReturn(sampleOrderDTO);
            when(modelMapper.map(any(OrderItem.class), eq(OrderItemDTO.class))).thenReturn(sampleItemDTO);

            List<OrderDTO> result = orderService.getOrdersBetweenDates(start, end);

            assertThat(result).hasSize(1);
            verify(orderRepository, times(1)).findOrdersBetweenDates(start, end);
        }
    }

    // ─────────────────────────────────────────────
    // updateOrder
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("updateOrder()")
    class UpdateOrderTests {

        @Test
        @DisplayName("Should update and return order when status allows modification")
        void updateOrder_PendingOrder_UpdatesSuccessfully() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
            when(modelMapper.map(sampleOrder, OrderDTO.class)).thenReturn(sampleOrderDTO);
            when(modelMapper.map(any(OrderItem.class), eq(OrderItemDTO.class))).thenReturn(sampleItemDTO);

            OrderDTO updateRequest = OrderDTO.builder()
                    .customerId(500L)
                    .shippingAddress("New Address")
                    .orderItems(List.of(sampleItemDTO))
                    .build();

            OrderDTO result = orderService.updateOrder(1L, updateRequest);

            assertThat(result).isNotNull();
            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when order is DELIVERED")
        void updateOrder_DeliveredOrder_ThrowsBusinessException() {
            Order deliveredOrder = Order.builder()
                    .id(1L)
                    .customerId(500L)
                    .status(OrderStatus.DELIVERED)
                    .orderItems(new ArrayList<>())
                    .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(deliveredOrder));

            assertThatThrownBy(() -> orderService.updateOrder(1L, sampleOrderDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot update order with status");
        }

        @Test
        @DisplayName("Should throw BusinessException when order is CANCELLED")
        void updateOrder_CancelledOrder_ThrowsBusinessException() {
            Order cancelledOrder = Order.builder()
                    .id(1L)
                    .status(OrderStatus.CANCELLED)
                    .orderItems(new ArrayList<>())
                    .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(cancelledOrder));

            assertThatThrownBy(() -> orderService.updateOrder(1L, sampleOrderDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot update order with status");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when order ID does not exist")
        void updateOrder_NonExistingOrder_ThrowsResourceNotFoundException() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.updateOrder(999L, sampleOrderDTO))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────
    // updateOrderStatus
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("updateOrderStatus()")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("Should update status from PENDING to CONFIRMED")
        void updateOrderStatus_ValidTransition_UpdatesStatus() {
            Order confirmedOrder = Order.builder()
                    .id(1L)
                    .customerId(500L)
                    .status(OrderStatus.CONFIRMED)
                    .orderItems(new ArrayList<>(List.of(sampleOrderItem)))
                    .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(confirmedOrder);

            OrderDTO confirmedDTO =
                    OrderDTO.builder().id(1L).status(OrderStatus.CONFIRMED).build();
            when(modelMapper.map(confirmedOrder, OrderDTO.class)).thenReturn(confirmedDTO);
            when(modelMapper.map(any(OrderItem.class), eq(OrderItemDTO.class))).thenReturn(sampleItemDTO);

            OrderDTO result = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

            assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should release inventory when order is CANCELLED")
        void updateOrderStatus_CancelledStatus_ReleasesInventory() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
            when(productServiceClient.releaseByProductAndWarehouse(101L, 1L, 2))
                    .thenReturn(ApiResponse.success(
                            "ok", InventoryResponse.builder().build()));
            when(modelMapper.map(sampleOrder, OrderDTO.class)).thenReturn(sampleOrderDTO);
            when(modelMapper.map(any(OrderItem.class), eq(OrderItemDTO.class))).thenReturn(sampleItemDTO);

            orderService.updateOrderStatus(1L, OrderStatus.CANCELLED);

            verify(productServiceClient, times(1)).releaseByProductAndWarehouse(101L, 1L, 2);
        }

        @Test
        @DisplayName("Should throw BusinessException when trying to update a DELIVERED order")
        void updateOrderStatus_FromDelivered_ThrowsBusinessException() {
            Order deliveredOrder = Order.builder()
                    .id(1L)
                    .status(OrderStatus.DELIVERED)
                    .orderItems(new ArrayList<>())
                    .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(deliveredOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot change status from DELIVERED");
        }

        @Test
        @DisplayName("Should throw BusinessException when moving from SHIPPED back to PENDING")
        void updateOrderStatus_ShippedToPending_ThrowsBusinessException() {
            Order shippedOrder = Order.builder()
                    .id(1L)
                    .status(OrderStatus.SHIPPED)
                    .orderItems(new ArrayList<>())
                    .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(shippedOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.PENDING))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot move from SHIPPED back to PENDING");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for unknown order")
        void updateOrderStatus_OrderNotFound_ThrowsResourceNotFoundException() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.updateOrderStatus(999L, OrderStatus.CONFIRMED))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────
    // deleteOrder
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("deleteOrder()")
    class DeleteOrderTests {

        @Test
        @DisplayName("Should delete order when status is PENDING")
        void deleteOrder_PendingOrder_DeletesSuccessfully() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
            doNothing().when(orderRepository).delete(sampleOrder);

            orderService.deleteOrder(1L);

            verify(orderRepository, times(1)).delete(sampleOrder);
        }

        @Test
        @DisplayName("Should delete order when status is CANCELLED")
        void deleteOrder_CancelledOrder_DeletesSuccessfully() {
            Order cancelledOrder = Order.builder()
                    .id(1L)
                    .status(OrderStatus.CANCELLED)
                    .orderItems(new ArrayList<>())
                    .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(cancelledOrder));
            doNothing().when(orderRepository).delete(cancelledOrder);

            orderService.deleteOrder(1L);

            verify(orderRepository, times(1)).delete(cancelledOrder);
        }

        @Test
        @DisplayName("Should throw BusinessException when deleting a CONFIRMED order")
        void deleteOrder_ConfirmedOrder_ThrowsBusinessException() {
            Order confirmedOrder = Order.builder()
                    .id(1L)
                    .status(OrderStatus.CONFIRMED)
                    .orderItems(new ArrayList<>())
                    .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(confirmedOrder));

            assertThatThrownBy(() -> orderService.deleteOrder(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Can only delete orders with PENDING or CANCELLED status");

            verify(orderRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw BusinessException when deleting a SHIPPED order")
        void deleteOrder_ShippedOrder_ThrowsBusinessException() {
            Order shippedOrder = Order.builder()
                    .id(1L)
                    .status(OrderStatus.SHIPPED)
                    .orderItems(new ArrayList<>())
                    .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(shippedOrder));

            assertThatThrownBy(() -> orderService.deleteOrder(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Can only delete orders with PENDING or CANCELLED status");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when order does not exist")
        void deleteOrder_OrderNotFound_ThrowsResourceNotFoundException() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.deleteOrder(999L)).isInstanceOf(ResourceNotFoundException.class);

            verify(orderRepository, never()).delete(any());
        }
    }

    // ─────────────────────────────────────────────
    // getOrderCountByCustomerId
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("getOrderCountByCustomerId()")
    class GetOrderCountByCustomerIdTests {

        @Test
        @DisplayName("Should return correct count for a given customer")
        void getOrderCountByCustomerId_ValidCustomer_ReturnsCount() {
            when(orderRepository.countByCustomerId(500L)).thenReturn(7L);

            Long count = orderService.getOrderCountByCustomerId(500L);

            assertThat(count).isEqualTo(7L);
            verify(orderRepository, times(1)).countByCustomerId(500L);
        }

        @Test
        @DisplayName("Should return zero when customer has no orders")
        void getOrderCountByCustomerId_NoOrders_ReturnsZero() {
            when(orderRepository.countByCustomerId(999L)).thenReturn(0L);

            Long count = orderService.getOrderCountByCustomerId(999L);

            assertThat(count).isZero();
        }
    }
}
