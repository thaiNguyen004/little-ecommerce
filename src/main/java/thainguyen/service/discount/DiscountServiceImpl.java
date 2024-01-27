package thainguyen.service.discount;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import thainguyen.data.DiscountRepository;
import thainguyen.domain.Discount;
import thainguyen.service.generic.GenericServiceImpl;

@Service
public class DiscountServiceImpl extends GenericServiceImpl<Discount>
        implements DiscountService{

    private final DiscountRepository discountRepo;

    public DiscountServiceImpl(EntityManager em ,DiscountRepository discountRepo) {
        super(em, Discount.class);
        this.discountRepo = discountRepo;
    }

}
