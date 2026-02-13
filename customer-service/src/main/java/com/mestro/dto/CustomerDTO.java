package com.mestro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
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
@Schema(description = "Customer data transfer object")
public class CustomerDTO {

    @Schema(description = "Customer unique identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Schema(description = "Customer first name", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Customer last name", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(
            description = "Customer email address",
            example = "john.doe@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Customer date of birth", example = "1990-05-15")
    private LocalDate dob;

    @Schema(description = "Customer phone number", example = "+1-555-123-4567")
    private String phone;

    @Schema(description = "Customer gender", example = "Male")
    private String gender;

    @Schema(description = "Additional notes about the customer", example = "VIP customer")
    private String notes;

    @Schema(description = "List of customer addresses", accessMode = Schema.AccessMode.READ_ONLY)
    private List<CustomerAddressDTO> addresses;

    @Schema(description = "Record creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Record last update timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}
