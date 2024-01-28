package thainguyen.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import thainguyen.domain.converter.PriceConverter;
import thainguyen.domain.valuetypes.Price;

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
    @NotNull
    private DetailProduct detailProduct;

    @Convert(converter = PriceConverter.class)
    private Price amount;
    private @NotNull Integer quantity;

    @Convert(converter = PriceConverter.class)
    private Price totalPrice;

    private Double totalWeight;

    /*Custom constructor*/
    public LineItem(DetailProduct detailProduct, Integer quantity, Price amount) {
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
