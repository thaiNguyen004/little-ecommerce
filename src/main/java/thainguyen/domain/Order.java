package thainguyen.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import thainguyen.domain.converter.PriceConverter;
import thainguyen.domain.valuetypes.Price;
import thainguyen.domain.valuetypes.Status;

import java.time.LocalDateTime;
import java.util.*;

import static thainguyen.domain.Constants.SEQUENCE_GENERATOR;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Orders")
public class Order {

    @Id
    @GeneratedValue(generator = SEQUENCE_GENERATOR)
    private Long id;

    @ManyToOne(targetEntity = User.class)
    private User user;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.PERSIST)
    private List<LineItem> lineItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.PERSIST)
    private Address address;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(unique = true)
    private Shipment shipment;

    @OneToMany(mappedBy = "order",fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JsonIgnore
    private List<Payment> payments = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<Discount> discounts = new ArrayList<>();

    @Convert(converter = PriceConverter.class)
    private Price totalPriceBeforeDiscount;

    @Convert(converter = PriceConverter.class)
    private Price totalPriceAfterDiscount;

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

}
