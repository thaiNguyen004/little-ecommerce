package thainguyen.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import thainguyen.domain.Discount;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
}
