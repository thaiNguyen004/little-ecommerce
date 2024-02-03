package thainguyen.data;

import org.springframework.data.jpa.repository.JpaRepository;
import thainguyen.domain.Brand;


public interface BrandRepository extends JpaRepository<Brand, Long>{

    boolean existsBrandsByName(String name);

}