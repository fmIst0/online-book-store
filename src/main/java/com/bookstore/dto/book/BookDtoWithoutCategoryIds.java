package com.bookstore.dto.book;

import java.math.BigDecimal;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class BookDtoWithoutCategoryIds {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private BigDecimal price;
    private String description;
    private String coverImage;
}
