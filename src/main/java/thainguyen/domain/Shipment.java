package thainguyen.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import thainguyen.domain.valuetypes.Price;

import java.util.HashSet;
import java.util.Set;

import static thainguyen.domain.Constants.SEQUENCE_GENERATOR;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Shipment {
    @Id
    @GeneratedValue(generator = SEQUENCE_GENERATOR)
    private Long id;

    @OneToOne(mappedBy = "shipment")
    private Order order;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "shipment")
    private Set<Tracking> trackings = new HashSet<>();

    private @NotNull String labelCode;
    @AttributeOverrides(value = {
            @AttributeOverride(name = "value", column = @Column(name = "FEE_VALUE")),
            @AttributeOverride(name = "currency", column = @Column(name = "FEE_CURRENCY"))
    })
    private @NotNull Price fee;
    @AttributeOverrides(value = {
            @AttributeOverride(name = "value", column = @Column(name = "INSURANCE_VALUE")),
            @AttributeOverride(name = "currency", column = @Column(name = "INSURANCE_CURRENCY"))
    })
    private @NotNull Price insuranceFee;
    private @NotNull String estimatedPickTime;
    private @NotNull String estimatedDeliverTime;
    private @NotNull Integer trackingID;
}
