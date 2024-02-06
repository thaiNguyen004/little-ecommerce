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
import thainguyen.domain.DetailProduct;
import thainguyen.domain.Product;
import thainguyen.domain.Size;
import thainguyen.domain.valuetypes.Price;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class DetailProductTests {

    @Autowired
    TestRestTemplate restTemplate;

    /*GET DetailProduct: Get DetailProduct by id success*/
    @Test
    void shouldReturn200WhenIFindAProductExist() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/detailproducts/302", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    /*GET DetailProduct: DetailProduct with that id not found in database*/
    @Test
    void shouldReturn404WhenIFindAProductNotExist() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/detailproducts/1111111", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    /*GET DetailProduct: Get DetailProduct by Id, DetailProduct Unauthorized*/
    @Test
    void attemptGetDetailProductByIdButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/detailproducts/302", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    /*GET DetailProduct: Get all DetailProducts success*/
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


    /*GET DetailProduct: Get all DetailProducts, DetailProduct Unauthorized*/
    @Test
    void attemptReturnListButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/detailproducts", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /*POST DetailProduct: Create DetailProduct success*/
    @Test
    @DirtiesContext
    void shouldReturn201WhenCreatedAProductSuccess() {
        DetailProduct detailProduct = new DetailProduct();
        detailProduct.setName("demo name");
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


    /*POST DetailProduct: Create DetailProduct unsuccess because idSize or idProduct not found*/
    @Test
    @DirtiesContext
    void shouldReturn404WhenCreatedADetailProductThatProductOrSizeNotFound() {
        DetailProduct detailProduct = new DetailProduct();
        detailProduct.setName("detail product name");
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


    /*POST DetailProduct: Bad request because info must be non null but it's null */
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


    /*POST DetailProduct: Forbiden because cridential info is bad*/
    @Test
    @DirtiesContext
    void attemptPostDetailProductWithBadCridential() {
        DetailProduct detailProduct = new DetailProduct();
        detailProduct.setName("demo detailproduct name");
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


    /*POST DetailProduct: Unauthorized */
    @Test
    @DirtiesContext
    void attemptPostDetailProductButNotLogin() {
        DetailProduct detailProduct = new DetailProduct();
        detailProduct.setName("demo detailproduct name");
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

    /*PUT DetailProduct: Update DetailProduct success*/
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


    /*PUT DetailProduct: DetailProduct with that id not found in database*/
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


    /*PUT DetailProduct: Update Product of DetailProduct success*/
    @Test
    @DirtiesContext
    void updateProductOfDetailProduct() {
        DetailProduct detailProduct = new DetailProduct();
        Product product = new Product();
        product.setId(204L);
        detailProduct.setWeight(1.1);
        detailProduct.setProduct(product);

        HttpEntity<DetailProduct> request = new HttpEntity<>(detailProduct);

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
        String productName = doc.read("$.data.product.name");
        Integer priceValue = doc.read("$.data.price");
        assertThat(weight).isEqualTo(1.1);
        assertThat(sizeId).isNotNull();
        assertThat(productName).isEqualTo("Louis Vuitton shirt");
        assertThat(priceValue).isEqualTo(15555);
    }


    /*PUT DetailProduct: Update DetailProduct unsuccess due id size or id product not found*/
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


    /*PUT DetailProduct: Bad Cridential*/
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


    /*PUT DetailProduct: Unauthorized*/
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
