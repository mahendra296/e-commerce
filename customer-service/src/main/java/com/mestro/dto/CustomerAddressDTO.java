package com.mestro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Customer address data transfer object")
public class CustomerAddressDTO {

    @Schema(description = "Address unique identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Associated customer ID", example = "1")
    private Long customerId;

    @NotBlank(message = "Address type is required")
    @Schema(
            description = "Type of address",
            example = "HOME",
            allowableValues = {"HOME", "WORK", "BILLING", "SHIPPING"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String addressType;

    @NotBlank(message = "Street is required")
    @Schema(description = "Street address", example = "123 Main Street", requiredMode = Schema.RequiredMode.REQUIRED)
    private String street;

    @Schema(description = "City name", example = "New York")
    private String city;

    @Schema(description = "State or province", example = "NY")
    private String state;

    @Schema(description = "ZIP or postal code", example = "10001")
    private String zipCode;

    @Schema(description = "Country name", example = "USA")
    private String country;

    @Builder.Default
    @Schema(description = "Whether this is the default address", example = "false")
    private Boolean isDefault = false;

    @Schema(description = "Record creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Record last update timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}
