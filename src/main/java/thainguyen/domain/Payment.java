package thainguyen.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import thainguyen.domain.converter.PriceConverter;
import thainguyen.domain.valuetypes.Price;
import thainguyen.domain.valuetypes.Status;

import java.time.LocalDateTime;

import static thainguyen.domain.Constants.SEQUENCE_GENERATOR;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(generator = SEQUENCE_GENERATOR)
    private Long id;

    @ManyToOne(targetEntity = Order.class)
    private Order order;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Convert(converter = PriceConverter.class)
    private Price amount;

    @Enumerated(EnumType.STRING)
    private Status status;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDateTime date;

    @PrePersist
    void dateToPay() {
        date = LocalDateTime.now();
    }

    public static enum PaymentMethod {
        CAST, VNPAY, CREDITCARD, VISA, MASTERCARD, ZALOPAY, MOMO
    }

}



