package thainguyen.service.order;

import thainguyen.controller.exception.GhtkCreateOrderFailedException;
import thainguyen.domain.Order;
import thainguyen.domain.valuetypes.Status;
import thainguyen.service.generic.GenericService;

import java.util.List;

public interface OrderService extends GenericService<Order> {
    List<Order> findByOwner(String username);

    Order findByIdAndOwner(Long id, String username);

    Status getStatus(Long id, String username);

    Order create(Order order, String username);

    Order create(Order order);

    void cancel(String orderId) throws GhtkCreateOrderFailedException;

    Order updateStatus(Long orderId, Status status);
}
