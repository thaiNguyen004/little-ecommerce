package thainguyen.category;

import thainguyen.generic.GenericService;

import java.sql.SQLIntegrityConstraintViolationException;

public interface CategoryService extends GenericService<Category> {

    Category create(Category category) throws SQLIntegrityConstraintViolationException;

    Category updateCategory(Long id, Category category);
}
