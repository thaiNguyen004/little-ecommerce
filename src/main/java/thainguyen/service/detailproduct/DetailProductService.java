package thainguyen.service.detailproduct;

import thainguyen.domain.DetailProduct;
import thainguyen.service.generic.GenericService;

import java.util.function.Function;

public interface DetailProductService extends GenericService<DetailProduct> {

    DetailProduct create(DetailProduct detailProduct);

    DetailProduct updateDetailProduct(Long id, DetailProduct detailProduct);
}
