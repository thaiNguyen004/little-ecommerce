package thainguyen.shipment;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import thainguyen.order.GhtkCreateOrderFailedException;
import thainguyen.order.Order;
import thainguyen.generic.GenericService;

@Service
public interface ShipmentService extends GenericService<Shipment> {

    Shipment createShipment(Order order) throws JsonProcessingException, GhtkCreateOrderFailedException;
}
