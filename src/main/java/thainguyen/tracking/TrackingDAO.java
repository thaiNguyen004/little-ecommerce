package thainguyen.tracking;

import java.util.List;

public interface TrackingDAO {

    List<Tracking> findAllTrackingByShipmentId(Long shipmentId);

}
