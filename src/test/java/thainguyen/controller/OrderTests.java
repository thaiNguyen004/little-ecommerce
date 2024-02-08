package thainguyen.controller;

import static org.assertj.core.api.Assertions.*;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.data.OrderRepository;
import thainguyen.domain.*;

import java.util.Arrays;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class OrderTests {
    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    OrderRepository repo;

    //    GET: find by id - success
    @Test
    void attemptFindOrderByIdSuccess() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/orders/352", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext doc = JsonPath.parse(response.getBody());
        String addressProvide = doc.read("$.data.address.province");
        String username = doc.read("$.data.user.username");
        Number userId = doc.read("$.data.user.id");
        Number length = doc.read("$.data.discounts.length()");
        String discountCode1 = doc.read("$.data.discounts[0].code");
        String discountCode2 = doc.read("$.data.discounts[1].code");

        assertThat(addressProvide).isEqualTo("Hà Nội");
        assertThat(username).isEqualTo("customer");
        assertThat(userId).isEqualTo(53);
        assertThat(discountCode1).isEqualTo("HAPPYNEWYEAR2024");
        assertThat(discountCode2).isEqualTo("FREESHIP2024");
        assertThat(length).isEqualTo(2);
    }


    //    GET: find by id - fail - not found
    @Test
    void attemptFindOrderNotFound() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "password")
                .getForEntity("/api/orders/9999", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    //    GET: find by id - fail - not own
    @Test
    void shouldReturn404WhenFindByIdButNotOwner() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer2", "password")
                .getForEntity("/api/orders/352", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    //    GET: find by id - fail - unauthorized
    @Test
    void attemptFindOrderByIdButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/orders/352", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    //    GET: find all - success
    @Test
    void attemptFindAllOrdersSuccess() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/orders", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext doc = JsonPath.parse(response.getBody());
        Number size = doc.read("$.data.length()");
        assertThat(size).isEqualTo(1);
    }


    //    GET: find all - fail - not found
    @Test
    void attemptFindAllOrdersButNotOwn() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer2", "password")
                .getForEntity("/api/orders", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    //    GET: find all - fail - unauthorized
    @Test
    void attemptFindAllButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/orders", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    //    POST: create a new order - success
    @Test
    @DirtiesContext
    void shouldCreateOrderSuccess() {
        Order order = new Order();
        Address address = new Address("0979284085"
                , "Hà Nội"
                , "Hoài Đức"
                , "Đức Giang"
                , "Ngh. 38/5 Lai Xá");
        order.setAddress(address);
        Discount d1 = new Discount();
        d1.setId(402L);
        Discount d2 = new Discount();
        d2.setId(403L);
        order.addDiscount(d1);
        order.addDiscount(d2);
        DetailProduct dp1 = new DetailProduct();
        dp1.setId(302L);
        DetailProduct dp2 = new DetailProduct();
        dp2.setId(303L);
        LineItem l1 = new LineItem();
        l1.setQuantity(12);
        l1.setDetailProduct(dp1);
        LineItem l2 = new LineItem();
        l2.setQuantity(2);
        l2.setDetailProduct(dp2);
        order.addLineItem(l1);
        order.addLineItem(l2);
        // 960.000 VND

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/orders", order, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        DocumentContext doc = JsonPath.parse(response.getBody());
        log.info(doc.read("$.message"));

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("employee", "password")
                .getForEntity(response.getHeaders().getLocation(), String.class);
        DocumentContext docGet = JsonPath.parse(getResponse.getBody());
        String username = docGet.read("$.data.user.username");
        Integer totalPriceAfterDiscount = docGet.read("$.data.totalPriceAfterDiscount");
        Integer totalPriceBeforeDiscount = docGet.read("$.data.totalPriceBeforeDiscount");
        String status = docGet.read("$.data.status");

        assertThat(username).isEqualTo("employee");
        assertThat(totalPriceAfterDiscount).isNotNull();
        assertThat(totalPriceBeforeDiscount).isNotNull();
        assertThat(status).isEqualTo("PENDING");
    }


    //    POST: create a new order - fail - ghtk throw error
    @Test
    void shouldReturn422WhenInsertAnOrderButErrorUnexpected() {
        Order order = new Order();
        Address address = new Address("0979284085"
                , "Hà Nội"
                , "Hoài Đức"
                , "Đức Giang"
                , "Ngh. 38/5 Lai Xá");
        order.setAddress(address);
        Discount d1 = new Discount();
        d1.setId(402L);
        Discount d2 = new Discount();
        d2.setId(403L);
        order.addDiscount(d1);
        order.addDiscount(d2);
        DetailProduct dp1 = new DetailProduct();
        dp1.setId(302L);
        DetailProduct dp2 = new DetailProduct();
        dp2.setId(303L);
        LineItem l1 = new LineItem();
        l1.setQuantity(12);
        l1.setDetailProduct(dp1);
        LineItem l2 = new LineItem();
        l2.setQuantity(2);
        l2.setDetailProduct(dp2);
        order.addLineItem(l1);
        order.addLineItem(l2);
        // 960.000 VND

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/orders", order, String.class);
        DocumentContext doc = JsonPath.parse(response.getBody());
        log.info(doc.read("$.message"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }


    //    POST: create a new order - fail - info not found
    @Test
    void attemptCreateOrderWithProductOrDiscountNotFound() {
        Order order = new Order();
        Address address = new Address("0979284085"
                , "Hà Nội"
                , "Hoài Đức"
                , "Đức Giang"
                , "Ngh. 38/5 Lai Xá");
        order.setAddress(address);
        Discount d1 = new Discount();
        d1.setId(999999L); // error discount
        Discount d2 = new Discount();
        d2.setId(403L);
        order.addDiscount(d1);
        order.addDiscount(d2);
        DetailProduct dp1 = new DetailProduct();
        dp1.setId(302L);
        DetailProduct dp2 = new DetailProduct();
        dp2.setId(303L);
        LineItem l1 = new LineItem();
        l1.setQuantity(12);
        l1.setDetailProduct(dp1);
        LineItem l2 = new LineItem();
        l2.setQuantity(2);
        l2.setDetailProduct(dp2);
        order.addLineItem(l1);
        order.addLineItem(l2);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/orders", order, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    //    POST: create a new order - fail - info must be not null but it's null
    @Test
    void attemptCreateOrderButLineItemNull() {
        Order order = new Order();
        Address address = new Address("0979284085"
                , "Hà Nội"
                , "Hoài Đức"
                , "Đức Giang"
                , "Ngh. 38/5 Lai Xá");
        order.setAddress(address);
        Discount d1 = new Discount();
        d1.setId(999999L); // error discount
        Discount d2 = new Discount();
        d2.setId(403L);
        order.addDiscount(d1);
        order.addDiscount(d2);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/orders", order, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    //    POST: create a new order - fail - unauthorized
    @Test
    void attemptCreateOrderButNotLogin() {
        Order order = new Order();
        Address address = new Address("0979284085"
                , "Hà Nội"
                , "Hoài Đức"
                , "Đức Giang"
                , "Ngh. 38/5 Lai Xá");
        order.setAddress(address);
        Discount d1 = new Discount();
        d1.setId(999999L); // error discount
        Discount d2 = new Discount();
        d2.setId(403L);
        order.addDiscount(d1);
        order.addDiscount(d2);
        DetailProduct dp1 = new DetailProduct();
        dp1.setId(302L);
        DetailProduct dp2 = new DetailProduct();
        dp1.setId(303L);
        LineItem l1 = new LineItem();
        l1.setQuantity(12);
        l1.setDetailProduct(dp1);
        LineItem l2 = new LineItem();
        l2.setQuantity(2);
        l2.setDetailProduct(dp2);
        order.addLineItem(l1);
        order.addLineItem(l2);

        ResponseEntity<Void> response = restTemplate
                .postForEntity("/api/orders", order, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    //    PUT: cancel order - fail - order has delivered successfully
    @Test
    void attemptCancelOrderHasAlreadySuccess() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/api/orders/cancel")
                .queryParam("order_id", 352);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .exchange(builder.toUriString(), HttpMethod.PUT, null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        log.info(response.getBody());
    }

    //    PUT: cancel order - fail - order has delivered successfully
    @Test
    void attemptCancelOrderNotExist() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/api/orders/cancel")
                .queryParam("order_id", 9999);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .exchange(builder.toUriString(), HttpMethod.PUT, null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        log.info(response.getBody());
    }


    //    PUT: cancel order - fail - unauthorized
    @Test
    void attemptCancelOrderButNotLogin() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/api/orders/cancel")
                .queryParam("order_id", 552);
        ResponseEntity<String> response = restTemplate
                .exchange(builder.toUriString(), HttpMethod.PUT, null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

}
