package thainguyen.service.category;

import thainguyen.domain.Category;
import thainguyen.service.generic.GenericService;

public interface CategoryService extends GenericService<Category> {

    Category create(Category category);

    Category updateByPut(Long id, Category category);

    Category updateByPatch(Long id, Category category);
}
