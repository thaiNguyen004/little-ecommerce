package thainguyen.product;

import org.springframework.data.jpa.repository.JpaRepository;
import thainguyen.product.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
