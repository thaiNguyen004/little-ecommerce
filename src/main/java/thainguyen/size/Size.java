package thainguyen.size;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import thainguyen.brand.Brand;
import thainguyen.category.Category;

import static thainguyen.utility.constant.Constants.SEQUENCE_GENERATOR;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Size {
    @Id
    @GeneratedValue(generator = SEQUENCE_GENERATOR)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = "brand of size must not be null")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = "category of size must not be null")
    private Category category;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Name attribute must not be null!")
    private Size.Name name;

    @NotNull(message = "Chest attribute must not be null")
    @Min(value = 1, message = "Chest width cannot be negative")
    private Integer chest;

    @NotNull(message = "Length attribute must not be null")
    @Min(value = 1, message = "Length width cannot be negative")
    private Integer length;

    @NotNull(message = "Width attribute must not be null")
    @Min(value = 1, message = "Width width cannot be negative")
    private Integer width;

    public static enum Name {
        S, M, L, XL, XXL, XXXL;
    }

    public Size(Name name, Integer chest, Integer length, Integer width) {
        this.name = name;
        this.chest = chest;
        this.length = length;
        this.width = width;
    }
}
