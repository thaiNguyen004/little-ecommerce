package thainguyen.service.product;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import thainguyen.data.ProductRepository;
import thainguyen.domain.Product;
import thainguyen.service.generic.GenericServiceImpl;

import java.util.Optional;

@Service
public class ProductServiceImpl extends GenericServiceImpl<Product> implements ProductService {
    private final ProductRepository repo;

    public ProductServiceImpl(EntityManager em, ProductRepository repo) {
        super(em, Product.class);
        this.repo = repo;
    }

    @Override
    public Product create(Product product) {
        return repo.save(product);
    }

    @Override
    public Product updateByPut(Long id, Product product) {
        Optional<Product> productPersist = repo.findById(id);
        return productPersist.map(p -> {
            product.setId(id);
            product.setVersion(p.getVersion());
            return repo.save(product);
        }).orElseGet(() -> null);
    }

    @Override
    public Product updateByPatch(Long id, Product productChanged) {
        Optional<Product> productPersist = repo.findById(id);
        return productPersist.map(product -> {
            if (productChanged.getName() != null) {
                product.setName(productChanged.getName());
            }
            if (productChanged.getDescription() != null) {
                product.setDescription(productChanged.getDescription());
            }
            if (productChanged.getBrand() != null) {
                product.setBrand(productChanged.getBrand());
            }
            if (productChanged.getCategory() != null) {
                product.setCategory(productChanged.getCategory());
            }
            return repo.save(product);
        }).orElseGet(() -> null);
    }
}
