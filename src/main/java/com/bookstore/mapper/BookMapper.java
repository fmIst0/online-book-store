package com.bookstore.mapper;

import com.bookstore.config.MapperConfig;
import com.bookstore.dto.book.BookDto;
import com.bookstore.dto.book.BookDtoWithoutCategoryIds;
import com.bookstore.dto.book.CreateBookRequestDto;
import com.bookstore.model.Book;
import com.bookstore.model.Category;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface BookMapper {
    BookDto toDto(Book book);

    Book toBookModel(CreateBookRequestDto bookRequestDto);

    BookDtoWithoutCategoryIds toDtoWithoutCategories(Book book);

    @AfterMapping
    default void setCategoryIds(@MappingTarget BookDto bookDto, Book book) {
        Set<Long> ids = book.getCategories()
                .stream()
                .map(Category::getId)
                .collect(Collectors.toSet());
        bookDto.setCategoryIds(ids);
    }

    @Named("bookFromId")
    default Book bookFromId(Long id) {
        if (id == null) {
            throw new RuntimeException("Id can't be null.");
        }
        Book book = new Book();
        book.setId(id);
        return book;
    }
}
