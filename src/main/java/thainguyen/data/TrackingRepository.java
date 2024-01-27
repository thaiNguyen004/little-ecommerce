package thainguyen.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import thainguyen.domain.Tracking;

@Repository
public interface TrackingRepository extends JpaRepository<Tracking, Long> {
}
