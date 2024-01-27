package thainguyen.service.brand;

import thainguyen.domain.Brand;
import thainguyen.service.generic.GenericService;

public interface BrandService extends GenericService<Brand> {

    Brand create(Brand brand);

    Brand updateByPut(Long id, Brand brand);

    Brand updateByPatch(Long id, Brand brand);
}

