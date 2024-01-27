package thainguyen.service.detailproduct;

import thainguyen.domain.DetailProduct;
import thainguyen.service.generic.GenericService;

public interface DetailProductService extends GenericService<DetailProduct> {

    DetailProduct create(DetailProduct detailProduct);

    DetailProduct updateByPut(Long id, DetailProduct detailProduct);
    DetailProduct updateByPatch(Long id, DetailProduct detailProduct);

}
