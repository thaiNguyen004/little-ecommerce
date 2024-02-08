package thainguyen.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static thainguyen.domain.Constants.SEQUENCE_GENERATOR;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class LineItem {

    @Id
    @GeneratedValue(generator = SEQUENCE_GENERATOR)
    private Long id;

    @ManyToOne(targetEntity = Order.class)
    @JsonIgnore
    private Order order;

    @ManyToOne(targetEntity = DetailProduct.class)
    @NotNull(message = "detailProduct attribute must not be null")
    private DetailProduct detailProduct;

    private Integer amount;
    @NotNull(message = "quantity attribute must not be null")
    private Integer quantity;

    private Integer totalPrice;

    private Double totalWeight;

    /*Custom constructor*/
    public LineItem(DetailProduct detailProduct, Integer quantity, Integer amount) {
        this.detailProduct = detailProduct;
        this.amount = amount;
        this.quantity = quantity;
    }

    public LineItem(DetailProduct detailProduct, Integer quantity) {
        this.detailProduct = detailProduct;
        this.amount = detailProduct.getPrice();
        this.quantity = quantity;
    }

}
