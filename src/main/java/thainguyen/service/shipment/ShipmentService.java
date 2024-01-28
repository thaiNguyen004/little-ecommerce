package thainguyen.service.shipment;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import thainguyen.domain.Order;
import thainguyen.domain.Shipment;
import thainguyen.service.generic.GenericService;

@Service
public interface ShipmentService extends GenericService<Shipment> {

    Shipment createShipment(Order order) throws JsonProcessingException;
}
