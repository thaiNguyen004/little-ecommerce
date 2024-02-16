package thainguyen.product;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import thainguyen.brand.Brand;
import thainguyen.category.Category;
import thainguyen.brand.BrandService;
import thainguyen.category.CategoryService;
import thainguyen.generic.GenericServiceImpl;

@Service
public class ProductServiceImpl extends GenericServiceImpl<Product> implements ProductService {
    private final ProductRepository repo;
    private final CategoryService categoryService;
    private final BrandService brandService;

    public ProductServiceImpl(EntityManager em, ProductRepository repo
            , BrandService brandService, CategoryService categoryService) {
        super(em, Product.class);
        this.repo = repo;
        this.brandService = brandService;
        this.categoryService = categoryService;
    }

    @Override
    public Product create(Product product) {
        Brand brand = brandService.findById(product.getBrand().getId());
        Category category = categoryService.findById(product.getCategory().getId());
        product.setBrand(brand);
        product.setCategory(category);
        return repo.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product productChanged) {
        Product productPersist = findById(id);
        if (productChanged.getName() != null) {
            productPersist.setName(productChanged.getName());
        }
        if (productChanged.getDescription() != null) {
            productPersist.setDescription(productChanged.getDescription());
        }
        if (productChanged.getBrand() != null) {
            productPersist.setBrand(brandService.findById(productChanged.getBrand().getId()));
        }
        if (productChanged.getCategory() != null) {
            productPersist.setCategory(categoryService.findById(productChanged.getCategory().getId()));
        }
        return repo.save(productPersist);
    }
}
