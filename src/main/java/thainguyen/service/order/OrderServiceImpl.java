package thainguyen.service.order;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import thainguyen.data.OrderRepository;
import thainguyen.data.UserRepository;
import thainguyen.domain.Order;
import thainguyen.service.generic.GenericServiceImpl;

import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl extends GenericServiceImpl<Order>
        implements OrderService {

    private final OrderRepository orderRepo;
    private final UserRepository userRepo;

    public OrderServiceImpl(EntityManager em
            , OrderRepository orderRepo
            , UserRepository userRepo) {

        super(em, Order.class);
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
    }

    @Override
    public List<Order> findByOwner(String username) {
        return userRepo.findByUsername(username).map(orderRepo::findByUser).orElseGet(() -> null);
    }

    @Override
    public Optional<Order> findByIdAndOwner(Long id, String username) {
        return userRepo.findByUsername(username).map(user -> {
            return orderRepo.findByIdAndUser(id, user);
        }).orElseGet(() -> null);
    }

    @Override
    public Order create(Order order, String username) {
        return userRepo.findByUsername(username).map(user -> {
            order.setUser(user);
            return orderRepo.save(order);
        }).orElseGet(() -> null);
    }

    @Override
    public Order create(Order order) {
        return orderRepo.save(order);
    }

    @Override
    public Order updateByPut(Long id, Order order) {
        return null;
    }

    @Override
    public Order updateByPatch(Long id, Order order) {
        return null;
    }
}
