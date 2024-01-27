package thainguyen.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import thainguyen.domain.DetailProduct;

@Repository
public interface DetailProductRepository
        extends JpaRepository<DetailProduct, Long> {
}
