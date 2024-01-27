package thainguyen.data;

import org.springframework.data.jpa.repository.JpaRepository;
import thainguyen.domain.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
