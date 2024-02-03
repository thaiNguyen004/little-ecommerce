package thainguyen.service.brand;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import thainguyen.data.BrandRepository;
import thainguyen.domain.Brand;
import thainguyen.service.generic.GenericServiceImpl;

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
    public Brand updateByPut(Long id, Brand brand) {
        return repo.findById(id).map(b -> {
            brand.setVersion(b.getVersion());
            brand.setId(id);
            return repo.save(brand);
        }).orElseGet(() -> null);
    }

    @Override
    public Brand updateByPatch(Long id, Brand brand) {
        return repo.findById(id).map(b -> {
            if (brand.getName() != null) {
                b.setName(brand.getName());
            }
            if (brand.getLogo() != null) {
                b.setLogo(brand.getLogo());
            }
            return repo.save(b);
        }).orElseGet(() -> null);
    }
}
