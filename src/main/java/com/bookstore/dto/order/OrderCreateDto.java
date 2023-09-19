package com.bookstore.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderCreateDto {
    @NotBlank
    private String shippingAddress;
}
