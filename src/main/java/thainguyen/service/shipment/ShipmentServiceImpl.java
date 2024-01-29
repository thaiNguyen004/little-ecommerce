package thainguyen.service.shipment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import thainguyen.controller.exception.GhtkCreateOrderFailedException;
import thainguyen.data.ShipmentRepository;
import thainguyen.domain.*;
import thainguyen.domain.valuetypes.Price;
import thainguyen.dto.ghtk.GhtkForm;
import thainguyen.dto.ghtk.OrderGHTKDto;
import thainguyen.service.generic.GenericServiceImpl;
import thainguyen.service.user.UserService;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ShipmentServiceImpl extends GenericServiceImpl<Shipment>
        implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final UserService userService;
    private final RestTemplate restTemplate = new RestTemplate();

    public ShipmentServiceImpl(EntityManager em, ShipmentRepository shipmentRepository, UserService userService) {
        super(em, Shipment.class);
        this.shipmentRepository = shipmentRepository;
        this.userService = userService;
    }

    @Override
    public Shipment createShipment(Order order) throws JsonProcessingException, GhtkCreateOrderFailedException {
        OrderGHTKDto responseFromGhtk = createOrderGhtk(order);
        if (!responseFromGhtk.getSuccess()) {
            throw new GhtkCreateOrderFailedException(responseFromGhtk.getMessage());
        }
        Shipment shipment = new Shipment(order);

        /*Create Shipment*/
        Price fee = new Price(BigDecimal.valueOf(responseFromGhtk.getOrder().getFee())
                , Currency.getInstance("VND"));
        Price insuranceFee = new Price(BigDecimal.valueOf(responseFromGhtk.getOrder().getInsurance_fee())
                , Currency.getInstance("VND"));
        shipment.setFee(fee);
        shipment.setInsuranceFee(insuranceFee);
        shipment.setEstimatedPickTime(responseFromGhtk.getOrder().getEstimated_pick_time());
        shipment.setEstimatedDeliverTime(responseFromGhtk.getOrder().getEstimated_deliver_time());
        shipment.setLabelCode(responseFromGhtk.getOrder().getLabel());
        shipment.setTrackingID(responseFromGhtk.getOrder().getTracking_id());

        return shipmentRepository.save(shipment);
    }

    public OrderGHTKDto createOrderGhtk(Order order) throws JsonProcessingException {
        List<GhtkForm.GhtkProductForm> products = transferLineItemsToListGhtkProductForm(order.getLineItems());
        GhtkForm.GhtkOrderForm ghtkOrderForm = transferOrderToGhtkOrderForm(order);

        GhtkForm ghtkForm = new GhtkForm();
        ghtkForm.setProducts(products);
        ghtkForm.setOrder(ghtkOrderForm);

        ObjectWriter ow = new ObjectMapper()
                .writerWithDefaultPrettyPrinter();
        String json = ow.writeValueAsString(ghtkForm);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Token", Constants.TOKEN);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<GhtkForm> request = new HttpEntity<>(ghtkForm, headers);

        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        return restTemplate.postForObject(Constants.URI_GHTK_ORDER, request, OrderGHTKDto.class);
    }

    public List<GhtkForm.GhtkProductForm> transferLineItemsToListGhtkProductForm(List<LineItem> lineItems) {
        List<GhtkForm.GhtkProductForm> products = new ArrayList<>();

        for (LineItem lineItem : lineItems) {
            DetailProduct detailProduct = lineItem.getDetailProduct();
            products.add(new GhtkForm.GhtkProductForm(detailProduct.getName()
                    , detailProduct.getWeight(), lineItem.getQuantity(), detailProduct.getId()));
        }

        return products;
    }

    public GhtkForm.GhtkOrderForm transferOrderToGhtkOrderForm(Order order) {
        User customer = order.getUser();
        Optional<User> adminOpt = userService.findByUsername("admin");
        if (adminOpt.isEmpty()) {
            return null;
        }
        User admin = adminOpt.get();

        Address addressOfCustomer = order.getAddress();
        Address addressOfAdmin = admin.getAddresses().get(0);

        GhtkForm.GhtkOrderForm ghtkOrderForm = new GhtkForm.GhtkOrderForm();
        ghtkOrderForm.setId(order.getId().toString() + "m");

        // Customer info
        ghtkOrderForm.setName(customer.getFullname());
        ghtkOrderForm.setAddress(addressOfCustomer.getDetailAddress());
        ghtkOrderForm.setProvince(addressOfCustomer.getProvince());
        ghtkOrderForm.setDistrict(addressOfCustomer.getDistrict());
        ghtkOrderForm.setWard(addressOfCustomer.getWard());
        ghtkOrderForm.setHamlet("Khác");
        ghtkOrderForm.setTel(addressOfCustomer.getPhoneNumber());

        // Shop info
        ghtkOrderForm.setPick_name(admin.getFullname());
        ghtkOrderForm.setPick_address(addressOfAdmin.getDetailAddress());
        ghtkOrderForm.setPick_province(addressOfAdmin.getProvince());
        ghtkOrderForm.setPick_district(addressOfAdmin.getDistrict());
        ghtkOrderForm.setPick_ward(addressOfCustomer.getWard());
        ghtkOrderForm.setPick_tel(addressOfAdmin.getPhoneNumber());

        ghtkOrderForm.setIs_freeship(1);
        ghtkOrderForm.setPick_money(order.getTotalPriceBeforeDiscount().getValue().intValue());
        ghtkOrderForm.setValue(order.getTotalPriceBeforeDiscount().getValue().intValue());
        ghtkOrderForm.setEmail(customer.getEmail());

        return ghtkOrderForm;
    }
}

