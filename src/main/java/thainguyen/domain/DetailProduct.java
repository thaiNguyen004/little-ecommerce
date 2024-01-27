package thainguyen.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import thainguyen.domain.valuetypes.Price;

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

    private @NotNull String name;

    @ManyToOne
    private Size size;

    @ManyToOne
    private Product product;

    private Price price;
    private Double weight;
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    private void preCreate() {
        createdAt = LocalDateTime.now();
    }

    /*Custom constructor*/
    public DetailProduct(String name, Double weight, Size size, Price price) {
        this.name = name;
        this.weight = weight;
        this.size = size;
        this.price = price;
    }
}
