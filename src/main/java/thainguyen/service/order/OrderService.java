package thainguyen.service.order;

import thainguyen.controller.exception.GhtkCreateOrderFailedException;
import thainguyen.domain.Order;
import thainguyen.domain.valuetypes.Status;
import thainguyen.service.generic.GenericService;

import java.util.List;
import java.util.UUID;

public interface OrderService extends GenericService<Order> {
    List<Order> findByOwner(String username);

    Order findByIdAndOwner(UUID id, String username);

    Status getStatus(UUID id, String username);

    Order create(Order order, String username);

    Order create(Order order);

    void cancel(UUID orderId) throws GhtkCreateOrderFailedException;

    Order updateStatus(UUID orderId, Status status);
}
