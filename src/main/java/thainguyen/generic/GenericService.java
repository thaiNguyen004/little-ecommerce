package thainguyen.generic;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GenericService <T> {
    List<T> findAll();
    List<T> findAll(Pageable pageable);

    T findById(Long id);

    <V> V findById(Class<V> dataClass, Long id);

    T findById(Long id, LockModeType lockModeType);
    T findReferenceById(Long id);

}
