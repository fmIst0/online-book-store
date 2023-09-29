package com.bookstore.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.bookstore.dto.cartitem.CartItemCreateDto;
import com.bookstore.dto.cartitem.CartItemUpdateDto;
import com.bookstore.dto.shoppingcart.ShoppingCartDto;
import com.bookstore.exception.EntityNotFoundException;
import com.bookstore.mapper.CartItemMapper;
import com.bookstore.mapper.ShoppingCartMapper;
import com.bookstore.model.Book;
import com.bookstore.model.CartItem;
import com.bookstore.model.ShoppingCart;
import com.bookstore.model.User;
import com.bookstore.repository.book.BookRepository;
import com.bookstore.repository.cartitem.CartItemRepository;
import com.bookstore.repository.shoppingcart.ShoppingCartRepository;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTest {
    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private ShoppingCartMapper shoppingCartMapper;
    @Mock
    private CartItemMapper cartItemMapper;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private BookRepository bookRepository;
    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;

    @Test
    @DisplayName("Verify finAll() method works")
    void findAll_ValidPageable_ReturnsAllCarts() {
        //Given
        ShoppingCart shoppingCart = getShoppingCart();

        ShoppingCartDto shoppingCartDto = getShoppingCartDtoFromCart(shoppingCart);

        Pageable pageable = PageRequest.of(0, 10);
        List<ShoppingCart> shoppingCarts = List.of(shoppingCart);

        Page<ShoppingCart> shoppingCartPage =
                new PageImpl<>(shoppingCarts, pageable, shoppingCarts.size());

        when(shoppingCartRepository.findAll(pageable)).thenReturn(shoppingCartPage);
        when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(shoppingCartDto);

        //When
        List<ShoppingCartDto> shoppingCartDtos = shoppingCartService.findAll(pageable);

        //Then
        assertThat(shoppingCartDtos).hasSize(1);
        assertThat(shoppingCartDtos.get(0)).isEqualTo(shoppingCartDto);

        verify(shoppingCartRepository, times(1)).findAll(pageable);
        verify(shoppingCartMapper, times(1)).toDto(shoppingCart);
        verifyNoMoreInteractions(shoppingCartRepository, shoppingCartMapper);
    }

    @Test
    @DisplayName("Verify getByUserId() method works")
    void getByUserId_ValidUserId_ReturnsUsersCart() {
        //Given
        Long userId = 1L;

        ShoppingCart shoppingCart = getShoppingCart();

        ShoppingCartDto expected = getShoppingCartDtoFromCart(shoppingCart);

        when(shoppingCartRepository.findByUserId(anyLong())).thenReturn(Optional.of(shoppingCart));
        when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(expected);

        //When
        ShoppingCartDto actual = shoppingCartService.getByUserId(userId);

        //Then
        assertEquals(expected, actual);

        verify(shoppingCartRepository, times(1)).findByUserId(anyLong());
        verify(shoppingCartMapper, times(1)).toDto(shoppingCart);
        verifyNoMoreInteractions(shoppingCartMapper, shoppingCartRepository);
    }

    @Test
    @DisplayName("Verify EntityNotFoundException thrown with invalid user id")
    void getByUserId_InvalidUserId_ThrowsEntityNotFoundException() {
        //Given
        Long userId = -1L;
        String expected = "Can't find a shopping cart by user id: " + userId;

        when(shoppingCartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        //When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> shoppingCartService.getShoppingCartByUserId(userId));

        //Then
        String actual = exception.getMessage();

        assertEquals(EntityNotFoundException.class, exception.getClass());
        assertEquals(expected, actual);

        verify(shoppingCartRepository, times(1)).findByUserId(userId);
        verifyNoMoreInteractions(shoppingCartRepository);
    }

    @Test
    @DisplayName("Verify saveBookToTheCart() method works")
    void saveBookToTheCart_ValidUserIdAndCartItemCreateDto_ReturnsShoppingCartDto() {
        //Given

        CartItem cartItem = getCartItem();

        ShoppingCart shoppingCart = getShoppingCart();

        CartItemCreateDto cartItemCreateDto = getCartItemCreateDto();

        Book book = getBook();

        final Long userId = 1L;
        final ShoppingCartDto expected = getShoppingCartDtoFromCart(shoppingCart);
        when(shoppingCartRepository.findByUserId(anyLong())).thenReturn(Optional.of(shoppingCart));
        when(bookRepository.findById(cartItemCreateDto.getBookId())).thenReturn(Optional.of(book));
        when(cartItemMapper.toEntity(cartItemCreateDto, book, shoppingCart)).thenReturn(cartItem);
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
        shoppingCart.addCartItemToSet(cartItem);
        when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(expected);
        //When
        ShoppingCartDto actual = shoppingCartService
                .saveBookToTheCart(userId, cartItemCreateDto);

        //Then
        assertEquals(expected, actual);
        assertEquals(expected.getCartItemsDto().size(), actual.getCartItemsDto().size());
        assertEquals(expected.getCartItemsDto(), actual.getCartItemsDto());

        verify(shoppingCartRepository, times(1)).findByUserId(anyLong());
        verify(bookRepository, times(1)).findById(anyLong());
        verify(cartItemMapper, times(1)).toEntity(cartItemCreateDto, book, shoppingCart);
        verify(cartItemRepository, times(1)).save(cartItem);
        verify(shoppingCartMapper, times(1)).toDto(shoppingCart);
        verifyNoMoreInteractions(shoppingCartRepository,
                                bookRepository,
                                cartItemMapper,
                                cartItemRepository,
                                shoppingCartMapper);
    }

    @Test
    @DisplayName("Verify EntityNotFoundException was thrown with invalid CartItemCreateDto")
    void saveBookToTheCart_InvalidCartItemCreateDto_ThrowsEntityNotFoundException() {
        //Given
        ShoppingCart shoppingCart = getShoppingCart();

        CartItemCreateDto cartItemCreateDto = getCartItemCreateDto();
        cartItemCreateDto.setBookId(-1L);

        when(shoppingCartRepository.findByUserId(anyLong())).thenReturn(Optional.of(shoppingCart));
        when(bookRepository.findById(cartItemCreateDto.getBookId())).thenReturn(Optional.empty());

        String expected = "Can't find a book in DB by id: " + cartItemCreateDto.getBookId();

        //When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> shoppingCartService.saveBookToTheCart(anyLong(), cartItemCreateDto));

        //Then
        String actual = exception.getMessage();

        assertEquals(EntityNotFoundException.class, exception.getClass());
        assertEquals(expected, actual);

        verify(shoppingCartRepository, times(1)).findByUserId(anyLong());
        verify(bookRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(shoppingCartRepository, bookRepository);
    }

    @Test
    @DisplayName("Verify update() method works")
    void update_ValidUserIdAndCartItemIdAndCartItemUpdateDto_UpdatesCart() {
        //Given
        Long cartItemId = 1L;

        CartItem cartItem = getCartItem();

        CartItemUpdateDto cartItemUpdateDto = getCartItemUpdateDto();

        ShoppingCart shoppingCart = getShoppingCart();

        final ShoppingCartDto expected = getShoppingCartDtoFromCart(shoppingCart);
        final Long userId = 1L;
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));
        cartItem.setQuantity(cartItemUpdateDto.getQuantity());
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
        when(shoppingCartRepository.findByUserId(userId)).thenReturn(Optional.of(shoppingCart));
        when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(expected);
        //When
        ShoppingCartDto actual = shoppingCartService.update(userId, cartItemId, cartItemUpdateDto);
        //Then
        assertEquals(expected, actual);

        verify(cartItemRepository, times(1)).findById(cartItemId);
        verify(cartItemRepository, times(1)).save(cartItem);
        verify(shoppingCartRepository, times(1)).findByUserId(userId);
        verify(shoppingCartMapper, times(1)).toDto(shoppingCart);
        verifyNoMoreInteractions(cartItemRepository, shoppingCartRepository, shoppingCartMapper);
    }

    @Test
    @DisplayName("Verify update() throws EntityNotFoundException with invalid cart item id")
    void update_InvalidCartItemId_ThrowsEntityNotFoundException() {
        //Given
        Long userId = 1L;
        Long cartItemId = -1L;
        String expected = "Can't find a cart item in DB by id: " + cartItemId;

        CartItemUpdateDto cartItemUpdateDto = getCartItemUpdateDto();

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

        //When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> shoppingCartService.update(userId, cartItemId, cartItemUpdateDto));

        //Then
        String actual = exception.getMessage();

        assertEquals(EntityNotFoundException.class, exception.getClass());
        assertEquals(expected, actual);

        verify(cartItemRepository, times(1)).findById(cartItemId);
        verifyNoMoreInteractions(cartItemRepository);
    }

    @Test
    @DisplayName("Verify deleteCartItemFromTheCart() method works")
    void deleteCartItemFromTheCart_ValidId_DeletesCartItem() {
        //Given
        Long cartItemId = 1L;

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(new CartItem()));

        //When
        shoppingCartService.deleteCartItemFromTheCart(cartItemId);

        //Then
        verify(cartItemRepository, times(1)).findById(cartItemId);
        verify(cartItemRepository, times(1)).deleteById(cartItemId);
        verifyNoMoreInteractions(cartItemRepository);
    }

    @Test
    @DisplayName("Verify EntityNotFoundException thrown with invalid cart item id")
    void deleteCartItemFromTheCart_InvalidId_ThrowsEntityNotFoundException() {
        //Given
        Long cartItemId = -1L;
        String expected = "Can't delete a cart item from DB with id: " + cartItemId;

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

        //When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> shoppingCartService.deleteCartItemFromTheCart(cartItemId));

        //Then
        String actual = exception.getMessage();

        assertEquals(EntityNotFoundException.class, exception.getClass());
        assertEquals(expected, actual);

        verify(cartItemRepository, times(1)).findById(cartItemId);
        verifyNoMoreInteractions(cartItemRepository);
    }

    @Test
    @DisplayName("Verify cleanShoppingCart() method works")
    void cleanShoppingCart_ValidCart_DeletesCartItems() {
        //Given
        User user = getUser();

        ShoppingCart expected = getShoppingCart();
        expected.setUser(user);

        ShoppingCart tested = getShoppingCart();
        tested.setUser(user);
        tested.setCartItems(Set.of(getCartItem()));

        //When
        shoppingCartService.cleanShoppingCart(tested);

        //Then
        assertEquals(expected, tested);
        verify(cartItemRepository, times(1)).deleteCartItemsByShoppingCartId(anyLong());
        verifyNoMoreInteractions(cartItemRepository);
    }

    private ShoppingCart getShoppingCart() {
        return new ShoppingCart()
                .setId(1L)
                .setUser(getUser())
                .setCartItems(new HashSet<>());
    }

    private User getUser() {
        return new User()
                .setId(1L)
                .setEmail("Test Email")
                .setPassword("Test Password")
                .setFirstName("Name")
                .setLastName("Surname")
                .setShippingAddress("Address")
                .setRoles(new HashSet<>());
    }

    private ShoppingCartDto getShoppingCartDtoFromCart(ShoppingCart shoppingCart) {
        return new ShoppingCartDto()
                .setUserId(shoppingCart.getUser().getId())
                .setCartItemsDto(new HashSet<>());
    }

    private CartItemCreateDto getCartItemCreateDto() {
        return new CartItemCreateDto()
                .setBookId(1L)
                .setQuantity(10);
    }

    private Book getBook() {
        return new Book()
                .setId(1L)
                .setTitle("Test Book")
                .setPrice(BigDecimal.TEN)
                .setDescription("Test Description")
                .setCoverImage("coverImage")
                .setAuthor("Tester")
                .setIsbn("isbn")
                .setCategories(new HashSet<>());
    }

    private CartItem getCartItem() {
        return new CartItem()
                .setId(1L)
                .setBook(getBook())
                .setShoppingCart(getShoppingCart())
                .setQuantity(10);
    }

    private CartItemUpdateDto getCartItemUpdateDto() {
        return new CartItemUpdateDto()
                .setQuantity(20);
    }
}
