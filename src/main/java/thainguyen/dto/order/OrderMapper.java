package thainguyen.dto.order;

import thainguyen.domain.Order;

public interface OrderMapper {
    Order convertOrderDtoToEntity(OrderDto orderDto);

}
