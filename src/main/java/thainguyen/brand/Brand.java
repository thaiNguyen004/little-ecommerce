package thainguyen.brand;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import thainguyen.product.Product;

import java.util.*;

import static thainguyen.utility.constant.Constants.SEQUENCE_GENERATOR;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Brand {
    @Id
    @GeneratedValue(generator = SEQUENCE_GENERATOR)
    private Long id;

    @Version
    @JsonIgnore
    private Long version;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "brand")
    @JsonIgnore
    private List<Product> products = new ArrayList<>();

    @NotBlank(message = "Name attribute must not be null and empty!")
    @Column(unique = true)
    private String name;

    @NotBlank(message = "Logo attribute must not be null or empty!")
    private String logo;

    /*Custom constructor*/

    public Brand(String name, String logo) {
        this.name = name;
        this.logo = logo;
    }
}
