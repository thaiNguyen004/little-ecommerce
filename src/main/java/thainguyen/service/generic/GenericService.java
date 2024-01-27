package thainguyen.service.generic;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface GenericService <T> {
    List<T> findAll();
    List<T> findAll(Pageable pageable);

    Optional<T> findById(Long id);
    T findById(Long id, LockModeType lockModeType);
    T findReferenceById(Long id);

}
