package com.mestro.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDTO {
    private Long id;

    private Long productId;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    private Boolean isPrimary;

    private Integer displayOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
