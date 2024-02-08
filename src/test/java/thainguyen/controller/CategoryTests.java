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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import thainguyen.domain.Category;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class CategoryTests {

    @Autowired
    private TestRestTemplate restTemplate;


    //    GET: find by id - success(OK)
    @Test
    void shnouldReturnCategoryWheFindById() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/categories/153", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext doc = JsonPath.parse(response.getBody());
        Number id = doc.read("$.data.id");
        String name = doc.read("$.data.name");
        String picture = doc.read("$.data.picture");
        String descriptioin = doc.read("$.data.description");

        assertThat(id).isEqualTo(153);
        assertThat(name).isEqualTo("pants");
        assertThat(descriptioin).isEqualTo("description of pants");
        assertThat(picture).isEqualTo("link picture of pants");
    }


    //    GET: find by id - fail(NOT_FOUND) - id not found in database
    @Test
    void shouldReturn404WhenRetrieveDataNotExist() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/categories/99999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    //    GET: find by id - fail(UNAUTHORIZED) - not login
    @Test
    void shouldReturn403WhenGetWhileUnauthenticated() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/categories/103", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    //    GET: find all - success(OK)
    @Test
    void shouldReturnListDataWhenRetrieveListing() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/categories", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext doc = JsonPath.parse(response.getBody());
        JSONArray ids = doc.read("$..id");
        assertThat(ids.size()).isEqualTo(4);
    }


    //    GET: find all - fail(UNAUTHORIZED)
    @Test
    void attemptGetAllCategoryButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/categories", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    //    POST: create a new Category - success(CREATED)
    @Test
    @DirtiesContext
    void shouldReturn201WhenCreatedAnCategory() {
        Category category = new Category();
        category.setName("DEMO Category");
        category.setDescription("Description of DEMO Category");
        category.setPicture("Link picture of DEMO Category");

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("admin", "password")
                .postForEntity("/api/categories", category, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        URI locationOfNewCategory = response.getHeaders().getLocation();

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity(locationOfNewCategory.getPath(), String.class);
        DocumentContext doc = JsonPath.parse(getResponse.getBody());
        String name = doc.read("$.data.name");
        String picture = doc.read("$.data.picture");
        String descriptioin = doc.read("$.data.description");
        Number parent = doc.read("$.data.parent");

        assertThat(name).isEqualTo("DEMO Category");
        assertThat(descriptioin).isEqualTo("Description of DEMO Category");
        assertThat(picture).isEqualTo("Link picture of DEMO Category");
        assertThat(parent).isNull();
    }


    //    POST: create a new Category - fail(NOT_FOUND) - info not found in database
    @Test
    @DirtiesContext
    void shouldReturn404WhenAttemptCreateAnCategoryWithParentNotFound() {
        Category category = new Category();
        category.setName("DEMO Category");
        category.setDescription("Description of DEMO Category");
        category.setPicture("Link picture of DEMO Category");
        Category parent = new Category();
        parent.setId(9999L);
        category.setParent(parent);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("admin", "password")
                .postForEntity("/api/categories", category, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    //    POST: create a new Category - fail(FORBIDDEN) - credential info is bad
    @Test
    void attemptPostCategoryWithBadCridential() {
        Category category = new Category();
        category.setName("DEMO Category");
        category.setDescription("Description of DEMO Category");
        category.setPicture("Link picture of DEMO Category");
        Category parent = new Category();
        parent.setId(153L);
        category.setParent(parent);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("customer", "password")
                .postForEntity("/api/categories", category, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }


    //    POST: create a new Category - fail(BAD_REQUEST) - info importance is null
    @Test
    void attemptPostCategoryButMissingInfoImportant() {
        Category category = new Category();
//        category.setName("DEMO Category");
        category.setDescription("Description of DEMO Category");
        category.setPicture("Link picture of DEMO Category");
        Category parent = new Category();
        parent.setId(153L);
        category.setParent(parent);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("admin", "password")
                .postForEntity("/api/categories", category, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    //    POST: create a new Category - fail(UNAUTHORIZED) - not login
    @Test
    void attemptPostCategoryButNotLogin() {
        Category category = new Category();
        category.setName("DEMO Category");
        category.setDescription("Description of DEMO Category");
        category.setPicture("Link picture of DEMO Category");
        Category parent = new Category();
        parent.setId(153L);
        category.setParent(parent);

        ResponseEntity<Void> response = restTemplate
                .postForEntity("/api/categories", category, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    //    PUT: update Category - success(OK)
    @Test
    @DirtiesContext
    void shouldReturnOKAndBodyWhenIPuttedDataSuccess() {
        Category category = new Category();
        category.setName("Demo name");
        category.setPicture("Demo link");
        HttpEntity<Category> request = new HttpEntity<>(category);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/categories/152", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext doc1 = JsonPath.parse(response.getBody());
        String description = doc1.read("$.data.description");
        assertThat(description).isNotNull();

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("employee", "password")
                .getForEntity("/api/categories/152", String.class);
        DocumentContext doc2 = JsonPath.parse(getResponse.getBody());
        String nameChanged = doc2.read("$.data.name");
        String parent = doc2.read("$.data.parent");
        assertThat(nameChanged).isEqualTo("Demo name");
        assertThat(parent).isNull();
    }


    //    PUT: update Category - fail(NOT_FOUND) - id category not found in database
    @Test
    void attemptPutCategoryButCategoryIdNotFound() {
        Category category = new Category();
        category.setName("name ro rang");
        category.setPicture("Demo link");
        HttpEntity<Category> request = new HttpEntity<>(category);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/categories/9999", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    //    PUT: update Category - fail(NOT_FOUND) - id parent not found in database
    @Test
    void attemptPutCategoryButParentIdNotFound() {
        Map<String, Object> map = new HashMap<>();
        map.put("picture", "new picture");
        map.put("parent", Map.of("id", 999999));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/categories/153", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    //    PUT: update Category - fail(FORBIDDEN) - credential info is bad
    @Test
    void attemptPutCategoryWithBadCridential() {
        Category category = new Category();
        category.setPicture("Demo link");
        // missing data required is name
        HttpEntity<Category> request = new HttpEntity<>(category);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .withBasicAuth("customer", "password")
                .exchange("/api/categories/152", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    //    PUT: update Category - fail(UNAUTHORIZED) - not login
    @Test
    void attemptPutCategoryButNotLogin() {
        Category category = new Category();
        category.setPicture("Demo link");
        // missing data required is name
        HttpEntity<Category> request = new HttpEntity<>(category);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/categories/152", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

}
