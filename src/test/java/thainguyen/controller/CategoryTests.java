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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class CategoryTests {

    @Autowired
    private TestRestTemplate restTemplate;


    /*GET Category: Get Category by id success*/
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


    /*GET Category: Category with that id not found in database*/
    @Test
    void shouldReturn404WhenRetrieveDataNotExist() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/categories/99999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    /*GET Category:Get Category by Id,  Category Unauthorized*/
    @Test
    void shouldReturn403WhenGetWhileUnauthenticated() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/categories/103", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    /*GET Category: Get all Categorys success*/
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


    /*GET Category: Get all Categorys, Category Unauthorized*/
    @Test
    void attemptGetAllCategoryButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/categories", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    /*POST Category: Create Category success haven't parent */
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

    /*POST Category: Create Category success have parent */
    @Test
    @DirtiesContext
    void shouldCreatedACategoryIsChildOfAnotherCategory() {
        Category category = new Category();
        category.setName("DEMO Category");
        category.setDescription("Description of DEMO Category");
        category.setPicture("Link picture of DEMO Category");
        Category parent = new Category();
        parent.setId(152L);
        category.setParent(parent);

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

        Number parentId = doc.read("$.data.parent.id");
        String parentName = doc.read("$.data.parent.name");
        String parentPicture = doc.read("$.data.parent.picture");

        // Self
        assertThat(name).isEqualTo("DEMO Category");
        assertThat(descriptioin).isEqualTo("Description of DEMO Category");
        assertThat(picture).isEqualTo("Link picture of DEMO Category");

        // Parent
        assertThat(parentId).isEqualTo(152);
        assertThat(parentName).isEqualTo("shirt");
        assertThat(parentPicture).isEqualTo("link picture of shirt");
    }

    /*POST Category: Create Category unsuccess because id parent not found*/
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

    /*POST Category: Forbiden because cridential info is bad*/
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


    /*POST Category: Bad request because info is null */
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


    /*POST Category: Unauthorized */
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


    /*PUT Category: Update Category success*/
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


    /*PUT Category: Bad request due missing info*/
    @Test
    void shouldReturnBadRequestWhenPuttedButMissingData() {
        Category category = new Category();
        category.setPicture("Demo link");
        // missing data required is name
        HttpEntity<Category> request = new HttpEntity<>(category);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/categories/152", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /*PUT Category: Category with that id not found in database*/
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


    /*PUT Category: Bad Cridential*/
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


    /*PUT Category: Unauthorized*/
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
