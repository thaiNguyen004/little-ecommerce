package thainguyen.controller;

import static org.assertj.core.api.Assertions.*;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import thainguyen.data.OrderRepository;
import thainguyen.domain.Address;
import thainguyen.domain.Order;
import thainguyen.dto.order.OrderDto;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

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


    /*GET Order: Order with that id not found in database*/


    /*GET Order:Get order by id  not found duo not own*/
    @Test
    void shouldReturn404WhenFindByIdButNotOwner () {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer2", "password")
                .getForEntity("/api/orders/352", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /*GET Order: Get Order by Id, Order Unauthorized*/


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
    /*
    * {
    *   "lineItems": [
    *       "detailProductId": 1,
    *       "quantity", 1
    *   ],
    *   "discounts": [2, 3, 4],
    *   "address": {
    *       "phoneNumber": "0336514962",
    *       "province": "Ha noi",
    *       "district": "hoai duc",
    *       "ward": "bac lai xa",
    *       "detailAddress": "so 49",
    *       "hamlet": "Khac"
    *   }
    * }
    * */
    /*POST Order: Create Order success*/
    @Test
    void shouldReturn401WhenInsertAnOrder() {
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
                .withBasicAuth("employee", "password")
                .postForEntity("/api/orders", orderDto, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfOrder = response.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("employee", "password")
                        .getForEntity(locationOfOrder, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext doc = JsonPath.parse(getResponse.getBody());

        // check buyer
        String customerName = doc.read("$.user.fullname");
        assertThat(customerName).isEqualTo("Nguyen employee");

        // check discount
        Number discountSize = doc.read("$.discounts.length()");
        assertThat(discountSize).isEqualTo(2);

        // check info order
        Number totalPrice = doc.read("totalPrice.value");
        assertThat(totalPrice).isEqualTo(960000.000);

        // check shipment
        String labelCode = doc.read("$.shipment.labelCode");
        log.info("labelCode: ", labelCode);
        String order = doc.read("$.shipment.order");
        assertThat(order).isNotNull();

        // check lineitem
        Number sizeLineItem = doc.read("$.lineItems.length()");
        assertThat(sizeLineItem).isEqualTo(2);
    }

    @Test
    void test1() {
        Optional<Order> order = repo.findById(352L);
        System.out.println(order.get().getTotalPriceBeforeDiscount());
    }

    /*POST Order: Create Order unsuccess because ...*/



    /*POST Order: Bad request because info must be non null but it's null */



    /*POST Order: Forbiden because cridential info is bad*/



    /*POST Order: Unauthorized */



    /*PUT Order: Update Order success*/



    /*PUT Order: Update Order unsuccess due ... not found*/



    /*PUT Order: Bad request because info must be non null but it's null*/



    /*PUT Order: Order with that id not found in database*/



    /*PUT Order: Bad Cridential*/



    /*PUT Order: Unauthorized*/



    /*Patch Order: Update Order success*/



    /*PATCH Order: Order with that id not found in database*/



    /*PATCH Order: Update Order unsuccess due ... not found*/



    /*PATCH Order: Bad Cridential*/



    /*PATCH Order: Unauthorized*/



}
