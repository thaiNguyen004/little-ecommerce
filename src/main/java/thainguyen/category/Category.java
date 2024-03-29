package thainguyen.category;

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
public class Category {

    @Id
    @GeneratedValue(generator = SEQUENCE_GENERATOR)
    private Long id;

    @Version
    @JsonIgnore
    private Long version;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "category")
    @JsonIgnore
    private Collection<Product> products = new ArrayList<>();

    @NotBlank(message = "Name attribute must not be null and empty!")
    @Column(unique = true)
    private String name;

    private String picture;

    private String description;

    @ManyToOne
    private Category parent;

    /*Custom constructor*/
    public Category(String name, String picture, String description, Category parent) {
        this.name = name;
        this.picture = picture;
        this.description = description;
        this.parent = parent;
    }

}
