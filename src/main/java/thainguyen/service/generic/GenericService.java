package thainguyen.service.generic;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import thainguyen.domain.Size;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface GenericService <T> {
    List<T> findAll();
    List<T> findAll(Pageable pageable);

    T findById(Long id);

    <V> V findById(Class<V> dataClass, Long id);

    T findById(Long id, LockModeType lockModeType);
    T findReferenceById(Long id);

}
