package thainguyen.service.category;

import thainguyen.domain.Category;
import thainguyen.service.generic.GenericService;

import java.sql.SQLIntegrityConstraintViolationException;

public interface CategoryService extends GenericService<Category> {

    Category create(Category category) throws SQLIntegrityConstraintViolationException;

    Category updateCategory(Long id, Category category);
}
