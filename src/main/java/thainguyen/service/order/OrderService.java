package thainguyen.service.order;

import thainguyen.domain.Order;
import thainguyen.domain.valuetypes.Status;
import thainguyen.service.generic.GenericService;

import java.util.List;
import java.util.Optional;

public interface OrderService extends GenericService<Order> {
    List<Order> findByOwner(String username);
    Optional<Order> findByIdAndOwner(Long id, String username);

    Status getStatus(Long id, String username);

    Order create(Order order, String username);

    Order create(Order order);

    boolean cancel(Long orderId);

    Order updateStatus(Long orderId, Status status);
}
