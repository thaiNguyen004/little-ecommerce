package thainguyen.order;

import lombok.*;
import thainguyen.size.Size;
import thainguyen.valuetype.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class OrderDetailDto {
    private UUID orderId;
    private List<LineItem> lineItems = new ArrayList<>();
    private Integer totalPriceBeforeDiscount;
    private Integer totalPriceAfterDiscount;
    private Status status;
    private LocalDateTime placedAt;
    private LocalDateTime modifiedAt;

    @Getter
    @Setter
    @Builder
    public static class LineItem {
        private Long productId;
        private String productName;
        private String productPicture;
        private Size.Name sizeName;
        private Integer amount;
        private Integer quantity;
    }

    public OrderDetailDto() {
    }
}
