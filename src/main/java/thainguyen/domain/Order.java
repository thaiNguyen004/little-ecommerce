package thainguyen.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import thainguyen.domain.valuetypes.Status;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Orders")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Order implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    private User user;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.PERSIST)
    @NotNull(message = "lineItems must not be null")
    @Size(min = 1, message = "Minimum of list lineItems is 1")
    @Valid
    private List<LineItem> lineItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @NotNull(message = "address of the order must not be null")
    @Valid
    private Address address;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(unique = true)
    private Shipment shipment;

    @OneToMany(mappedBy = "order",fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JsonIgnore
    private List<Payment> payments = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<Discount> discounts = new ArrayList<>();

    private Integer totalPriceBeforeDiscount;
    private Integer totalPriceAfterDiscount;

    @Enumerated(EnumType.STRING)
    private Status status;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDateTime placedAt;

    @UpdateTimestamp
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDateTime modifiedAt;

    @PrePersist
    private void preCreate() {
        placedAt = LocalDateTime.now();
        modifiedAt = LocalDateTime.now();
    }

    public void addDiscount(Discount discount) {
        this.discounts.add(discount);
    }

    public void addLineItem(LineItem lineItem) {
        this.lineItems.add(lineItem);
        lineItem.setOrder(this);
    }

    /*Custom constructor*/

    public Order(UUID id, Address address,
                 Integer totalPriceBeforeDiscount,
                 Integer totalPriceAfterDiscount,
                 Status status,
                 LocalDateTime placedAt,
                 LocalDateTime modifiedAt) {
        this.id = id;
        this.address = address;
        this.totalPriceBeforeDiscount = totalPriceBeforeDiscount;
        this.totalPriceAfterDiscount = totalPriceAfterDiscount;
        this.status = status;
        this.placedAt = placedAt;
        this.modifiedAt = modifiedAt;
    }
}
