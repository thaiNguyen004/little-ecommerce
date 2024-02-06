package thainguyen.service.detailproduct;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import thainguyen.data.DetailProductRepository;
import thainguyen.domain.DetailProduct;
import thainguyen.domain.Product;
import thainguyen.domain.Size;
import thainguyen.service.generic.GenericServiceImpl;
import thainguyen.service.product.ProductService;
import thainguyen.service.size.SizeService;

import java.io.IOException;
import java.util.function.Function;

@Service
@Slf4j
public class DetailProductServiceImpl extends GenericServiceImpl<DetailProduct>
        implements DetailProductService {

    private final DetailProductRepository repo;
    private final ProductService productService;
    private final SizeService sizeService;

    public DetailProductServiceImpl(EntityManager em, DetailProductRepository repo , SizeService sizeService
            , ProductService productService) {
        super(em, DetailProduct.class);
        this.repo = repo;
        this.sizeService = sizeService;
        this.productService = productService;
    }

    @Override
    public DetailProduct create(DetailProduct detailProduct) {
        detailProduct.setSize(sizeService.findById(detailProduct.getSize().getId()));
        detailProduct.setProduct(productService.findById(detailProduct.getProduct().getId()));
        return repo.save(detailProduct);

    }

    @Override
    public DetailProduct updateDetailProduct(Long id, DetailProduct detailProduct) {
        DetailProduct detailProductPersist = findById(id);
        if (detailProduct.getSize() != null) {
            detailProductPersist.setSize(sizeService.findById(detailProduct.getSize().getId()));
        }
        if (detailProduct.getPrice() != null) {
            detailProductPersist.setPrice(detailProduct.getPrice());
        }
        if (detailProduct.getProduct() != null) {
            detailProductPersist.setProduct(productService.findById(detailProduct.getProduct().getId()));
        }
        if (detailProduct.getWeight() != null) {
            detailProductPersist.setWeight(detailProduct.getWeight());
        }
        return repo.save(detailProductPersist);
    }

}
