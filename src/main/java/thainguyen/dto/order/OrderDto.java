package thainguyen.dto.order;

import lombok.Getter;
import lombok.Setter;
import thainguyen.domain.Address;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderDto {
    private List<LineItemDto> lineItems = new ArrayList<>();
    private List<Long> discounts = new ArrayList<>();
    private Address address;

    @Getter
    @Setter
    public static class LineItemDto {
        private Long detailProductId;
        private Integer quantity;
    }
}
