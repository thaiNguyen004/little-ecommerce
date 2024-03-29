package thainguyen.product;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import thainguyen.brand.Brand;
import thainguyen.category.Category;
import thainguyen.detailproduct.DetailProduct;

import java.util.*;

import static thainguyen.utility.constant.Constants.SEQUENCE_GENERATOR;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(generator = SEQUENCE_GENERATOR)
    private Long id;

    @Version
    @JsonIgnore
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = "category attribute must not be null")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = "brand attribute must not be null")
    private Brand brand;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product")
    @JsonIgnore
    private List<DetailProduct> detailProducts = new ArrayList<>();

    @NotBlank(message = "Name attribute must not be null and empty!")
    private String name;

    private String description;

    private String picture;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date createdAt;

    @UpdateTimestamp
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date updatedAt;

    @PrePersist
    private void preCreate() {
        createdAt = new Date();
    }

    /*Custom constructor*/
    public Product(Category category, Brand brand, String name, String picture, String description) {
        this.category = category;
        this.brand = brand;
        this.name = name;
        this.picture = picture;
        this.description = description;
    }
}
