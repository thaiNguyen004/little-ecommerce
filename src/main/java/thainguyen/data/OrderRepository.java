package thainguyen.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import thainguyen.domain.Order;
import thainguyen.domain.valuetypes.Status;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query(value = "SELECT new Order(o.id, o.address, o.totalPriceBeforeDiscount, o.totalPriceAfterDiscount, o.status" +
            ", o.placedAt, o.modifiedAt) FROM Order o WHERE o.user.username = :username")
    List<Order> findByUsername(@Param("username") String username);

    @Query(value = "SELECT o FROM Order o WHERE o.id = :id AND o.user.username = :username")
    Optional<Order> findByIdAndUsername(UUID id, @Param("username") String username);

    @Query(value = "SELECT o.status FROM Order o WHERE o.id = :id AND o.user.username = :username")
    Status getStatus(@Param("id") UUID id, @Param("username") String username);
}
