package com.bookstore.dto.cartitem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class CartItemCreateDto {
    @NotNull
    private Long bookId;
    @NotNull
    @Min(1)
    private int quantity;
}
