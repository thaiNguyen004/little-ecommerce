package thainguyen.data;

import org.springframework.data.jpa.repository.JpaRepository;
import thainguyen.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
