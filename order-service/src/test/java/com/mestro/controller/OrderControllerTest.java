package com.mestro.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mestro.common.dto.PageResponseDTO;
import com.mestro.dto.OrderDTO;
import com.mestro.dto.OrderItemDTO;
import com.mestro.enums.OrderStatus;
import com.mestro.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@DisplayName("OrderController Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    private ObjectMapper objectMapper;
    private OrderDTO sampleOrderDTO;
    private OrderItemDTO sampleOrderItemDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        sampleOrderItemDTO = OrderItemDTO.builder()
                .id(1L)
                .productId(101L)
                .warehouseId(1L)
                .productName("Laptop Pro X")
                .quantity(2)
                .unitPrice(new BigDecimal("999.99"))
                .subtotal(new BigDecimal("1999.98"))
                .build();

        sampleOrderDTO = OrderDTO.builder()
                .id(1L)
                .customerId(500L)
                .status(OrderStatus.PENDING)
                .orderItems(List.of(sampleOrderItemDTO))
                .totalAmount(new BigDecimal("1999.98"))
                .shippingAddress("123 Main St, Springfield, IL 62701")
                .billingAddress("123 Main St, Springfield, IL 62701")
                .notes("Please handle with care")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ─────────────────────────────────────────────
    // POST /api/v1/orders
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/v1/orders - Create Order")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order and return 201 Created")
        void createOrder_ValidRequest_Returns201() throws Exception {
            when(orderService.createOrder(any(OrderDTO.class))).thenReturn(sampleOrderDTO);

            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleOrderDTO)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.customerId").value(500L))
                    .andExpect(jsonPath("$.data.status").value("PENDING"))
                    .andExpect(jsonPath("$.message").value("Order created successfully"));

            verify(orderService, times(1)).createOrder(any(OrderDTO.class));
        }

        @Test
        @DisplayName("Should return 400 when customerId is null")
        void createOrder_NullCustomerId_Returns400() throws Exception {
            OrderDTO invalidOrder = OrderDTO.builder()
                    .customerId(null) // required field missing
                    .orderItems(List.of(sampleOrderItemDTO))
                    .build();

            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidOrder)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(orderService, never()).createOrder(any());
        }

        @Test
        @DisplayName("Should return 400 when orderItems list is empty")
        void createOrder_EmptyOrderItems_Returns400() throws Exception {
            OrderDTO invalidOrder = OrderDTO.builder()
                    .customerId(500L)
                    .orderItems(List.of()) // @NotEmpty violated
                    .build();

            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidOrder)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(orderService, never()).createOrder(any());
        }

        @Test
        @DisplayName("Should return 400 when orderItem has invalid quantity")
        void createOrder_InvalidItemQuantity_Returns400() throws Exception {
            OrderItemDTO badItem = OrderItemDTO.builder()
                    .productId(101L)
                    .quantity(0) // @Min(1) violated
                    .unitPrice(new BigDecimal("9.99"))
                    .build();

            OrderDTO order = OrderDTO.builder()
                    .customerId(500L)
                    .orderItems(List.of(badItem))
                    .build();

            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(order)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/orders/{orderId}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/v1/orders/{orderId} - Get Order By ID")
    class GetOrderByIdTests {

        @Test
        @DisplayName("Should return 200 and order details for a valid ID")
        void getOrderById_ValidId_Returns200() throws Exception {
            when(orderService.getOrderById(1L)).thenReturn(sampleOrderDTO);

            mockMvc.perform(get("/api/v1/orders/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.customerId").value(500L))
                    .andExpect(jsonPath("$.data.orderItems", hasSize(1)))
                    .andExpect(jsonPath("$.message").value("Order retrieved successfully"));

            verify(orderService, times(1)).getOrderById(1L);
        }

        @Test
        @DisplayName("Should return 200 for each distinct order ID")
        void getOrderById_MultipleIds_ReturnsCorrectOrder() throws Exception {
            OrderDTO secondOrder = OrderDTO.builder()
                    .id(2L)
                    .customerId(600L)
                    .status(OrderStatus.CONFIRMED)
                    .orderItems(List.of(sampleOrderItemDTO))
                    .build();

            when(orderService.getOrderById(1L)).thenReturn(sampleOrderDTO);
            when(orderService.getOrderById(2L)).thenReturn(secondOrder);

            mockMvc.perform(get("/api/v1/orders/2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(2L))
                    .andExpect(jsonPath("$.data.customerId").value(600L));
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/orders
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/v1/orders - Get All Orders (Paginated)")
    class GetAllOrdersTests {

        @Test
        @DisplayName("Should return 200 with paginated order list")
        void getAllOrders_Returns200WithPage() throws Exception {
            PageResponseDTO<OrderDTO> page = PageResponseDTO.<OrderDTO>builder()
                    .content(List.of(sampleOrderDTO))
                    .page(0)
                    .size(10)
                    .totalElements(1L)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            when(orderService.getAllOrders(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/v1/orders").param("page", "0").param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.message").value("Orders retrieved successfully"));
        }

        @Test
        @DisplayName("Should return 200 with empty content when no orders exist")
        void getAllOrders_EmptyList_Returns200WithEmptyContent() throws Exception {
            PageResponseDTO<OrderDTO> emptyPage = PageResponseDTO.<OrderDTO>builder()
                    .content(List.of())
                    .totalElements(0L)
                    .totalPages(0)
                    .build();

            when(orderService.getAllOrders(any(Pageable.class))).thenReturn(emptyPage);

            mockMvc.perform(get("/api/v1/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(0)));
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/orders/customer/{customerId}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/v1/orders/customer/{customerId} - Get Orders By Customer")
    class GetOrdersByCustomerIdTests {

        @Test
        @DisplayName("Should return 200 with orders for the given customer")
        void getOrdersByCustomerId_ValidCustomer_Returns200() throws Exception {
            PageResponseDTO<OrderDTO> page = PageResponseDTO.<OrderDTO>builder()
                    .content(List.of(sampleOrderDTO))
                    .totalElements(1L)
                    .totalPages(1)
                    .build();

            when(orderService.getOrdersByCustomerId(eq(500L), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/orders/customer/500"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].customerId").value(500L))
                    .andExpect(jsonPath("$.message").value("Customer orders retrieved successfully"));
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/orders/status/{status}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/v1/orders/status/{status} - Get Orders By Status")
    class GetOrdersByStatusTests {

        @Test
        @DisplayName("Should return 200 with list of PENDING orders")
        void getOrdersByStatus_ValidStatus_Returns200() throws Exception {
            when(orderService.getOrdersByStatus(OrderStatus.PENDING)).thenReturn(List.of(sampleOrderDTO));

            mockMvc.perform(get("/api/v1/orders/status/PENDING"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].status").value("PENDING"));
        }

        @Test
        @DisplayName("Should return 200 with empty list when no matching orders")
        void getOrdersByStatus_NoMatchingOrders_Returns200WithEmptyList() throws Exception {
            when(orderService.getOrdersByStatus(OrderStatus.DELIVERED)).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/orders/status/DELIVERED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/orders/date-range
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/v1/orders/date-range - Get Orders By Date Range")
    class GetOrdersByDateRangeTests {

        @Test
        @DisplayName("Should return 200 with orders within the given date range")
        void getOrdersByDateRange_ValidRange_Returns200() throws Exception {
            when(orderService.getOrdersBetweenDates(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(sampleOrderDTO));

            mockMvc.perform(get("/api/v1/orders/date-range")
                            .param("startDate", "2024-01-01T00:00:00")
                            .param("endDate", "2024-12-31T23:59:59"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)));
        }

        @Test
        @DisplayName("Should return 400 when date parameters are missing")
        void getOrdersByDateRange_MissingParams_Returns400() throws Exception {
            mockMvc.perform(get("/api/v1/orders/date-range")).andExpect(status().isBadRequest());
        }
    }

    // ─────────────────────────────────────────────
    // PUT /api/v1/orders/{orderId}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /api/v1/orders/{orderId} - Update Order")
    class UpdateOrderTests {

        @Test
        @DisplayName("Should return 200 with updated order data")
        void updateOrder_ValidRequest_Returns200() throws Exception {
            OrderDTO updatedOrder = OrderDTO.builder()
                    .id(1L)
                    .customerId(500L)
                    .status(OrderStatus.CONFIRMED)
                    .orderItems(List.of(sampleOrderItemDTO))
                    .shippingAddress("456 Oak Ave, Chicago, IL 60601")
                    .build();

            when(orderService.updateOrder(eq(1L), any(OrderDTO.class))).thenReturn(updatedOrder);

            mockMvc.perform(put("/api/v1/orders/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleOrderDTO)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                    .andExpect(jsonPath("$.message").value("Order updated successfully"));

            verify(orderService, times(1)).updateOrder(eq(1L), any(OrderDTO.class));
        }
    }

    // ─────────────────────────────────────────────
    // PATCH /api/v1/orders/{orderId}/status
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("PATCH /api/v1/orders/{orderId}/status - Update Order Status")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("Should return 200 when status is updated successfully")
        void updateOrderStatus_ValidTransition_Returns200() throws Exception {
            OrderDTO confirmedOrder = OrderDTO.builder()
                    .id(1L)
                    .customerId(500L)
                    .status(OrderStatus.CONFIRMED)
                    .orderItems(List.of(sampleOrderItemDTO))
                    .build();

            when(orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED)).thenReturn(confirmedOrder);

            mockMvc.perform(patch("/api/v1/orders/1/status").param("status", "CONFIRMED"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                    .andExpect(jsonPath("$.message").value("Order status updated successfully"));

            verify(orderService, times(1)).updateOrderStatus(1L, OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should return 400 when status parameter is missing")
        void updateOrderStatus_MissingStatus_Returns400() throws Exception {
            mockMvc.perform(patch("/api/v1/orders/1/status")).andExpect(status().isBadRequest());
        }
    }

    // ─────────────────────────────────────────────
    // DELETE /api/v1/orders/{orderId}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/v1/orders/{orderId} - Delete Order")
    class DeleteOrderTests {

        @Test
        @DisplayName("Should return 200 when order is deleted successfully")
        void deleteOrder_ValidId_Returns200() throws Exception {
            doNothing().when(orderService).deleteOrder(1L);

            mockMvc.perform(delete("/api/v1/orders/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Order deleted successfully"));

            verify(orderService, times(1)).deleteOrder(1L);
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/orders/customer/{customerId}/count
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/v1/orders/customer/{customerId}/count - Get Order Count")
    class GetOrderCountByCustomerTests {

        @Test
        @DisplayName("Should return 200 with the correct order count")
        void getOrderCountByCustomer_ValidCustomerId_Returns200() throws Exception {
            when(orderService.getOrderCountByCustomerId(500L)).thenReturn(5L);

            mockMvc.perform(get("/api/v1/orders/customer/500/count"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(5))
                    .andExpect(jsonPath("$.message").value("Order count retrieved successfully"));

            verify(orderService, times(1)).getOrderCountByCustomerId(500L);
        }

        @Test
        @DisplayName("Should return 0 when customer has no orders")
        void getOrderCountByCustomer_NoOrders_ReturnsZero() throws Exception {
            when(orderService.getOrderCountByCustomerId(999L)).thenReturn(0L);

            mockMvc.perform(get("/api/v1/orders/customer/999/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(0));
        }
    }
}
