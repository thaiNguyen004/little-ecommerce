package thainguyen.detailproduct;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import thainguyen.size.Size;
import thainguyen.product.Product;

import java.time.LocalDateTime;

import static thainguyen.utility.constant.Constants.SEQUENCE_GENERATOR;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DetailProduct {
    @Id
    @GeneratedValue(generator = SEQUENCE_GENERATOR)
    private Long id;

    @Version
    @JsonIgnore
    private Long version;

    @ManyToOne
    @NotNull(message = "size attribute must not be null")
    private Size size;

    @ManyToOne
    @NotNull(message = "product attribute must not be null")
    private Product product;

    @NotNull(message = "price attribute must not be null")
    @Min(value = 1000, message = "Price of detail product required greater than 1000 VND")
    private Integer price;

    @NotNull(message = "weight attribute must not be null")
    @DecimalMin(value = "0.01", message = "Weigh of detail product require greater than 10g")
    private Double weight;

    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    private void preCreate() {
        createdAt = LocalDateTime.now();
    }

    /*Custom constructor*/
    public DetailProduct(Double weight, Size size, int price) {
        this.weight = weight;
        this.size = size;
        this.price = price;
    }
}
