package thainguyen.controller;

import static org.assertj.core.api.Assertions.assertThat;

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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class ProductTests {

    @Autowired
    TestRestTemplate restTemplate;


    /*GET Product: Get Product by id success*/
    @Test
    void shouldReturn200WhenIFindAProductExist() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/products/205", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    /*GET Product: Product with that id not found in database*/
    @Test
    void shouldReturn404WhenIFindAProductNotExist() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/products/909090", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    /*GET Product:Get Product by Id,  Product Unauthorized*/
    @Test
    void attemptGetProductByIdButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/products/205", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    /*GET Product: Get all Products success*/
    @Test
    void shouldReturnListProductWhenFindAll() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "password")
                .getForEntity("/api/products", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext doc = JsonPath.parse(response.getBody());
        Number size = doc.read("$.length()");
        assertThat(size).isEqualTo(4);
    }


    /*GET Product: Get all Products, Product Unauthorized*/
    @Test
    void attemptGetAllProductButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/products", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    /*POST Product: Create Product success*/
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
        Number idBrand = doc.read("$.brand.id");
        Number idCategory = doc.read("$.category.id");
        assertThat(idBrand).isEqualTo(102);
        assertThat(idCategory).isEqualTo(154);
    }


    /*POST Product: Create Product unsuccess because idBrand or idCategory not found*/
    @Test
    @DirtiesContext
    void shouldReturn404WhenCreateAnProductThatCategoryAndBrandNotFound() {
        Product product = new Product();
        product.setName("Demo product");
        product.setPicture("Demo link product");
        product.setDescription("Demo description product");
        Brand brand = new Brand();
        brand.setId(9999L);
        Category category = new Category();
        category.setId(9999L);
        product.setBrand(brand);
        product.setCategory(category);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/products", product, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /*POST Product: Create Product unsuccess because idBrand or idCategory is null */
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


    /*POST Product: Forbiden because cridential info is bad*/
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


    /*POST Product: Bad request because info must be non null but it's null */
    @Test
    @DirtiesContext
    void attemptPostProductButFieldNotNullIsNull() {
        Product product = new Product();
//        product.setName("Demo product");
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
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }




    /*POST Product: Unauthorized */
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


    /*PUT Product: Update Product success*/
    @Test
    @DirtiesContext
    void shouldReturnDataUpdatedWhenPuttedAProduct() {
        Product product = new Product();
        product.setName("Demo name product");
        Brand brand = new Brand();
        brand.setId(103L);
        Category category = new Category();
        category.setId(154L);
        product.setBrand(brand);
        product.setCategory(category);
        product.setBrand(brand);
        product.setCategory(category);
        product.setDescription("Demo description product");
        HttpEntity<Product> request = new HttpEntity<>(product);

        ResponseEntity<String> response =
                restTemplate.withBasicAuth("employee", "password")
                        .exchange("/api/products/202", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("employee", "password")
                .getForEntity("/api/products/202", String.class);
        DocumentContext doc = JsonPath.parse(getResponse.getBody());
        String description = doc.read("$.description");
        String name = doc.read("$.name");
        String picture = doc.read("$.picture");
        Number brandId = doc.read("$.brand.id");

        assertThat(description).isEqualTo("Demo description product");
        assertThat(name).isEqualTo("Demo name product");
        assertThat(picture).isNull();
        assertThat(brandId).isEqualTo(103);
    }


    // PUT Product: Bad request due when put product missing field required
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

    // PUT Product: Unprocessable entity due when put product that brand or category not found
    @Test
    @DirtiesContext
    void attemptPutProductBut() {
        Product product = new Product();
        product.setName("Demo name product");
        Brand brand = new Brand();
        brand.setId(999999L);
        Category category = new Category();
        category.setId(99999L);
        product.setBrand(brand);
        product.setCategory(category);
        product.setBrand(brand);
        product.setCategory(category);
        product.setDescription("Demo description product");
        HttpEntity<Product> request = new HttpEntity<>(product);

        ResponseEntity<String> response =
                restTemplate.withBasicAuth("employee", "password")
                        .exchange("/api/products/202", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /*PUT Product: Product with that id not found in database*/
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


    /*PUT Product: Bad Cridential*/
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


    /*PUT Product: Unauthorized*/
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


    /*Patch Product: Update Product success*/
    @Test
    @DirtiesContext
    void shouldReturnDataUpdatedWhenPatchedAProduct() {
        Product product = new Product();
        product.setName("Demo name product");
        HttpEntity<Product> request = new HttpEntity<>(product);

        ResponseEntity<String> response =
                restTemplate.withBasicAuth("employee", "password")
                        .exchange("/api/products/202", HttpMethod.PATCH, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("employee", "password")
                .getForEntity("/api/products/202", String.class);
        DocumentContext doc = JsonPath.parse(getResponse.getBody());
        String description = doc.read("$.description");
        String name = doc.read("$.name");
        String picture = doc.read("$.picture");

        assertThat(name).isEqualTo("Demo name product");
        assertThat(description).isEqualTo("Description of Gucci shirt");
        assertThat(picture).isEqualTo("picture of product 1");
    }


    /*PATCH Product: Product with that id not found in database*/
    @Test
    @DirtiesContext
    void attemptUpdateProductWithIdNotFound() {
        Product product = new Product();
        product.setName("Demo name product");
        HttpEntity<Product> request = new HttpEntity<>(product);

        ResponseEntity<String> response =
                restTemplate.withBasicAuth("employee", "password")
                        .exchange("/api/products/100101", HttpMethod.PATCH, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    /*PATCH Product: Update Brand of Product success*/
    @Test
    @DirtiesContext
    void shouldCanChangeBrandOfProduct() {
        Product product = new Product();
        product.setName("Demo name product");
        Brand brand = new Brand();
        brand.setId(103L);
        product.setBrand(brand);
        HttpEntity<Product> request = new HttpEntity<>(product);

        ResponseEntity<String> patchResponse =
                restTemplate.withBasicAuth("employee", "password")
                        .exchange("/api/products/202", HttpMethod.PATCH, request, String.class);
        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        Number idBrandOfPatchReponse = JsonPath.parse(patchResponse.getBody()).read("$.brand.id");
        assertThat(idBrandOfPatchReponse).isEqualTo(103);
    }

    /*PATCH Product: Unprocessable entity due when put product that brand or category not found*/
    @Test
    @DirtiesContext
    void shouldNotChangeProductWhenAnyBrandOrCategoryNotExist() {
        Product product = new Product();
        product.setName("Demo name product");
        Brand brand = new Brand();
        brand.setId(52L);
        Category category = new Category();
        category.setId(9999L);
        product.setBrand(brand);
        product.setCategory(category);
        HttpEntity<Product> request = new HttpEntity<>(product);

        ResponseEntity<String> patchResponse =
                restTemplate.withBasicAuth("employee", "password")
                        .exchange("/api/products/153", HttpMethod.PATCH, request, String.class);
        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }


    /*PATCH Product: Bad Cridential*/
    @Test
    @DirtiesContext
    void attemptPatchProductWithBadCridential() {
        Product product = new Product();
        product.setName("Demo name product");
        Brand brand = new Brand();
        brand.setId(103L);
        product.setBrand(brand);
        HttpEntity<Product> request = new HttpEntity<>(product);

        ResponseEntity<String> patchResponse =
                restTemplate.withBasicAuth("customer", "password")
                        .exchange("/api/products/202", HttpMethod.PATCH, request, String.class);
        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    /*PATCH Product: Unauthorized*/
    @Test
    @DirtiesContext
    void attemptPatchProductButNotLogin() {
        Product product = new Product();
        product.setName("Demo name product");
        Brand brand = new Brand();
        brand.setId(103L);
        product.setBrand(brand);
        HttpEntity<Product> request = new HttpEntity<>(product);

        ResponseEntity<String> patchResponse =
                restTemplate.exchange("/api/products/202", HttpMethod.PATCH, request, String.class);
        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

}
