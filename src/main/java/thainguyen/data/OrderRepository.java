package thainguyen.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import thainguyen.domain.Order;
import thainguyen.domain.valuetypes.Status;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = "SELECT o FROM Order o WHERE o.status IN ('SUCCESS', 'PENDING') AND o.user.username = :username")
    List<Order> findByUser(@Param("username") String username);

    @Query(value = "SELECT o FROM Order o WHERE o.id = :id AND o.status IN ('SUCCESS', 'PENDING') AND o.user.username = :username")
    Optional<Order> findByIdAndUser(Long id, @Param("username") String username);

    @Query(value = "SELECT o.status FROM Order o WHERE o.id = :id AND o.user.username = :username")
    Status getStatus(@Param("id") Long id, @Param("username") String username);
}
