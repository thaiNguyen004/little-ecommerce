package thainguyen.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import thainguyen.domain.Brand;
import thainguyen.domain.Category;
import thainguyen.domain.Product;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class ProductTests {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;


//GET: find by id - success(OK)

    @Test
    void shouldReturn200WhenIFindAProductExist() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/products/205", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


//GET: find by id - fail(NOT_FOUND) - id not found in database

    @Test
    void shouldReturn404WhenIFindAProductNotExist() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/products/909090", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


//GET: find by id - fail(UNAUTHORIZED) - not login

    @Test
    void attemptGetProductByIdButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/products/205", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


//GET: find all - success(OK)

    @Test
    void shouldReturnListProductWhenFindAll() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "password")
                .getForEntity("/api/products", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext doc = JsonPath.parse(response.getBody());
        Number size = doc.read("$.length()");
        assertThat(size).isEqualTo(3);
    }


//GET: find all - fail(UNAUTHORIZED)

    @Test
    void attemptGetAllProductButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/products", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


//POST: create a new Product - success(CREATED)

    @Test
    void shouldReturn201WhenCreatedAProductSuccess() {
        Product product = new Product();
        product.setName("Demo product");
        product.setPicture("Demo link product");
        product.setDescription("Demo description product");
        Brand brand = new Brand();
        brand.setId(102L);
        Category category = new Category();
        category.setId(154L);
        product.setBrand(brand);
        product.setCategory(category);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/products", product, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewProduct = response.getHeaders().getLocation();
        log.error(locationOfNewProduct + "-------------------");
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity(locationOfNewProduct, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext doc = JsonPath.parse(getResponse.getBody());
        Number idBrand = doc.read("$.data.brand.id");
        Number idCategory = doc.read("$.data.category.id");
        assertThat(idBrand).isEqualTo(102);
        assertThat(idCategory).isEqualTo(154);
    }


//POST: create a new Product - fail(NOT_FOUND) - info not found in database

    @Test
    @DirtiesContext
    void shouldReturn404WhenCreateAnProductThatCategoryAndBrandNotFound() throws JsonProcessingException {
        Product product = new Product();
        product.setName("Demo product");
        Category c = new Category();
        c.setId(9999999L);
        Brand b = new Brand();
        b.setId(9999999L);
        product.setCategory(c);
        product.setBrand(b);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/products", product, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

//POST: create a new Product - fail(BAD_REQUEST) - info importance is null

    @Test
    @DirtiesContext
    void shouldReturn400WhenCreateAnProductThatCategoryAndBrandIsNull() {
        Product product = new Product();
        product.setName("Demo product");
        product.setPicture("Demo link product");
        product.setDescription("Demo description product");
        product.setBrand(null);
        product.setCategory(null);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/products", product, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


//POST: create a new Product - fail(FORBIDDEN) - credential info is bad

    @Test
    @DirtiesContext
    void shouldCreateFailWhenBadCridential() {
        Product product = new Product();
        product.setName("Demo product");
        product.setPicture("Demo link product");
        product.setDescription("Demo description product");
        Brand brand = new Brand();
        brand.setId(102L);
        Category category = new Category();
        category.setId(154L);
        product.setBrand(brand);
        product.setCategory(category);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("customer", "password")
                .postForEntity("/api/products", product, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }


//POST: create a new Size - fail(UNAUTHORIZED) - not login

    @Test
    @DirtiesContext
    void attemptPostProductButNotLogin() {
        Product product = new Product();
        product.setName("Demo product");
        product.setPicture("Demo link product");
        product.setDescription("Demo description product");
        Brand brand = new Brand();
        brand.setId(102L);
        Category category = new Category();
        category.setId(154L);
        product.setBrand(brand);
        product.setCategory(category);

        ResponseEntity<Void> response = restTemplate
                .postForEntity("/api/products", product, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

//PUT: update Product - success(OK)

    @Test
    @DirtiesContext
    void shouldReturnDataUpdatedWhenPuttedAProduct() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Demo name product");
        map.put("description", "Demo description product");
        map.put("brand", Map.of("id", "103"));
        map.put("category", Map.of("id", "154"));
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);
        ResponseEntity<String> response =
                restTemplate.withBasicAuth("employee", "password")
                        .exchange("/api/products/202", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("employee", "password")
                .getForEntity("/api/products/202", String.class);
        DocumentContext doc = JsonPath.parse(getResponse.getBody());
        String description = doc.read("$.data.description");
        String name = doc.read("$.data.name");
        String picture = doc.read("$.data.picture");
        Number brandId = doc.read("$.data.brand.id");

        assertThat(description).isEqualTo("Demo description product");
        assertThat(name).isEqualTo("Demo name product");
        assertThat(picture).isEqualTo("picture of product 1");
        assertThat(brandId).isEqualTo(103);
    }


    // PUT: update Product - fail(BAD_REQUEST) - info importance is null
    @Test
    @DirtiesContext
    void attemptPutProductButFieldsRequiredNotNull() {
        Product product = new Product();
        product.setName("Demo name product");
        product.setDescription("Demo description product");
        HttpEntity<Product> request = new HttpEntity<>(product);

        ResponseEntity<String> response =
                restTemplate.withBasicAuth("employee", "password")
                        .exchange("/api/products/202", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // PUT: update Product - fail(NOT_FOUND) - info not found in database
    @Test
    @DirtiesContext
    void attemptPutProductBut() throws JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Demo name product");
        map.put("picture", "Demo description product");
        map.put("description", "Demo description product");
        map.put("category", Map.of("id", "99999"));
        map.put("brand", Map.of("id", "99999"));
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);

        ResponseEntity<String> response =
                restTemplate.withBasicAuth("employee", "password")
                        .exchange("/api/products/202", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

//PUT: update Product - fail(NOT_FOUND) - id not found in database

    @Test
    @DirtiesContext
    void shouldReturn404WhenPuttedAProductNotExist() {
        Product product = new Product();
        product.setName("Demo name product");
        Brand brand = new Brand();
        brand.setId(103L);
        Category category = new Category();
        category.setId(154L);
        product.setBrand(brand);
        product.setCategory(category);
        product.setDescription("Demo description product");
        HttpEntity<Product> request = new HttpEntity<>(product);

        ResponseEntity<String> response =
                restTemplate.withBasicAuth("employee", "password")
                        .exchange("/api/products/9999", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


//PUT: update Product - fail(FORBIDDEN) - credential info is bad

    @Test
    @DirtiesContext
    void shouldReturnForbidenWhenPuttedAProductWithBadCridential() {
        Product product = new Product();
        product.setName("Demo name product");
        product.setDescription("Demo description product");
        HttpEntity<Product> request = new HttpEntity<>(product);

        ResponseEntity<String> response =
                restTemplate.withBasicAuth("customer", "password")
                        .exchange("/api/products/9999", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }


//PUT: update Product - fail(UNAUTHORIZED) - not login

    @Test
    @DirtiesContext
    void attemptPutProductButNotLogin() {
        Product product = new Product();
        product.setName("Demo name product");
        product.setDescription("Demo description product");
        HttpEntity<Product> request = new HttpEntity<>(product);

        ResponseEntity<String> response =
                restTemplate.withBasicAuth("customer", "password")
                        .exchange("/api/products/9999", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

}
