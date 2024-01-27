package thainguyen.service.generic;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Slf4j
public class GenericServiceImpl <T> implements GenericService<T> {
    private final EntityManager em;
    private final Class<T> entityClass;

    public GenericServiceImpl(EntityManager em, Class<T> entityClass) {
        this.em = em;
        this.entityClass = entityClass;
    }

    @Override
    public List<T> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        query.select(query.from(entityClass));
        log.info(entityClass + " is retrieved success");
        return em.createQuery(query).getResultList();
    }

    @Override
    public List<T> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public Optional<T> findById(Long id) {
        T entity = em.find(entityClass, id);
        return Optional.ofNullable(entity);
    }

    @Override
    public T findById(Long id, LockModeType lockModeType) {
        return em.find(entityClass, id, lockModeType);
    }

    @Override
    public T findReferenceById(Long id) {
        return em.getReference(entityClass, id);
    }

}
