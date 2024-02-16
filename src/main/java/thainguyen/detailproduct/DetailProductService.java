package thainguyen.detailproduct;

import thainguyen.generic.GenericService;

public interface DetailProductService extends GenericService<DetailProduct> {

    DetailProduct create(DetailProduct detailProduct);

    DetailProduct updateDetailProduct(Long id, DetailProduct detailProduct);
}
