package thainguyen.data;

import org.springframework.data.jpa.repository.JpaRepository;
import thainguyen.domain.LineItem;

public interface LineItemRepository extends JpaRepository<LineItem, Long> {
}
