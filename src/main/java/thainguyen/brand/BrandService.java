package thainguyen.brand;

import thainguyen.generic.GenericService;

import java.sql.SQLIntegrityConstraintViolationException;

public interface BrandService extends GenericService<Brand> {

    Brand create(Brand brand) throws SQLIntegrityConstraintViolationException;

    Brand updateBrand(Long id, Brand brand);
}

