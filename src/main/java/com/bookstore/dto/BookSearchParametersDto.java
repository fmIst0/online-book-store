package com.bookstore.dto;

public record BookSearchParametersDto(String[] titles,
                                      String[] authors,
                                      String[] prices,
                                      String[] isbns) {
}
