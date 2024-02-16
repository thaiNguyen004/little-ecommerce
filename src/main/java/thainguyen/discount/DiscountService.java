package thainguyen.discount;

import thainguyen.generic.GenericService;

import java.sql.SQLIntegrityConstraintViolationException;

public interface DiscountService extends GenericService<Discount> {
    Discount create(Discount discount) throws SQLIntegrityConstraintViolationException;

    Discount updateDiscount(Long id, Discount discount) throws DiscountInvalidException;

    void checkDiscount(Discount discount) throws DiscountInvalidException;
}
