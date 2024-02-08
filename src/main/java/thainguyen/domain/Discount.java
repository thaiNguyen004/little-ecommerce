package thainguyen.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static thainguyen.domain.Constants.SEQUENCE_GENERATOR;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Discount {
    @Id
    @GeneratedValue(generator = SEQUENCE_GENERATOR)
    private Long id;

    @Version
    private Long version;

    @ManyToMany(mappedBy = "discounts")
    @JsonIgnore
    private Set<Order> order = new HashSet<>();

    @NotNull
    @NotBlank(message = "Code attribute must not be null or empty!")
    @Column(unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Type type;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Kind kind;

    @Min(value = 1, message = "Discount's value require greater than 1")
    @NotNull(message = "Value attribute must not be null")
    private Integer value;

    @NotNull(message = "Quantity attribute must not be null")
    private Integer quantity;

    private LocalDateTime start;
    private LocalDateTime end;

    public static enum Type {
        PERCENTAGE, AMOUNT
    }

    public static enum Kind {
        NORMAL, FREESHIP
    }

    /*Custom constructor*/

    public Discount(String code
            , Type type
            , Kind kind
            , Integer value
            , Integer quantity
            , LocalDateTime start
            , LocalDateTime end) {
        this.code = code;
        this.type = type;
        this.kind = kind;
        this.value = value;
        this.quantity = quantity;
        this.start = start;
        this.end = end;
    }

}
