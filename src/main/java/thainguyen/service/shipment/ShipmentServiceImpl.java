package thainguyen.service.shipment;

import static thainguyen.dto.ghtk.GhtkForm.GhtkProduct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.data.ShipmentRepository;
import thainguyen.domain.*;
import thainguyen.domain.valuetypes.Price;
import thainguyen.dto.ghtk.FeeDeliveryDto;
import thainguyen.dto.ghtk.GhtkDeliveryForm;
import thainguyen.dto.ghtk.GhtkForm;
import thainguyen.dto.ghtk.OrderGHTKDto;
import thainguyen.service.generic.GenericServiceImpl;
import thainguyen.service.user.UserService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Service
public class ShipmentServiceImpl extends GenericServiceImpl<Shipment>
        implements ShipmentService {

    private final ShipmentRepository shipmentRepo;
    private final UserService userService;
    private final RestTemplate restTemplate = new RestTemplate();

    public ShipmentServiceImpl(EntityManager em, ShipmentRepository shipmentRepo
            , UserService userService) {
        super(em, Shipment.class);
        this.shipmentRepo = shipmentRepo;
        this.userService = userService;
    }

    @Override
    public Shipment createShipmentFromGhtk(Order order) {
        GhtkDeliveryForm ghtkDeliveryForm = transferFromOrderToFeeGhtkForm(order);
        FeeDeliveryDto feeDeliveryDto = createFeeDeliveryDto(ghtkDeliveryForm);
        // Creating Shipment
        if (! feeDeliveryDto.isSuccess()) return null;
        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setFee(new Price(
                BigDecimal.valueOf(feeDeliveryDto.getFee().getFee().longValue())
                , Currency.getInstance("VND")));
        shipment.setInsuranceFee(new Price(
                BigDecimal.valueOf(feeDeliveryDto.getFee().getInsurance_fee().longValue())
                , Currency.getInstance("VND")));
        GhtkForm ghtkForm = transferToGhtkForm(order, feeDeliveryDto, ghtkDeliveryForm);
        OrderGHTKDto orderGHTKDto = createOrderGhtkDto(ghtkForm);
        if (orderGHTKDto == null) return null;
        shipment.setLabelCode(orderGHTKDto.getOrder().getLabel());
        shipment.setEstimatedDeliverTime(orderGHTKDto.getOrder().getEstimated_deliver_time());
        shipment.setEstimatedPickTime(orderGHTKDto.getOrder().getEstimated_pick_time());
        shipment.setTrackingID(orderGHTKDto.getOrder().getTracking_id());
        return shipment;
    }

    private OrderGHTKDto createOrderGhtkDto(GhtkForm ghtkForm) {
        try {
            ObjectWriter ow = new ObjectMapper()
                    .writerWithDefaultPrettyPrinter();
            String json = ow.writeValueAsString(ghtkForm);
            System.out.println(json);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Token", Constants.TOKEN);
            HttpEntity<GhtkForm> request = new HttpEntity<>(ghtkForm, headers);
            return restTemplate.postForObject(Constants.URI_GHTK + "/services/shipment/order/?ver=1.5"
                    , request, OrderGHTKDto.class);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private GhtkForm transferToGhtkForm(Order order, FeeDeliveryDto feeDeliveryDto
            , GhtkDeliveryForm ghtkDeliveryForm) {

        GhtkForm ghtkForm = new GhtkForm();
        GhtkForm.GhtkOrder orderGhtk = new GhtkForm.GhtkOrder();
        List<GhtkProduct> products = new ArrayList<>();

        for (LineItem lt : order.getLineItems()) {
            DetailProduct detailProduct = lt.getDetailProduct();
            products.add(new GhtkProduct(detailProduct.getName()
                    , detailProduct.getWeight(), lt.getQuantity(), detailProduct.getId()));
        }

        orderGhtk.setPick_name(ghtkDeliveryForm.getPick_name());
        orderGhtk.setPick_province(ghtkDeliveryForm.getAddress().getPick_province());
        orderGhtk.setPick_district(ghtkDeliveryForm.getAddress().getPick_district());
        orderGhtk.setPick_ward(ghtkDeliveryForm.getAddress().getPick_ward());
        orderGhtk.setPick_address(ghtkDeliveryForm.getAddress().getPick_address());
        orderGhtk.setPick_tel(ghtkDeliveryForm.getAddress().getPick_tel());

        orderGhtk.setName(ghtkDeliveryForm.getName());
        orderGhtk.setProvince(ghtkDeliveryForm.getAddress().getProvince());
        orderGhtk.setDistrict(ghtkDeliveryForm.getAddress().getDistrict());
        orderGhtk.setWard(ghtkDeliveryForm.getAddress().getWard());
        orderGhtk.setAddress(ghtkDeliveryForm.getAddress().getAddress());
        orderGhtk.setHamlet(ghtkDeliveryForm.getAddress().getHamlet());
        orderGhtk.setTel(ghtkDeliveryForm.getAddress().getTel());
        orderGhtk.setEmail(order.getUser().getEmail());

        orderGhtk.setId(order.getId().toString());
        orderGhtk.setIs_freeship(1);
        orderGhtk.setValue(feeDeliveryDto.getFee().getInsurance_fee());

        Integer feeShip = feeDeliveryDto.getFee().getFee();
        Integer totalPrice = order.getTotalPrice().getValue().intValue();
        Integer cod = 0;
        List<Discount> discounts = order.getDiscounts();
        if (discounts == null || discounts.isEmpty()) {
            cod = feeShip + totalPrice;
        } else {
            for (Discount discount : discounts) {
                if (discount.getKind().equals(Discount.Kind.FREESHIP)) {
                    if (discount.getType().equals(Discount.Type.AMOUNT)) {
                        feeShip = feeShip - discount.getValue();
                    } else {
                        feeShip = feeShip - (feeShip * discount.getValue());
                    }
                    cod = cod + feeShip;
                }
                if (discount.getKind().equals(Discount.Kind.NORMAL)) {
                    if (discount.getType().equals(Discount.Type.AMOUNT)) {
                        totalPrice = totalPrice - discount.getValue();
                    } else {
                        totalPrice = totalPrice - (totalPrice * discount.getValue());
                    }
                    cod = cod + totalPrice;
                }
            }
        }
        orderGhtk.setPick_money(cod);
        ghtkForm.setOrder(orderGhtk);
        ghtkForm.setProducts(products);
        return ghtkForm;
    }

    private GhtkDeliveryForm transferFromOrderToFeeGhtkForm(Order order) {
        Optional<User> admin = userService.findByUsername("admin");
        Address adminAddress = admin.get().getAddresses().get(0);
        Address customerAddress = order.getAddress();

        GhtkDeliveryForm ghtkDeliveryForm = new GhtkDeliveryForm();
        ghtkDeliveryForm.setName(order.getUser().getFullname());
        ghtkDeliveryForm.setPick_name(admin.get().getFullname());

        ghtkDeliveryForm.getAddress().setPick_province(adminAddress.getProvince());
        ghtkDeliveryForm.getAddress().setPick_district(adminAddress.getDistrict());
        ghtkDeliveryForm.getAddress().setPick_ward(adminAddress.getWard());
        ghtkDeliveryForm.getAddress().setPick_address(adminAddress.getDetailAddress());
        ghtkDeliveryForm.getAddress().setPick_tel(adminAddress.getPhoneNumber());

        ghtkDeliveryForm.getAddress().setProvince(customerAddress.getProvince());
        ghtkDeliveryForm.getAddress().setDistrict(customerAddress.getDistrict());
        ghtkDeliveryForm.getAddress().setWard(customerAddress.getWard());
        ghtkDeliveryForm.getAddress().setAddress(customerAddress.getDetailAddress());
        ghtkDeliveryForm.getAddress().setHamlet(customerAddress.getHamlet());
        ghtkDeliveryForm.getAddress().setTel(customerAddress.getPhoneNumber());

        ghtkDeliveryForm.setValue(order.getTotalPrice().getValue().intValue());

        Double totalWeightOrder = 0.0;
        for (LineItem lineItem : order.getLineItems()) {
            totalWeightOrder += (lineItem.getTotalWeight() * 1000);
        }
        ghtkDeliveryForm.setWeight(totalWeightOrder.intValue());
        return ghtkDeliveryForm;
    }

    private FeeDeliveryDto createFeeDeliveryDto(GhtkDeliveryForm feeGhtkForm) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Constants.URI_GHTK + "/services/shipment/fee");
        builder.queryParam("pick_province", feeGhtkForm.getAddress().getPick_province());
        builder.queryParam("pick_district", feeGhtkForm.getAddress().getPick_district());
        builder.queryParam("pick_ward", feeGhtkForm.getAddress().getPick_ward());
        builder.queryParam("pick_address", feeGhtkForm.getAddress().getPick_address());
        builder.queryParam("province", feeGhtkForm.getAddress().getProvince());
        builder.queryParam("district", feeGhtkForm.getAddress().getDistrict());
        builder.queryParam("ward", feeGhtkForm.getAddress().getWard());
        builder.queryParam("address", feeGhtkForm.getAddress().getAddress());
        builder.queryParam("weight", feeGhtkForm.getWeight());
        builder.queryParam("value", feeGhtkForm.getValue());
        String finalUri = builder.toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", Constants.TOKEN);
        HttpEntity httpEntity = new HttpEntity(headers);
        try {
            ResponseEntity<String> res =
                    restTemplate.exchange(finalUri, HttpMethod.GET, httpEntity, String.class);
            DocumentContext doc = JsonPath.parse(res.getBody());
            Number haha = doc.read("$.fee.fee");
            System.out.println(haha);
        }catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


}
