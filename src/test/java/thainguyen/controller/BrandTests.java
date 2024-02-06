package thainguyen.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import thainguyen.domain.Brand;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class BrandTests {

    @Autowired
    TestRestTemplate restTemplate;

//GET: Get Brand by id success

    @Test
    void shouldReturnABrandWhenIFindById() {
        ResponseEntity<String> response =
                restTemplate
                        .withBasicAuth("customer", "password")
                        .getForEntity("/api/brands/102", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext doc = JsonPath.parse(response.getBody());
        Number id = doc.read("$.data.id");
        String name = doc.read("$.data.name");
        assertThat(id).isEqualTo(102);
        assertThat(name).isEqualTo("gucci");
    }


//GET: Brand with that id not found in database

    @Test
    void shouldReturnNotFoundStatusWhenIdDoesNotExist() {
        ResponseEntity<String> response =
                restTemplate
                        .withBasicAuth("admin", "password")
                        .getForEntity("/api/brands/99999", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


//GET:Get Brand by Id,  Brand Unauthorized

    @Test
    void shouldReturn403WhenGetThatNotAuthenticated() {
        ResponseEntity<String> unauthorized =
                restTemplate.getForEntity("/api/brands/102", String.class);
        assertThat(unauthorized.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


//GET: Get all Brands success

    @Test
    void shouldReturnListWhenFindAll() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/brands", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext doc = JsonPath.parse(response.getBody());
        JSONArray ids = doc.read("$..id");
        assertThat(ids.size()).isEqualTo(2);
    }



//GET: Get all Brands, Brand Unauthorized

    @Test
    void shouldReturnUnauthorizedWhenGetListBrandButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/brands", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


//POST Brand: Create Brand success

    @Test
    @DirtiesContext
    void shouldReturnANewBrandWhenDataIsSaved() {
        Brand newBrand = new Brand();
        newBrand.setName("dior");
        newBrand.setLogo("link picture for dior");
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("admin", "password")
                .postForEntity("/api/brands", newBrand, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfCreatedBrand = response.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity(locationOfCreatedBrand.getPath(), String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext doc = JsonPath.parse(getResponse.getBody());
        String name = doc.read("$.data.name");
        String logo = doc.read("$.data.logo");

        assertThat(name).isEqualTo("dior");
        assertThat(logo).isEqualTo("link picture for dior");
    }


//POST Brand: Forbiden because cridential info is bad

    @Test
    @DirtiesContext
    void attemptPostNewBrandWithBadCridential() {
        Brand newBrand = new Brand();
        newBrand.setName("dior");
        newBrand.setLogo("link picture for dior");
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("customer", "password")
                .postForEntity("/api/brands", newBrand, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }


//POST Brand: Bad request because info is null

    @Test
    @DirtiesContext
    void attemptPostNewBrandButMissingInfoImportant() {
        Brand newBrand = new Brand();
        newBrand.setLogo("link picture for dior");
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/brands", newBrand, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


//POST Brand: Unauthorized

    @Test
    @DirtiesContext
    void attemptPostNewBrandButNotLogin() {
        Brand newBrand = new Brand();
        newBrand.setLogo("link picture for dior");
        ResponseEntity<Void> response = restTemplate
                .postForEntity("/api/brands", newBrand, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


//PUT Brand: Update Brand success

    @Test
    @DirtiesContext
    void shouldReturnOKDataIsPutted() {
        Map<String, Object> brandMap = new HashMap<>();
        brandMap.put("name", "gucci 2");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(brandMap);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/brands/102", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("employee", "password")
                .getForEntity("/api/brands/102", String.class);

        DocumentContext doc = JsonPath.parse(getResponse.getBody());
        String name = doc.read("$.data.name");
        String logo = doc.read("$.data.logo");
        assertThat(name).isEqualTo("gucci 2");
    }


//PUT: Brand with that id not found in database

    @Test
    void shouldReturn404WhenPutDataNotExist() {
        Map<String, Object> brandMap = new HashMap<>();
        brandMap.put("name", "gucci 2");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(brandMap);
        ResponseEntity<Brand> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/brands/90000", HttpMethod.PUT, request, Brand.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


//PUT Brand: Bad Cridential

    @Test
    void shouldReturn403WhenPutBrandThatBadCridential() {
        Brand attemptUpdate = new Brand();
        attemptUpdate.setName("attempt update");
        HttpEntity<Brand> request = new HttpEntity<>(attemptUpdate);
        ResponseEntity<Brand> response = restTemplate
                .withBasicAuth("customer", "password")
                .exchange("/api/brands/102", HttpMethod.PUT, request, Brand.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

//PUT Brand: Unauthorized

    @Test
    void attemptPutBrandButNotLogin() {
        Brand attemptUpdate = new Brand();
        attemptUpdate.setName("attempt update");
        HttpEntity<Brand> request = new HttpEntity<>(attemptUpdate);
        ResponseEntity<Brand> response = restTemplate
                .exchange("/api/brands/102", HttpMethod.PUT, request, Brand.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


}
