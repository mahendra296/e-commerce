package com.mestro.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mestro.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer_addresses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAddress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private Customer customer;

    private String addressType; // HOME, WORK, BILLING, SHIPPING

    private String street;

    private String city;

    private String state;

    private String zipCode;

    private String country;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;
}
