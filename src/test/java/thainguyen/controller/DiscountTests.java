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
import thainguyen.domain.Discount;

import java.net.URI;
import java.time.LocalDateTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DiscountTests {

    @Autowired
    TestRestTemplate restTemplate;

    /*GET: Get Discount by id success*/
    @Test
    void attempFindDiscountByIdSuccess() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/discounts/402", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    /*GET: Discount with that id not found in database*/
    @Test
    void attempFindDiscountNotFound() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/discounts/9999", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    /*GET:Get Discount by Id,  Discount Unauthorized*/
    @Test
    void attempFindDiscountButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/discounts/402", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    /*GET: Get all Discounts success*/
    @Test
    void attemptGetAllDiscountsSuccess() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .getForEntity("/api/discounts", String.class);
        Number sizeOfDiscounts = JsonPath.parse(response.getBody()).read("$.length()");

        assertThat(sizeOfDiscounts).isEqualTo(2);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    /*GET: Get all Discounts, Discount Unauthorized*/
    @Test
    void attemptGetAllDiscountsButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/discounts", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    /*POST Discount: Create Discount success */
    @Test
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
        String code = doc.read("$.code");
        String type = doc.read("$.type");
        Number value = doc.read("$.value");

        assertThat(code).isEqualTo("DEMOTESTCREATEDISCOUNT");
        assertThat(type).isEqualTo("AMOUNT");
        assertThat(value).isEqualTo(200000);
    }


    /*POST Discount: Forbiden because cridential info is bad*/
    @Test
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


    /*POST Discount: Bad request because info is null */
    @Test
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

    /*POST Discount: Create Discount fail due percent > 100*/
    @Test
    void attemptCreateAnDiscountWhilePercentGreateThan100() {
        Discount discount = new Discount("DEMOTESTCREATEDISCOUNT",
                Discount.Type.PERCENTAGE, // info is null
                Discount.Kind.FREESHIP,
                101, 1000
                , LocalDateTime.of(2024, 1, 14, 00, 00, 00)
                , LocalDateTime.of(2024, 2, 14, 00, 00, 00));

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/discounts", discount, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }



    /*POST Discount: Unauthorized */
    @Test
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


    /*PUT Discount: Update Discount success*/
    @Test
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
        String code = doc.read("$.code");
        String type = doc.read("$.type");
        Number value = doc.read("$.value");

        assertThat(code).isEqualTo("EHEHE");
        assertThat(type).isEqualTo("PERCENTAGE");
        assertThat(value).isEqualTo(12);
    }


    /*PUT Discount: Update Discount fail due percent > 100*/
    @Test
    void attemptPutDiscountWhilePercentBetterThan100() {
        Discount discount = new Discount("EHEHE",
                Discount.Type.PERCENTAGE,
                Discount.Kind.FREESHIP,
                101, 111
                , LocalDateTime.of(2024, 1, 14, 00, 00, 00)
                , LocalDateTime.of(2024, 2, 14, 00, 00, 00));
        HttpEntity<Discount> request = new HttpEntity<>(discount);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/discounts/402", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY); // beta

    }

    /*PUT Discount: Update Discount fail due percent < 1*/
    @Test
    void attemptPutDiscountWhilePercentLessThan1() {
        Discount discount = new Discount("EHEHE",
                Discount.Type.PERCENTAGE,
                Discount.Kind.FREESHIP,
                0, 111
                , LocalDateTime.of(2024, 1, 14, 00, 00, 00)
                , LocalDateTime.of(2024, 2, 14, 00, 00, 00));
        HttpEntity<Discount> request = new HttpEntity<>(discount);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/discounts/402", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY); // beta

    }

    /*PUT Discount: Update Discount fail due amount < 1*/
    @Test
    void attemptPutDiscountWhileAmountLessThan1() {
        Discount discount = new Discount("EHEHE",
                Discount.Type.PERCENTAGE,
                Discount.Kind.FREESHIP,
                0, 111
                , LocalDateTime.of(2024, 1, 14, 00, 00, 00)
                , LocalDateTime.of(2024, 2, 14, 00, 00, 00));
        HttpEntity<Discount> request = new HttpEntity<>(discount);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/discounts/402", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY); // beta
    }


    /*PUT Discount: Update Discount fail missing info*/
    @Test
    void attemptPutDiscountFailDueMissingInfo() {
        Discount discount = new Discount(null, // this info is null
                Discount.Type.PERCENTAGE,
                Discount.Kind.FREESHIP,
                0, 111
                , LocalDateTime.of(2024, 1, 14, 00, 00, 00)
                , LocalDateTime.of(2024, 2, 14, 00, 00, 00));
        HttpEntity<Discount> request = new HttpEntity<>(discount);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/discounts/402", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST); // beta
    }


    /*PUT: Discount with that id not found in database*/
    @Test
    void attemptPutDiscountNotFound() {
        Discount discount = new Discount("DISCOUNT DEMO", // this info is null
                Discount.Type.PERCENTAGE,
                Discount.Kind.FREESHIP,
                1, 111
                , LocalDateTime.of(2024, 1, 14, 00, 00, 00)
                , LocalDateTime.of(2024, 2, 14, 00, 00, 00));
        HttpEntity<Discount> request = new HttpEntity<>(discount);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/discounts/0101001101", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    /*PUT Discount: Bad Cridential*/
    @Test
    void attemptPutDiscountWithBadCridential() {
        Discount discount = new Discount("DISCOUNT DEMO", // this info is null
                Discount.Type.PERCENTAGE,
                Discount.Kind.FREESHIP,
                1, 111
                , LocalDateTime.of(2024, 1, 14, 00, 00, 00)
                , LocalDateTime.of(2024, 2, 14, 00, 00, 00));
        HttpEntity<Discount> request = new HttpEntity<>(discount);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .exchange("/api/discounts/402", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }


    /*PUT Discount: Unauthorized*/
    @Test
    void attemptPutDiscountButNotLogin() {
        Discount discount = new Discount("DISCOUNT DEMO", // this info is null
                Discount.Type.PERCENTAGE,
                Discount.Kind.FREESHIP,
                1, 111
                , LocalDateTime.of(2024, 1, 14, 00, 00, 00)
                , LocalDateTime.of(2024, 2, 14, 00, 00, 00));
        HttpEntity<Discount> request = new HttpEntity<>(discount);
        ResponseEntity<String> response = restTemplate
                .exchange("/api/discounts/402", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    /*Patch Discount: Update Discount success*/
    @Test
    void attemptPatchDiscountSuccess() {
        Discount discount = new Discount();
        discount.setCode("HAHAHHA");
        HttpEntity<Discount> request = new HttpEntity<>(discount);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/discounts/402", HttpMethod.PATCH, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/discounts/402", String.class);
        DocumentContext doc = JsonPath.parse(getResponse.getBody());
        String code = doc.read("$.code");
        String type = doc.read("$.type");
        Number value = doc.read("$.value");

        assertThat(code).isEqualTo("HAHAHHA");
        assertThat(type).isNotNull();
        assertThat(value).isNotNull();
    }

    /*Patch Discount: Update Discount fail due percent > 100*/
    @Test
    void attemptPatchDiscountFailDuePercentGreaterThan100() {
        Discount discount = new Discount();
        discount.setType(Discount.Type.PERCENTAGE);
        discount.setValue(101);
        discount.setCode("HAHAHHA");
        HttpEntity<Discount> request = new HttpEntity<>(discount);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/discounts/402", HttpMethod.PATCH, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }



    /*PATCH Discount: Discount with that id not found in database*/
    @Test
    void attemptPatchDiscountNotFound() {
        Discount discount = new Discount();
        discount.setCode("HAHAHHA");
        HttpEntity<Discount> request = new HttpEntity<>(discount);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/discounts/9999", HttpMethod.PATCH, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    /*PATCH Discount: Bad Cridential*/
    @Test
    void attemptPatchDiscountWithBadCridential() {
        Discount discount = new Discount();
        discount.setCode("HAHAHHA");
        HttpEntity<Discount> request = new HttpEntity<>(discount);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .exchange("/api/discounts/402", HttpMethod.PATCH, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }


    /*PATCH Discount: Unauthorized*/
    @Test
    void attemptPatchDiscountButNotLogin() {
        Discount discount = new Discount();
        discount.setCode("HAHAHHA");
        HttpEntity<Discount> request = new HttpEntity<>(discount);
        ResponseEntity<String> response = restTemplate
                .exchange("/api/discounts/402", HttpMethod.PATCH, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

}
