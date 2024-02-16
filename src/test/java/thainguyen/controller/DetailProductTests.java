package thainguyen.controller;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import thainguyen.detailproduct.DetailProduct;
import thainguyen.product.Product;
import thainguyen.size.Size;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class DetailProductTests {

    @Autowired
    TestRestTemplate restTemplate;

    //    GET: find by id - success(OK)
    @Test
    void shouldReturn200WhenIFindAProductExist() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/detailproducts/302", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    //    GET: find by id - fail(NOT_FOUND) - id not found in database
    @Test
    void shouldReturn404WhenIFindAProductNotExist() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/detailproducts/1111111", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    //    GET: find by id - fail(UNAUTHORIZED) - not login
    @Test
    void attemptGetDetailProductByIdButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/detailproducts/302", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    //    GET: find all - success(OK)
    @Test
    void shouldReturnList() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/detailproducts", String.class);
        DocumentContext doc = JsonPath.parse(response.getBody());
        Number length = doc.read("$.data.length()");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(length).isEqualTo(3);
    }


    //    GET: find all - fail(UNAUTHORIZED)
    @Test
    void attemptReturnListButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/detailproducts", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    //    POST: create a new DetailProduct - success(CREATED)
    @Test
    @DirtiesContext
    void shouldReturn201WhenCreatedAProductSuccess() {
        DetailProduct detailProduct = new DetailProduct();
        detailProduct.setWeight(999.99);
        Size size = new Size();
        size.setId(253L);
        Product product = new Product();
        product.setId(204L);
        detailProduct.setSize(size);
        detailProduct.setProduct(product);
        detailProduct.setPrice(119000);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/detailproducts", detailProduct, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewProduct = response.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity(locationOfNewProduct, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext doc = JsonPath.parse(getResponse.getBody());
        Number weight = doc.read("$.data.weight");
        Number sizeId = doc.read("$.data.size.id");
        Number productId = doc.read("$.data.product.id");
        Integer valuePrice = doc.read("$.data.price");
        assertThat(weight).isEqualTo(999.99);
        assertThat(sizeId).isEqualTo(253);
        assertThat(productId).isEqualTo(204);
        assertThat(valuePrice).isEqualTo(119000);
    }


//    POST: create a new DetailProduct - fail(NOT_FOUND) - id product or id size not found in database
    @Test
    @DirtiesContext
    void shouldReturn404WhenCreatedADetailProductThatProductOrSizeNotFound() {
        DetailProduct detailProduct = new DetailProduct();
        Size size = new Size();
        size.setId(9999L);
        detailProduct.setSize(size);
        Product product = new Product();
        product.setId(202L);
        detailProduct.setProduct(product);
        detailProduct.setPrice(119000);
        detailProduct.setWeight(999.99);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/detailproducts", detailProduct, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    //    POST: create a new DetailProduct - fail(BAD_REQUEST) - info importance is null
    @Test
    @DirtiesContext
    void attemptPostDetailProductButSizeOrProductIsNull() {
        DetailProduct detailProduct = new DetailProduct();
        detailProduct.setPrice(119000);
        detailProduct.setWeight(999.99);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/detailproducts", detailProduct, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    //    POST: create a new DetailProduct - fail(FORBIDDEN) - credential info is bad
    @Test
    @DirtiesContext
    void attemptPostDetailProductWithBadCridential() {
        DetailProduct detailProduct = new DetailProduct();
        Size size = new Size();
        size.setId(252L);
        detailProduct.setSize(size);
        Product product = new Product();
        product.setId(202L);
        detailProduct.setProduct(product);
        detailProduct.setPrice(119000);
        detailProduct.setWeight(999.99);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("customer", "password")
                .postForEntity("/api/detailproducts", detailProduct, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    //    POST: create a new DetailProduct - fail(UNAUTHORIZED) - not login
    @Test
    @DirtiesContext
    void attemptPostDetailProductButNotLogin() {
        DetailProduct detailProduct = new DetailProduct();
        Size size = new Size();
        size.setId(252L);
        detailProduct.setSize(size);
        Product product = new Product();
        product.setId(202L);
        detailProduct.setProduct(product);
        detailProduct.setPrice(119000);
        detailProduct.setWeight(999.99);

        ResponseEntity<Void> response = restTemplate
                .postForEntity("/api/detailproducts", detailProduct, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


//    PUT: update DetailProduct - success(OK)
    @Test
    @DirtiesContext
    void shouldReturnDataUpdatedWhenPatchedAProduct() {
        Map<String, Object> map = new HashMap<>();
        map.put("weight", "1.1");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);

        ResponseEntity<String> response =
                restTemplate.withBasicAuth("employee", "password")
                        .exchange("/api/detailproducts/304", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("employee", "password")
                .getForEntity("/api/detailproducts/304", String.class);
        DocumentContext doc = JsonPath.parse(getResponse.getBody());
        Number weight = doc.read("$.data.weight");
        Number sizeId = doc.read("$.data.size.id");
        Number productId = doc.read("$.data.product.id");
        Integer priceValue = doc.read("$.data.price");
        assertThat(weight).isEqualTo(1.1);
        assertThat(sizeId).isNotNull();
        assertThat(productId).isNotNull();
        assertThat(priceValue).isNotNull();
    }


    //    PUT: update DetailProduct - fail(NOT_FOUND) - id not found in database
    @Test
    @DirtiesContext
    void attemptPatchDetailProductNotFound() {
        Map<String, Object> map = new HashMap<>();
        map.put("product", Map.of("id", "204"));
        map.put("weight", "1.1");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);
        ResponseEntity<String> response =
                restTemplate.withBasicAuth("employee", "password")
                        .exchange("/api/detailproducts/99999", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    /*PUT: update DetailProduct - fail(NOT_FOUND) - id product or id size not found in database*/
    @Test
    @DirtiesContext
    void attemptUpdateSizeofDetailProductNotFound() {
        Map<String, Object> map = new HashMap<>();
        map.put("size", Map.of("id", "99999"));
        map.put("weight", "1.1");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);

        ResponseEntity<String> response =
                restTemplate.withBasicAuth("employee", "password")
                        .exchange("/api/detailproducts/304", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }



    //    PUT: update DetailProduct - fail(FORBIDDEN) - credential info is bad
    @Test
    @DirtiesContext
    void attemptUpdateDetailProductWithBadCridential() {
        Map<String, Object> map = new HashMap<>();
        map.put("product", Map.of("id", "204"));
        map.put("weight", "1.1");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);

        ResponseEntity<String> response =
                restTemplate
                        .withBasicAuth("customer", "password")
                        .exchange("/api/detailproducts/304", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }



    //    PUT: update DetailProduct - fail(UNAUTHORIZED) - not login
    @Test
    @DirtiesContext
    void attemptUpdateDetailProductButNotLogin() {
        Map<String, Object> map = new HashMap<>();
        map.put("product", Map.of("id", "204"));
        map.put("weight", "1.1");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);

        ResponseEntity<String> response =
                restTemplate
                        .exchange("/api/detailproducts/304", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

}
