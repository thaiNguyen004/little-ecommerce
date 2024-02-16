package thainguyen.detailproduct;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import thainguyen.detailproduct.DetailProduct;

@Repository
public interface DetailProductRepository
        extends JpaRepository<DetailProduct, Long> {
}
