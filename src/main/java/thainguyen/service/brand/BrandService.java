package thainguyen.service.brand;

import thainguyen.domain.Brand;
import thainguyen.service.generic.GenericService;

import java.sql.SQLIntegrityConstraintViolationException;

public interface BrandService extends GenericService<Brand> {

    Brand create(Brand brand) throws SQLIntegrityConstraintViolationException;

    Brand updateByPut(Long id, Brand brand);

    Brand updateByPatch(Long id, Brand brand);
}

