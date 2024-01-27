package thainguyen.domain;

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
public class Size {
    @Id
    @GeneratedValue(generator = SEQUENCE_GENERATOR)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @Enumerated(EnumType.STRING)
    private @NotNull Type name;
    private Integer chest;
    private Integer length;
    private Integer width;

    public static enum Type {
        S, M, L, XL, XXL, XXXL
    }

    public Size(Type name, Integer chest, Integer length, Integer width) {
        this.name = name;
        this.chest = chest;
        this.length = length;
        this.width = width;
    }
}
