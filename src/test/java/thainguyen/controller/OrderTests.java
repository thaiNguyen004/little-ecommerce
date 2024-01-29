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
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.data.OrderRepository;
import thainguyen.domain.Address;
import thainguyen.dto.order.OrderDto;

import java.util.Arrays;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class OrderTests {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    OrderRepository repo;

    /*GET Order: Get Order by id success*/
    @Test
    void attemptFindOrderByIdSuccess () {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/orders/352", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext doc = JsonPath.parse(response.getBody());
        String addressProvide = doc.read("$.address.province");
        String username = doc.read("$.user.username");
        Number userId = doc.read("$.user.id");
        Number length = doc.read("$.discounts.length()");
        String discountCode1 = doc.read("$.discounts[0].code");
        String discountCode2 = doc.read("$.discounts[1].code");

        assertThat(addressProvide).isEqualTo("Hà Nội");
        assertThat(username).isEqualTo("customer");
        assertThat(userId).isEqualTo(53);
        assertThat(discountCode1).isEqualTo("HAPPYNEWYEAR2024");
        assertThat(discountCode2).isEqualTo("FREESHIP2024");
        assertThat(length).isEqualTo(2);
    }

    @Test
    void attemptFindOrderByIdSuccess2 () {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "password")
                .getForEntity("/api/orders/552", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext doc = JsonPath.parse(response.getBody());
        String addressProvide = doc.read("$.address.province");
        String username = doc.read("$.user.username");
        Number userId = doc.read("$.user.id");
        Number length = doc.read("$.discounts.length()");
        Number priceBeforeDiscount = doc.read("$.totalPriceBeforeDiscount.value");
        Number priceAfterDiscount = doc.read("$.totalPriceAfterDiscount.value");

        assertThat(addressProvide).isEqualTo("Phú Thọ");
        assertThat(username).isEqualTo("admin");
        assertThat(userId).isEqualTo(1);
        assertThat(length).isEqualTo(2);

        assertThat(priceBeforeDiscount).isEqualTo(676500.00);
        assertThat(priceAfterDiscount).isEqualTo(575025.00);
    }


    /*GET Order: Order with that id not found in database*/
    @Test
    void attemptFindOrderNotFound () {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "password")
                .getForEntity("/api/orders/9999", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    /*GET Order:Get order by id  not found duo not own*/
    @Test
    void shouldReturn404WhenFindByIdButNotOwner () {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer2", "password")
                .getForEntity("/api/orders/352", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /*GET Order: Get Order by Id, Order Unauthorized*/
    @Test
    void attemptFindOrderByIdButNotLogin () {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/orders/352", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /*GET Order:Get all orders not found duo not own*/
    @Test
    void attemptFindAllOrdersButNotOwn() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer2", "password")
                .getForEntity("/api/orders", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    /*GET Order: Get all Orders success*/
    @Test
    void attemptFindAllOrdersSuccess() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/orders", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext doc = JsonPath.parse(response.getBody());
        Number size = doc.read("$.length()");
        assertThat(size).isEqualTo(1);
    }


    /*GET Order: Get all Orders, Order Unauthorized*/
    @Test
    void attemptFindAllButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/orders", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    /*POST Order: Create Order success*/
    @Test
    void shouldReturn422WhenInsertAnOrderButErrorUnexpected() {
        OrderDto orderDto = new OrderDto();
        Address address = new Address("0979284085"
                , "Hà Nội"
                , "Hoài Đức"
                , "Đức Giang"
                , "Ngh. 38/5 Lai Xá");
        orderDto.setAddress(address);
        orderDto.setDiscounts(Arrays.asList(402L, 403L));

        OrderDto.LineItemDto lineItemDto1 = new OrderDto.LineItemDto();
        lineItemDto1.setQuantity(12);
        lineItemDto1.setDetailProductId(302L);

        OrderDto.LineItemDto lineItemDto2 = new OrderDto.LineItemDto();
        lineItemDto2.setQuantity(2);
        lineItemDto2.setDetailProductId(303L);
        // 960.000 VND
        orderDto.setLineItems(Arrays.asList(lineItemDto1, lineItemDto2));

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/orders", orderDto, String.class);
        DocumentContext doc = JsonPath.parse(response.getBody());
        log.info(doc.read("$.message"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }


    /*POST Order: Create Order unsuccess because ...*/
    @Test
    void attemptCreateOrderWithProductOrDiscountNotFound() {
        OrderDto orderDto = new OrderDto();
        Address address = new Address("0979284085"
                , "Phú Thọ"
                , "Yên Lập"
                , "Phúc Khánh"
                , "Khu 1");
        orderDto.setAddress(address);
        orderDto.setDiscounts(Arrays.asList(99999L, 403L)); // error discound

        OrderDto.LineItemDto lineItemDto1 = new OrderDto.LineItemDto();
        lineItemDto1.setQuantity(12);
        lineItemDto1.setDetailProductId(302L);

        OrderDto.LineItemDto lineItemDto2 = new OrderDto.LineItemDto();
        lineItemDto2.setQuantity(2);
        lineItemDto2.setDetailProductId(303L);

        orderDto.setLineItems(Arrays.asList(lineItemDto1, lineItemDto2));

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/orders", orderDto, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }


    /*POST Order: Bad request because info must be non null but it's null */
    @Test
    void attemptCreateOrderButLineItemNull() {
        OrderDto orderDto = new OrderDto();
        Address address = new Address("0979284085"
                , "Phú Thọ"
                , "Yên Lập"
                , "Phúc Khánh"
                , "Khu 1");
        orderDto.setAddress(address);
        orderDto.setDiscounts(Arrays.asList(402L, 403L)); // error discound

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/orders", orderDto, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    /*POST Order: Unauthorized */
    @Test
    void attemptCreateOrderButNotLogin() {
        OrderDto orderDto = new OrderDto();
        Address address = new Address("0979284085"
                , "Phú Thọ"
                , "Yên Lập"
                , "Phúc Khánh"
                , "Khu 1");
        orderDto.setAddress(address);
        orderDto.setDiscounts(Arrays.asList(402L, 403L));

        OrderDto.LineItemDto lineItemDto1 = new OrderDto.LineItemDto();
        lineItemDto1.setQuantity(12);
        lineItemDto1.setDetailProductId(302L);

        OrderDto.LineItemDto lineItemDto2 = new OrderDto.LineItemDto();
        lineItemDto2.setQuantity(2);
        lineItemDto2.setDetailProductId(303L);
        // 960.000 VND
        orderDto.setLineItems(Arrays.asList(lineItemDto1, lineItemDto2));

        ResponseEntity<Void> response = restTemplate
                .postForEntity("/api/orders", orderDto, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    /*PUT Order: Cancel Order success*/
    @Test
    void attemptCancelOrder() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/api/orders/cancel")
                .queryParam("order_id", 552);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "password")
                .exchange(builder.toUriString(), HttpMethod.PUT, null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    /*PUT Order: Cancel Order unauthorized*/
    @Test
    void attemptCancelOrderButNotLogin() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/api/orders/cancel")
                .queryParam("order_id", 552);
        ResponseEntity<String> response = restTemplate
                .exchange(builder.toUriString(), HttpMethod.PUT, null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /*PUT Order: Cancel Order return notfound because not own*/
    @Test
    void attemptCancelOrderButNotOwn() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/api/orders/cancel")
                .queryParam("order_id", 552);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .exchange(builder.toUriString(), HttpMethod.PUT, null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

}
