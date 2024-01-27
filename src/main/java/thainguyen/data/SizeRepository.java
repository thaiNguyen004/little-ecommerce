package thainguyen.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import thainguyen.domain.Size;

@Repository
public interface SizeRepository
        extends JpaRepository<Size, Long> {
}
