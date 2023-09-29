package com.bookstore.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;

@Accessors(chain = true)
@Getter
@Setter
@EqualsAndHashCode
@Entity
@SQLDelete(sql = "UPDATE shopping_carts SET is_deleted=true WHERE id=?")
@Table(name = "shopping_carts")
public class ShoppingCart {
    @Id
    private Long id;
    @OneToOne
    @MapsId
    @PrimaryKeyJoinColumn(name = "user_id")
    private User user;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "shoppingCart", cascade = CascadeType.ALL)
    private Set<CartItem> cartItems;

    public void addCartItemToSet(CartItem cartItem) {
        cartItems.add(cartItem);
        cartItem.setShoppingCart(this);
    }

    public BigDecimal getTotal() {
        return this.getCartItems()
                .stream()
                .map(cartItem -> cartItem.getBook()
                        .getPrice()
                        .multiply(
                                BigDecimal.valueOf(cartItem.getQuantity())
                        )
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
