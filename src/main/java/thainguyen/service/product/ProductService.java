package thainguyen.service.product;

import thainguyen.domain.Product;
import thainguyen.service.generic.GenericService;

public interface ProductService extends GenericService<Product> {

    Product create(Product product);

    Product updateByPut(Long id, Product product);

    Product updateByPatch(Long id, Product product);
}
