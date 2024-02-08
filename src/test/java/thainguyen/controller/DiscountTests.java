package thainguyen.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import thainguyen.domain.Discount;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DiscountTests {

    @Autowired
    TestRestTemplate restTemplate;

    /*GET: find by id - success*/
    @Test
    void attempFindDiscountByIdSuccess() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/discounts/402", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    /*GET: find all - fail - id not found*/
    @Test
    void attempFindDiscountNotFound() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/discounts/9999", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    /*GET: find by id - fail - unauthorized*/
    @Test
    void attempFindDiscountButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/discounts/402", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    /*GET: find all - success*/
    @Test
    void attemptGetAllDiscountsSuccess() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .getForEntity("/api/discounts", String.class);
        Number sizeOfDiscounts = JsonPath.parse(response.getBody()).read("$.data.length()");

        assertThat(sizeOfDiscounts).isEqualTo(2);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    /*GET: find all - fail - unauthorized*/
    @Test
    void attemptGetAllDiscountsButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/discounts", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    /*POST: create a new discount - success */
    @Test
    @DirtiesContext
    void attemptCreateAnDiscountSuccess() {
        Discount discount = new Discount("DEMOTESTCREATEDISCOUNT",
                Discount.Type.AMOUNT,
                Discount.Kind.FREESHIP,
                200000, 1000
                , LocalDateTime.of(2024, 1, 14, 00, 00, 00)
                , LocalDateTime.of(2024, 2, 14, 00, 00, 00));

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/discounts", discount, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        URI location = response.getHeaders().getLocation();

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity(location, String.class);
        DocumentContext doc = JsonPath.parse(getResponse.getBody());
        String code = doc.read("$.data.code");
        String type = doc.read("$.data.type");
        Number value = doc.read("$.data.value");

        assertThat(code).isEqualTo("DEMOTESTCREATEDISCOUNT");
        assertThat(type).isEqualTo("AMOUNT");
        assertThat(value).isEqualTo(200000);
    }


    /*POST: create a new discount - fail - forbidden*/
    @Test
    @DirtiesContext
    void attemptCreateAnDiscountWithBadCridential() {
        Discount discount = new Discount("DEMOTESTCREATEDISCOUNT",
                Discount.Type.AMOUNT,
                Discount.Kind.FREESHIP,
                200000, 1000
                , LocalDateTime.of(2024, 1, 14, 00, 00, 00)
                , LocalDateTime.of(2024, 2, 14, 00, 00, 00));

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("customer", "password")
                .postForEntity("/api/discounts", discount, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }


    /*POST: create a new discount - fail - info required is null */
    @Test
    @DirtiesContext
    void attemptCreateAnDiscountButInfoIsNull() {
        Discount discount = new Discount("DEMOTESTCREATEDISCOUNT",
                null, // info is null
                Discount.Kind.FREESHIP,
                200000, 1000
                , LocalDateTime.of(2024, 1, 14, 00, 00, 00)
                , LocalDateTime.of(2024, 2, 14, 00, 00, 00));

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/discounts", discount, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /*POST: create a new discount - fail - percent > 100*/
    @Test
    @DirtiesContext
    void attemptCreateAnDiscountWhilePercentGreateThan100() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", "DEMOTESTCREATEDISCOUNT");
        map.put("type", "PERCENTAGE");
        map.put("kind", "FREESHIP");
        map.put("value", 101);
        map.put("quantity", 1000);
        map.put("start", LocalDateTime.of(2024, 1, 14, 00, 00, 00));
        map.put("end", LocalDateTime.of(2024, 2, 14, 00, 00, 00));
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/discounts", request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }



    /*POST: create a new discount - fail - unauthorized*/
    @Test
    @DirtiesContext
    void attemptCreateAnDiscountButNotLogin() {
        Discount discount = new Discount("DEMOTESTCREATEDISCOUNT",
                Discount.Type.AMOUNT,
                Discount.Kind.FREESHIP,
                200000, 1000
                , LocalDateTime.of(2024, 1, 14, 00, 00, 00)
                , LocalDateTime.of(2024, 2, 14, 00, 00, 00));

        ResponseEntity<Void> response = restTemplate
                .postForEntity("/api/discounts", discount, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    /*PUT: update a discount - success*/
    @Test
    @DirtiesContext
    void attemptPutDiscountSuccess() {
        Discount discount = new Discount("EHEHE",
                Discount.Type.PERCENTAGE,
                Discount.Kind.FREESHIP,
                12, 111
                , LocalDateTime.of(2024, 1, 14, 00, 00, 00)
                , LocalDateTime.of(2024, 2, 14, 00, 00, 00));
        HttpEntity<Discount> request = new HttpEntity<>(discount);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/discounts/402", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/discounts/402", String.class);
        DocumentContext doc = JsonPath.parse(getResponse.getBody());
        String code = doc.read("$.data.code");
        String type = doc.read("$.data.type");
        Number value = doc.read("$.data.value");

        assertThat(code).isEqualTo("EHEHE");
        assertThat(type).isEqualTo("PERCENTAGE");
        assertThat(value).isEqualTo(12);
    }


    /*PUT: update a discount - fail - percent > 100*/
    @Test
    void attemptPutDiscountWhilePercentBetterThan100() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", "EHEHE");
        map.put("type", "PERCENTAGE");
        map.put("kind", "FREESHIP");
        map.put("value", 101);
        map.put("quantity", 111);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/discounts/402", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST); // beta

    }

    /*PUT: update a discount - fail - percent < 1*/
    @Test
    void attemptPutDiscountWhilePercentLessThan1() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", "EHEHE");
        map.put("type", "PERCENTAGE");
        map.put("kind", "FREESHIP");
        map.put("value", 0);
        map.put("quantity", 111);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/discounts/402", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST); // beta

    }

    /*PUT: update a discount - fail - amount < 1*/
    @Test
    void attemptPutDiscountWhileAmountLessThan1() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", "EHEHE");
        map.put("type", "AMOUNT");
        map.put("kind", "FREESHIP");
        map.put("value", 0);
        map.put("quantity", 111);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/discounts/402", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST); // beta
    }


    /*PUT: update a discount - fail - ain't existence*/
    @Test
    void attemptPutDiscountNotFound() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", "DISCOUNT DEMO");
        map.put("type", "PERCENTAGE");
        map.put("kind", "FREESHIP");
        map.put("value", 1);
        map.put("quantity", 111);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/discounts/0101001101", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    /*PUT: update a discount - fail - forbidden*/
    @Test
    void attemptPutDiscountWithBadCridential() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", "DISCOUNT DEMO");
        map.put("type", "PERCENTAGE");
        map.put("kind", "FREESHIP");
        map.put("value", 1);
        map.put("quantity", 111);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .exchange("/api/discounts/402", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }


    /*PUT: update a discount - fail - unauthorized*/
    @Test
    void attemptPutDiscountButNotLogin() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", "DISCOUNT DEMO");
        map.put("type", "PERCENTAGE");
        map.put("kind", "FREESHIP");
        map.put("value", 1);
        map.put("quantity", 111);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);
        ResponseEntity<String> response = restTemplate
                .exchange("/api/discounts/402", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    @Test
    void attemptUpdateDiscountSuccess() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", "HAHAHA");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/discounts/402", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/discounts/402", String.class);
        DocumentContext doc = JsonPath.parse(getResponse.getBody());
        String code = doc.read("$.data.code");
        String type = doc.read("$.data.type");
        Number value = doc.read("$.data.value");

        assertThat(code).isEqualTo("HAHAHA");
        assertThat(type).isNotNull();
        assertThat(value).isNotNull();
    }

}
