package thainguyen.service.category;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import thainguyen.data.CategoryRepository;
import thainguyen.domain.Category;
import thainguyen.service.generic.GenericServiceImpl;

import java.util.Optional;

@Service
@Slf4j
public class CategoryServiceImpl extends GenericServiceImpl<Category> implements CategoryService {

    private CategoryRepository repo;

    public CategoryServiceImpl(EntityManager em, CategoryRepository repo) {
        super(em, Category.class);
        this.repo = repo;
    }

    @Override
    public Category create(Category category) {
        return repo.save(category);
    }

    @Override
    public Category updateByPut(Long id, Category categoryUpdate) {
        return repo.findById(id).map(category -> {
            categoryUpdate.setVersion(category.getVersion());
            categoryUpdate.setId(id);
            if (categoryUpdate.getParent() != null) {
                Optional<Category> categoryOpt = repo.findById(categoryUpdate.getParent().getId());
                categoryOpt.ifPresent(categoryUpdate::setParent);
                if (categoryOpt.isEmpty()) {
                    return null; // break
                }
            }
            return repo.save(categoryUpdate);
        }).orElseGet(() -> null);
    }


    @Override
    public Category updateByPatch(Long id, Category categoryPatch) {
        return repo.findById(id).map(category -> {
            if (categoryPatch.getName() != null) {
                category.setName(categoryPatch.getName());
            }
            if (categoryPatch.getPicture() != null) {
                category.setPicture(categoryPatch.getPicture());
            }
            if (categoryPatch.getDescription() != null) {
                category.setDescription(categoryPatch.getDescription());
            }
            if (categoryPatch.getParent() != null) {
                Optional<Category> categoryOpt = repo.findById(categoryPatch.getParent().getId());
                categoryOpt.ifPresent(category::setParent);
                if (categoryOpt.isEmpty()) {
                    return null; // break
                }
            }
            return repo.save(category);
        }).orElseGet(() -> null);
    }
}
