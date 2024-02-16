package thainguyen.shipment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import thainguyen.tracking.Tracking;
import thainguyen.order.Order;

import java.util.HashSet;
import java.util.Set;

import static thainguyen.utility.constant.Constants.SEQUENCE_GENERATOR;

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
    private Integer fee;

    @NotNull(message = "insuranceFee attribute must not be null")
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
