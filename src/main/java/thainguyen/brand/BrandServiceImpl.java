package thainguyen.brand;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import thainguyen.generic.GenericServiceImpl;

import java.sql.SQLIntegrityConstraintViolationException;

@Service
@Slf4j
public class BrandServiceImpl extends GenericServiceImpl<Brand> implements BrandService{

    private final BrandRepository repo;

    public BrandServiceImpl(EntityManager em, BrandRepository repo) {
        super(em, Brand.class);
        this.repo = repo;
    }

    @Override
    public Brand create(Brand brand) throws SQLIntegrityConstraintViolationException {
        boolean isExist = repo.existsBrandsByName(brand.getName());
        if (isExist) {
            throw new SQLIntegrityConstraintViolationException("Brand with name = " + brand.getName() + "already existed");
        }
        return repo.save(brand);
    }


    @Override
    public Brand updateBrand(Long id, Brand brand) {
        Brand brandPersist = findById(id);
        if (brand.getName() != null) {
            brandPersist.setName(brand.getName());
        }
        if (brand.getLogo() != null) {
            brandPersist.setLogo(brand.getLogo());
        }
        return repo.save(brandPersist);
    }
}
