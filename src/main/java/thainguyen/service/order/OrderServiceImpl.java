package thainguyen.service.order;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.controller.exception.GhtkCreateOrderFailedException;
import thainguyen.data.OrderRepository;
import thainguyen.domain.*;
import thainguyen.domain.valuetypes.Status;
import thainguyen.dto.ghtk.GhtkForm;
import thainguyen.service.detailproduct.DetailProductService;
import thainguyen.service.discount.DiscountService;
import thainguyen.service.generic.GenericServiceImpl;
import thainguyen.service.user.UserService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends GenericServiceImpl<Order>
        implements OrderService {

    private final OrderRepository orderRepo;
    private final UserService userService;
    private final DetailProductService dpService;
    private final DiscountService discountService;
    private final RestTemplate restTemplate = new RestTemplate();

    public OrderServiceImpl(EntityManager em
            , OrderRepository orderRepo
            , UserService userService
            , DetailProductService dpService
            , DiscountService discountService) {

        super(em, Order.class);
        this.orderRepo = orderRepo;
        this.userService = userService;
        this.dpService = dpService;
        this.discountService = discountService;
    }

    @Override
    public List<Order> findByOwner(String username) {
        List<Order> orders = orderRepo.findByUsername(username);
        if(orders.isEmpty()) {
            throw new NoResultException("Order placed by " + username + " not found");
        }
        return orders;
    }

    @Override
    public Order findByIdAndOwner(UUID id, String username) {
        Optional<Order> orderOpt = orderRepo.findByIdAndUsername(id, username);
        return orderOpt.orElseThrow(() ->
                new NoResultException("No found order with id = "
                        + id + " and username = " + username));
    }

    @Override
    public Order create(Order order, String username) {
        User user = userService.findByUsername(username);
        order.setUser(user);
        // add line items
        order.setLineItems(order.getLineItems()
                .stream().map(lineItem -> {
                    LineItem lt = new LineItem();
                    lt.setQuantity(lineItem.getQuantity());
                    DetailProduct detailProduct = dpService.findById(lineItem.getDetailProduct().getId());
                    lt.setDetailProduct(detailProduct);
                    lt.setAmount(detailProduct.getPrice());

                    // calc total price and total weight
                    lt.setTotalPrice(lt.getQuantity() * lt.getAmount());
                    lt.setTotalWeight(lt.getQuantity() * detailProduct.getWeight());

                    lt.setOrder(order);
                    return lt;
                }).collect(Collectors.toList()));

        // add discount if exist
        if (order.getDiscounts() != null && !order.getDiscounts().isEmpty()) {
            order.setDiscounts(order.getDiscounts().stream().map(discount ->
                    discountService.findById(discount.getId())).collect(Collectors.toList()));
        }
        return orderRepo.save(order);
    }


    @Override
    public Order create(Order order) {
        return orderRepo.save(order);
    }

    @Override
    public void cancel(UUID orderId) throws GhtkCreateOrderFailedException {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(Constants.URI_GHTK_ORDER_CANCEL + "/partner_id:" + orderId.toString());
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Token", Constants.TOKEN);
            HttpEntity<GhtkForm> request = new HttpEntity<>(null, headers);

            restTemplate.postForEntity(builder.toUriString(), request, String.class);
        } catch (HttpClientErrorException ex) {
            DocumentContext documentContext = JsonPath.parse(ex.getResponseBodyAsString());
            String message = documentContext.read("$.message");
            throw new GhtkCreateOrderFailedException(message);
        }
    }

    @Override
    public Status getStatus(UUID id, String username) {
        return orderRepo.getStatus(id, username);
    }

    @Override
    public Order updateStatus(UUID orderId, Status status) {
        return orderRepo.findById(orderId).map(order -> {
            order.setStatus(status);
            return orderRepo.save(order);
        }).orElseThrow(() -> new NoResultException("Invalid Order ID, Order not found"));
    }
}
