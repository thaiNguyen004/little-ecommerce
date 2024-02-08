package thainguyen.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull(message = "labalCode attribute must not be null")
    private String labelCode;

    @NotNull(message = "fee attribute must not be null")
    @Size(min = 1, message = "fee's minimum value is 1")
    private Integer fee;

    @NotNull(message = "insuranceFee attribute must not be null")
    @Size(min = 0, message = "insuranceFee's minimum value is 0")
    private Integer insuranceFee;

    @NotNull(message = "estimatedPickTime attribute must not be null")
    private String estimatedPickTime;

    @NotNull(message = "estimatedDeliverTime attribute must not be null")
    private String estimatedDeliverTime;

    @NotNull(message = "trackingID attribute must not be null")
    private Integer trackingID;

    /*Custom constructor*/
    public Shipment(Order order) {
        this.order = order;
    }
}
