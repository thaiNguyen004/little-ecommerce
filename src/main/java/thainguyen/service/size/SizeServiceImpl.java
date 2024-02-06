package thainguyen.service.size;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import thainguyen.data.SizeRepository;
import thainguyen.domain.Brand;
import thainguyen.domain.Category;
import thainguyen.domain.Size;
import thainguyen.service.brand.BrandService;
import thainguyen.service.category.CategoryService;
import thainguyen.service.generic.GenericServiceImpl;

@Service
public class SizeServiceImpl extends GenericServiceImpl<Size> implements SizeService {

    private final SizeRepository repo;
    private final CategoryService categoryService;
    private final BrandService brandService;

    public SizeServiceImpl(EntityManager em, SizeRepository repo,
                           BrandService brandService, CategoryService categoryService) {
        super(em, Size.class);
        this.repo = repo;
        this.brandService= brandService;
        this.categoryService = categoryService;
    }

    @Override
    public Size create(Size size) {
        size.setBrand(brandService.findById(size.getBrand().getId()));
        size.setCategory(categoryService.findById(size.getCategory().getId()));
        return repo.save(size);
    }

    @Override
    public Size updateSize(Long id, Size size) {
        Size sizePersist = findById(id);
        if (size.getName() != null) {
            sizePersist.setName(size.getName());
        }
        if (size.getChest() != null) {
            sizePersist.setChest(size.getChest());
        }
        if (size.getWidth() != null) {
            sizePersist.setWidth(size.getWidth());
        }
        if (size.getLength() != null) {
            sizePersist.setLength(size.getLength());
        }
        if (size.getBrand() != null) {
            sizePersist.setBrand(brandService.findById(size.getBrand().getId()));
        }
        if (size.getCategory() != null) {
            sizePersist.setCategory(categoryService.findById(size.getCategory().getId()));
        }
        return repo.save(sizePersist);
    }
}
