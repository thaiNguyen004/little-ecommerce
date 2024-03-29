package thainguyen.shipment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import thainguyen.address.Address;
import thainguyen.order.GhtkCreateOrderFailedException;
import thainguyen.detailproduct.DetailProduct;
import thainguyen.lineitem.LineItem;
import thainguyen.order.Order;
import thainguyen.generic.GenericServiceImpl;
import thainguyen.user.User;
import thainguyen.user.UserService;
import thainguyen.utility.constant.Constants;

import java.util.*;

@Service
public class ShipmentServiceImpl extends GenericServiceImpl<Shipment>
        implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final UserService userService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    public ShipmentServiceImpl(EntityManager em, ShipmentRepository shipmentRepository
            , UserService userService, ObjectMapper objectMapper) {
        super(em, Shipment.class);
        this.shipmentRepository = shipmentRepository;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Shipment createShipment(Order order) throws JsonProcessingException, GhtkCreateOrderFailedException {
        OrderGHTKDto responseFromGhtk = createOrderGhtk(order);
        if (!responseFromGhtk.getSuccess()) {
            throw new GhtkCreateOrderFailedException(responseFromGhtk.getMessage());
        }
        Shipment shipment = new Shipment(order);

        /*Create Shipment*/
        shipment.setFee(responseFromGhtk.getOrder().getFee());
        shipment.setInsuranceFee(responseFromGhtk.getOrder().getInsurance_fee());
        shipment.setEstimatedPickTime(responseFromGhtk.getOrder().getEstimated_pick_time());
        shipment.setEstimatedDeliverTime(responseFromGhtk.getOrder().getEstimated_deliver_time());
        shipment.setLabelCode(responseFromGhtk.getOrder().getLabel());
        shipment.setTrackingID(responseFromGhtk.getOrder().getTracking_id());

        return shipmentRepository.save(shipment);
    }

    public OrderGHTKDto createOrderGhtk(Order order) throws JsonProcessingException {
        List<GhtkFormDto.GhtkProductForm> products = transferLineItemsToListGhtkProductForm(order.getLineItems());
        GhtkFormDto.GhtkOrderForm ghtkOrderForm = transferOrderToGhtkOrderForm(order);

        GhtkFormDto ghtkForm = new GhtkFormDto();
        ghtkForm.setProducts(products);
        ghtkForm.setOrder(ghtkOrderForm);

        // LOG
        ObjectWriter ow = new ObjectMapper()
                .writerWithDefaultPrettyPrinter();
        String json = ow.writeValueAsString(ghtkForm);
        System.out.println(json);
        // LOG

        HttpHeaders headers = new HttpHeaders();
        headers.add("Token", Constants.TOKEN);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<GhtkFormDto> request = new HttpEntity<>(ghtkForm, headers);

//        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        OrderGHTKDto orderGHTKDto = null;
        try {
            orderGHTKDto = restTemplate.postForObject(Constants.URI_GHTK_ORDER, request, OrderGHTKDto.class);
        } catch (HttpClientErrorException.UnprocessableEntity e) {
            String responseBody = e.getResponseBodyAsString();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            orderGHTKDto = objectMapper.readValue(responseBody, OrderGHTKDto.class);
        }
        return orderGHTKDto;
    }

    public List<GhtkFormDto.GhtkProductForm> transferLineItemsToListGhtkProductForm(List<LineItem> lineItems) {
        List<GhtkFormDto.GhtkProductForm> products = new ArrayList<>();

        for (LineItem lineItem : lineItems) {
            DetailProduct detailProduct = lineItem.getDetailProduct();
            products.add(new GhtkFormDto.GhtkProductForm(detailProduct.getProduct().getName()
                    , detailProduct.getWeight(), lineItem.getQuantity(), detailProduct.getId()));
        }

        return products;
    }

    public GhtkFormDto.GhtkOrderForm transferOrderToGhtkOrderForm(Order order) {
        User customer = order.getUser();
        User admin = userService.findByUsername("admin");

        Address addressOfCustomer = order.getAddress();
        Address addressOfAdmin = admin.getAddresses().get(0);

        GhtkFormDto.GhtkOrderForm ghtkOrderForm = new GhtkFormDto.GhtkOrderForm();
        ghtkOrderForm.setId(order.getId().toString());

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
        ghtkOrderForm.setPick_money(order.getTotalPriceBeforeDiscount());
        ghtkOrderForm.setValue(order.getTotalPriceBeforeDiscount());
        ghtkOrderForm.setEmail(customer.getEmail());

        return ghtkOrderForm;
    }
}

