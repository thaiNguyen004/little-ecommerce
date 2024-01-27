package thainguyen.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import thainguyen.domain.LineItem;

@Repository
public interface DetailOrderRepository extends JpaRepository<LineItem, Long> {
}
