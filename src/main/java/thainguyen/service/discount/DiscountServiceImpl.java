package thainguyen.service.discount;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import thainguyen.data.DiscountRepository;
import thainguyen.domain.Discount;
import thainguyen.service.generic.GenericServiceImpl;

@Service
public class DiscountServiceImpl extends GenericServiceImpl<Discount>
        implements DiscountService {

    private final DiscountRepository discountRepo;

    public DiscountServiceImpl(EntityManager em, DiscountRepository discountRepo) {
        super(em, Discount.class);
        this.discountRepo = discountRepo;
    }

    @Override
    public Discount create(Discount discount)  {
        return discountRepo.save(discount);
    }

    @Override
    public Discount updateByPut(Long id, Discount discount) {
        return discountRepo.findById(id).map(persistDiscount -> {
            discount.setId(id);
            discount.setVersion(persistDiscount.getVersion());
            return discountRepo.save(discount);
        }).orElseGet(() -> null);
    }

    @Override
    public Discount updateByPatch(Long id, Discount discount) {
        return discountRepo.findById(id).map(persistDiscount -> {
            if (discount.getCode() != null) {
                persistDiscount.setCode(discount.getCode());
            }
            if (discount.getType() != null) {
                persistDiscount.setType(discount.getType());
            }
            if (discount.getKind() != null) {
                persistDiscount.setKind(discount.getKind());
            }
            if (discount.getQuantity() != null) {
                persistDiscount.setQuantity(discount.getQuantity());
            }
            if (discount.getValue() != null) {
                persistDiscount.setValue(discount.getValue());
            }
            if (discount.getStart() != null) {
                persistDiscount.setStart(discount.getStart());
            }
            if (discount.getEnd() != null) {
                persistDiscount.setEnd(discount.getEnd());
            }
            return discountRepo.save(persistDiscount);
        }).orElseGet(() -> null);
    }
}
