package thainguyen.service.order;

import thainguyen.domain.Order;
import thainguyen.service.generic.GenericService;

import java.util.List;
import java.util.Optional;

public interface OrderService extends GenericService<Order> {
    List<Order> findByOwner(String username);
    Optional<Order> findByIdAndOwner(Long id, String username);

    Order create(Order order, String username);

    Order create(Order order);

    Order updateByPut(Long id, Order order);

    Order updateByPatch(Long id, Order order);
}
