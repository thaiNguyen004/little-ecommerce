package thainguyen.product;

import thainguyen.generic.GenericService;

public interface ProductService extends GenericService<Product> {

    Product create(Product product);

    Product updateProduct(Long id, Product product);
}
