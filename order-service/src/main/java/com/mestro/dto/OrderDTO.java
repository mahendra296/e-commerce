package com.mestro.dto;

import com.mestro.enums.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private OrderStatus status;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemDTO> orderItems;

    private BigDecimal totalAmount;

    private String shippingAddress;

    private String billingAddress;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
