package thainguyen.category;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import thainguyen.generic.GenericServiceImpl;

import java.sql.SQLIntegrityConstraintViolationException;

@Service
@Slf4j
public class CategoryServiceImpl extends GenericServiceImpl<Category> implements CategoryService {

    private CategoryRepository repo;

    public CategoryServiceImpl(EntityManager em, CategoryRepository repo) {
        super(em, Category.class);
        this.repo = repo;
    }

    @Override
    public Category create(Category category) throws SQLIntegrityConstraintViolationException, IllegalArgumentException {
        boolean isExist = repo.existsByName(category.getName());
        if (isExist) {
            throw new SQLIntegrityConstraintViolationException("Category with name = " + category.getName() + " already existed");
        }
        if (category.getParent() != null) {
            Category parent = findById(category.getParent().getId());
            category.setParent(parent);
        }
        return repo.save(category);
    }

    @Override
    public Category updateCategory(Long id, Category categoryPatch) throws IllegalArgumentException  {
        Category categoryPersist = findById(id);
        if (categoryPatch.getName() != null) {
            categoryPersist.setName(categoryPatch.getName());
        }
        if (categoryPatch.getPicture() != null) {
            categoryPersist.setPicture(categoryPatch.getPicture());
        }
        if (categoryPatch.getDescription() != null) {
            categoryPersist.setDescription(categoryPatch.getDescription());
        }
        if (categoryPatch.getParent() != null) {
            categoryPersist.setParent(findById(categoryPatch.getParent().getId()));
        }
        return repo.save(categoryPersist);
    }

}
