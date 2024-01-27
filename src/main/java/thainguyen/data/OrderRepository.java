package thainguyen.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import thainguyen.domain.Order;
import thainguyen.domain.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);
    Optional<Order> findByIdAndUser(Long id, User user);
}
