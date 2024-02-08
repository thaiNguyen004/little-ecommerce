package thainguyen.service.generic;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Slf4j
public abstract class GenericServiceImpl <T> implements GenericService<T> {
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
        List<T> resultList = em.createQuery(query).getResultList();
        if (! resultList.isEmpty()) return resultList;
        throw new NoResultException(entityClass.getName() + " not found");
    }

    @Override
    public List<T> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public T findById(Long id) throws NoResultException {
        try {
            T result = em.find(entityClass, id);
            if (result != null) return result;
            String messageError = "Invalid " + entityClass.getName().toLowerCase()
                    + " ID, " + entityClass.getName().toLowerCase() + " not found";
            throw new NoResultException(messageError);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(entityClass + " id " + " required not null");
        }
    }

    @Override
    public <V> V findById(Class<V> dataClass, Long id) {
        V result = em.find(dataClass, id);
        if (result != null) return result;
        String messageError = "Invalid " + dataClass.getName().toLowerCase()
                + " ID, " + dataClass.getName().toLowerCase() + " not found";
        throw new NoResultException(messageError);
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
