package thainguyen.service.detailproduct;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import thainguyen.data.DetailProductRepository;
import thainguyen.domain.DetailProduct;
import thainguyen.service.generic.GenericServiceImpl;

@Service
public class DetailProductServiceImpl extends GenericServiceImpl<DetailProduct> implements DetailProductService {

    private final DetailProductRepository repo;

    public DetailProductServiceImpl(EntityManager em, DetailProductRepository repo) {
        super(em, DetailProduct.class);
        this.repo = repo;
    }

    @Override
    public DetailProduct create(DetailProduct detailProduct) {
        return repo.save(detailProduct);
    }

    @Override
    public DetailProduct updateByPut(Long id, DetailProduct detailProduct) {
        return repo.findById(id).map(detailProductPersist -> {
            detailProduct.setId(id);
            detailProduct.setVersion(detailProductPersist.getVersion());
            return repo.save(detailProduct);
        }).orElseGet(() -> null);
    }

    @Override
    public DetailProduct updateByPatch(Long id, DetailProduct detailProduct) {
        return repo.findById(id).map(detailProductPersist -> {
            if (detailProduct.getSize() != null) {
                detailProductPersist.setSize(detailProduct.getSize());
            }
            if (detailProduct.getPrice() != null) {
                detailProductPersist.setPrice(detailProduct.getPrice());
            }
            if (detailProduct.getProduct() != null) {
                detailProductPersist.setProduct(detailProduct.getProduct());
            }
            if (detailProduct.getWeight() != null) {
                detailProductPersist.setWeight(detailProduct.getWeight());
            }
            return repo.save(detailProductPersist);
        }).orElseGet(() -> null);
    }
}
