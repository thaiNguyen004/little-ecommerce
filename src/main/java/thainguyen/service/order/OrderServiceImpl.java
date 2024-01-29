package thainguyen.service.order;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.data.OrderRepository;
import thainguyen.data.UserRepository;
import thainguyen.domain.Constants;
import thainguyen.domain.Order;
import thainguyen.domain.valuetypes.Status;
import thainguyen.dto.ghtk.GhtkForm;
import thainguyen.service.generic.GenericServiceImpl;

import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl extends GenericServiceImpl<Order>
        implements OrderService {

    private final OrderRepository orderRepo;
    private final UserRepository userRepo;
    private final RestTemplate restTemplate = new RestTemplate();

    public OrderServiceImpl(EntityManager em
            , OrderRepository orderRepo
            , UserRepository userRepo) {

        super(em, Order.class);
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
    }

    @Override
    public List<Order> findByOwner(String username) {
        return userRepo.findByUsername(username).map(user ->
                orderRepo.findByUser(user.getUsername())).orElseGet(() -> null);
    }

    @Override
    public Optional<Order> findByIdAndOwner(Long id, String username) {
        return userRepo.findByUsername(username).map(user -> {
            return orderRepo.findByIdAndUser(id, user.getUsername());
        }).orElseGet(() -> null);
    }

    @Override
    public Order create(Order order, String username) {
        return userRepo.findByUsername(username).map(user -> {
            order.setUser(user);
            return orderRepo.save(order);
        }).orElseGet(() -> null);
    }

    @Override
    public Order create(Order order) {
        return orderRepo.save(order);
    }

    @Override
    public boolean cancel(Long orderId) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(Constants.URI_GHTK_ORDER_CANCEL + "/partner_id:" + orderId +"q");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Token", Constants.TOKEN);
        HttpEntity<GhtkForm> request = new HttpEntity<>(null, headers);

        ResponseEntity<String> requestCancelling = restTemplate
                .postForEntity(builder.toUriString(), request, String.class);
        System.out.println(requestCancelling.getBody());
        DocumentContext documentContext = JsonPath.parse(requestCancelling.getBody());
        Boolean isSuccess = documentContext.read("$.success");
        if (isSuccess) {
            updateStatus(orderId, Status.CANCEL);
        }
        return isSuccess;
    }

    @Override
    public Status getStatus(Long id, String username) {
        return orderRepo.getStatus(id, username);
    }

    @Override
    public Order updateStatus(Long orderId, Status status) {
        return orderRepo.findById(orderId).map(order -> {
            order.setStatus(status);
            return orderRepo.save(order);
        }).orElseGet(() -> null);
    }
}
