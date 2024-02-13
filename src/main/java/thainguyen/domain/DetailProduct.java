package thainguyen.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

import static thainguyen.domain.Constants.SEQUENCE_GENERATOR;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DetailProduct {
    @Id
    @GeneratedValue(generator = SEQUENCE_GENERATOR)
    private Long id;

    @Version
    private Long version;

    @NotBlank(message = "Name attribute must not be null and empty!")
    private String name;

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
    public DetailProduct(String name, Double weight, Size size, int price) {
        this.name = name;
        this.weight = weight;
        this.size = size;
        this.price = price;
    }
}
