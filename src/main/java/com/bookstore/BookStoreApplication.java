package com.bookstore;

import com.bookstore.model.Book;
import com.bookstore.service.BookService;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BookStoreApplication {
    @Autowired
    private BookService bookService;

    public static void main(String[] args) {
        SpringApplication.run(BookStoreApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            Book math = new Book();
            math.setTitle("Math");
            math.setAuthor("Artem");
            math.setIsbn("09123");
            math.setDescription("Math study book.");
            math.setPrice(BigDecimal.TEN);
            math.setCoverImage("image");

            bookService.save(math);

            Book english = new Book();
            english.setTitle("English");
            english.setAuthor("Ivan");
            english.setIsbn("78654");
            english.setDescription("English study book.");
            english.setPrice(BigDecimal.valueOf(15));
            english.setCoverImage("image123");

            bookService.save(english);
            System.out.println(bookService.findAll());
        };
    }

}
