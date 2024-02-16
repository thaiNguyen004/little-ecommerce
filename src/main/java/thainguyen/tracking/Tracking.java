package thainguyen.tracking;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import thainguyen.shipment.Shipment;

import java.time.LocalDateTime;

import static thainguyen.utility.constant.Constants.SEQUENCE_GENERATOR;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Tracking {
    @Id
    @GeneratedValue(generator = SEQUENCE_GENERATOR)
    private Long id;

    @ManyToOne
    private Shipment shipment;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

}