package thainguyen.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import thainguyen.domain.Size;
import thainguyen.domain.valuetypes.Status;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class OrderSimpleDto {
    private UUID orderId;
    private Long numberOfLineItem;
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
