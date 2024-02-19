package thainguyen.tracking;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaPredicate;
import org.hibernate.query.criteria.JpaRoot;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TrackingDAOImpl implements TrackingDAO{

    private final EntityManager em;

    public TrackingDAOImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<Tracking> findAllTrackingByShipmentId(Long shipmentId) {
        HibernateCriteriaBuilder builder = em.unwrap(Session.class).getCriteriaBuilder();
        JpaCriteriaQuery<Tracking> mainQuery = builder.createQuery(Tracking.class);
        JpaRoot<Tracking> trackingRoot = mainQuery.from(Tracking.class);

        JpaPredicate eqShipmentId = builder.equal(
                trackingRoot.get(Tracking_.shipment).get("id"),
                shipmentId
        );

        mainQuery.select(trackingRoot).where(eqShipmentId);
        List<Tracking> result = em.createQuery(mainQuery).getResultList();
        if (!result.isEmpty()) return result;
        throw new NoResultException("No tracking found");
    }
}
