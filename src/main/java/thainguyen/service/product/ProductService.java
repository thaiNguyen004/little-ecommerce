package thainguyen.service.product;

import thainguyen.domain.Product;
import thainguyen.service.generic.GenericService;

public interface ProductService extends GenericService<Product> {

    Product create(Product product);

    Product updateProduct(Long id, Product product);
}
