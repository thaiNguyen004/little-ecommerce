package thainguyen.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import thainguyen.converter.PriceConverter;
import thainguyen.valuetype.Price;
import thainguyen.valuetype.Status;
import thainguyen.order.Order;

import java.time.LocalDateTime;

import static thainguyen.utility.constant.Constants.SEQUENCE_GENERATOR;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(generator = SEQUENCE_GENERATOR)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Order.class)
    private Order order;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "paymentMethod attribute must not be null")
    private PaymentMethod paymentMethod;

    @NotNull(message = "amount attribute must not be null")
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "status attribute must not be null")
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



