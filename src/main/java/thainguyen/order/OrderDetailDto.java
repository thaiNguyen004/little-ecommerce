package thainguyen.order;

import lombok.*;
import thainguyen.address.Address_;
import thainguyen.payment.Payment;
import thainguyen.size.Size;
import thainguyen.valuetype.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailDto {
    private UUID orderId;
    private List<LineItem> lineItems = new ArrayList<>();
    private Address address;
    private Integer totalPriceBeforeDiscount;
    private Integer totalPriceAfterDiscount;
    private Shipment shipment;
    private Status currentStatus;
    private Payment.PaymentMethod paymentMethod;
    private List<Tracking> trackings = new ArrayList<>();
    private LocalDateTime placedAt;
    private LocalDateTime modifiedAt;

    @Getter
    @Setter
    @Builder
    public static class LineItem {
        private Long id;
        private Long productId;
        private String productName;
        private String productPicture;
        private Size.Name sizeName;
        private Integer amount;
        private Integer quantity;
    }

    @Getter
    @Setter
    @Builder
    public static class Address {
        private String fullname;
        private String province;
        private String district;
        private String ward;
        private String detailAddress;
        private String phoneNumber;
    }

    @Getter
    @Setter
    @Builder
    public static class Tracking {
        private Long statusNumber;
        private String statusText;
        private LocalDateTime date;
    }

    @Getter
    @Setter
    @Builder
    public static class Shipment {
        private String labelCode;
        private Integer fee;
        private Integer insuranceFee;
    }


}
