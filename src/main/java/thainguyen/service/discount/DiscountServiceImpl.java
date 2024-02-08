package thainguyen.service.discount;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import thainguyen.controller.exception.DiscountInvalidException;
import thainguyen.data.DiscountRepository;
import thainguyen.domain.Discount;
import thainguyen.service.generic.GenericServiceImpl;

import java.sql.SQLIntegrityConstraintViolationException;

@Service
public class DiscountServiceImpl extends GenericServiceImpl<Discount>
        implements DiscountService {

    private final DiscountRepository discountRepo;

    public DiscountServiceImpl(EntityManager em, DiscountRepository discountRepo) {
        super(em, Discount.class);
        this.discountRepo = discountRepo;
    }

    @Override
    public Discount create(Discount discount) throws SQLIntegrityConstraintViolationException {
        boolean isCodeExist = discountRepo.existsDiscountByCode(discount.getCode());
        if (isCodeExist) throw new SQLIntegrityConstraintViolationException("Discount with code = "+ discount.getCode() +" already exist");
        return discountRepo.save(discount);
    }

    @Override
    public Discount updateDiscount(Long id, Discount discount) throws DiscountInvalidException {
        Discount discountPersist = findById(id);
        if (discount.getCode() != null) {
            discountPersist.setCode(discount.getCode());
        }
        if (discount.getType() != null) {
            discountPersist.setType(discount.getType());
        }
        if (discount.getKind() != null) {
            discountPersist.setKind(discount.getKind());
        }
        if (discount.getQuantity() != null) {
            discountPersist.setQuantity(discount.getQuantity());
        }
        if (discount.getValue() != null) {
            discountPersist.setValue(discount.getValue());
            checkDiscount(discountPersist);
        }
        if (discount.getStart() != null) {
            discountPersist.setStart(discount.getStart());
        }
        if (discount.getEnd() != null) {
            discountPersist.setEnd(discount.getEnd());
        }
        return discountRepo.save(discountPersist);
    }

    public void checkDiscount(Discount discount) throws DiscountInvalidException {
        if (discount.getType().equals(Discount.Type.PERCENTAGE)) {
            if (discount.getValue() > 100) throw new DiscountInvalidException("Discount value must not greater than 100%");
            if (discount.getValue() < 1) throw new DiscountInvalidException("Discount value must not less than 1%");
        }
        if (discount.getValue() < 1) throw new DiscountInvalidException("Discount value must not less than 1");
    }
}
