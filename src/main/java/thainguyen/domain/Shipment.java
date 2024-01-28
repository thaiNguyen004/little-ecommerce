package thainguyen.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import thainguyen.domain.converter.PriceConverter;
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
    @JsonIgnore
    private Order order;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "shipment")
    private Set<Tracking> trackings = new HashSet<>();

    private @NotNull String labelCode;

    @Convert(converter = PriceConverter.class)
    private @NotNull Price fee;

    @Convert(converter = PriceConverter.class)
    private @NotNull Price insuranceFee;

    private @NotNull String estimatedPickTime;
    private @NotNull String estimatedDeliverTime;
    private @NotNull Integer trackingID;

    /*Custom constructor*/
    public Shipment(Order order) {
        this.order = order;
    }
}
