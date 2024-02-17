package thainguyen.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import thainguyen.size.Size;
import thainguyen.valuetype.Status;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class OrderSimpleDto {
    private UUID orderId;
    private Long numberOfLineItem;
    private Long productId;
    private String productName;
    private String productPicture;
    private Size.Name sizeName;
    private Integer amount;
    private Integer quantity;
    private Integer totalPriceBeforeDiscount;
    private Integer totalPriceAfterDiscount;
    private Status status;
    private LocalDateTime placedAt;
    private LocalDateTime modifiedAt;
}