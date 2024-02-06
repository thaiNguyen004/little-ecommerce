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
import org.springframework.transaction.annotation.Transactional;
import thainguyen.data.SizeRepository;
import thainguyen.domain.Brand;
import thainguyen.domain.Category;
import thainguyen.domain.Size;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class SizeTests {

    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    SizeRepository sizeRepository;

    /*GET: Get Size by id success*/
    @Test
    void shouldReturnASizeWhenIFindById() {
        ResponseEntity<String> response =
                restTemplate
                        .withBasicAuth("customer", "password")
                        .getForEntity("/api/sizes/252", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext doc = JsonPath.parse(response.getBody());
        Number id = doc.read("$.data.id");
        String name = doc.read("$.data.name");
        assertThat(id).isEqualTo(252);
        assertThat(name).isEqualTo("M");
    }


    /*GET: Size with that id not found in database*/
    @Test
    void attemptFindSizeByIdNotFound() {
        ResponseEntity<String> response =
                restTemplate
                        .withBasicAuth("customer", "password")
                        .getForEntity("/api/sizes/99999", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    /*GET:Get Size by Id,  Size Unauthorized*/
    @Test
    void attemptFindSizeByIdButNotLogin() {
        ResponseEntity<String> response =
                restTemplate
                        .getForEntity("/api/sizes/252", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    /*GET: Get all Sizes success*/
    @Test
    void shouldReturnListWhenFindAll() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/sizes", String.class);
        Number sizeOfSize = JsonPath.parse(response.getBody()).read("$.data.length()");
        assertThat(sizeOfSize).isEqualTo(2);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    /*GET: Get all Sizes, Size Unauthorized*/
    @Test
    void attemptFindAllSizeButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/sizes", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    /*POST Size: Create Size success */
    @Test
    @DirtiesContext
    void attemptCreateAnSizeSuccess() {
        Size newSize = new Size();
        newSize.setName(Size.Type.XL);
        newSize.setWidth(200);
        newSize.setChest(100);
        newSize.setLength(400);
        Brand brand = new Brand();
        brand.setId(102L);
        Category category = new Category();
        category.setId(152L);
        newSize.setBrand(brand);
        newSize.setCategory(category);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("admin", "password")
                .postForEntity("/api/sizes", newSize, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfCreatedSize = response.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity(locationOfCreatedSize, String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext doc = JsonPath.parse(getResponse.getBody());
        String name = doc.read("$.data.name");
        Number width = doc.read("$.data.width");

        assertThat(name).isEqualTo("XL");
        assertThat(width).isEqualTo(200);
    }


    /*POST Size: Forbiden because cridential info is bad*/
    @Test
    @DirtiesContext
    void attemptCreateAnSizeFailDueBadCridential() {
        Size newSize = new Size();
        newSize.setName(Size.Type.XL);
        newSize.setWidth(200);
        newSize.setChest(100);
        newSize.setLength(400);
        Brand brand = new Brand();
        brand.setId(52L);
        Category category = new Category();
        category.setId(103L);
        newSize.setBrand(brand);
        newSize.setCategory(category);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("customer", "password")
                .postForEntity("/api/sizes", newSize, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    /*POST Size: Not_found entity due category or brand is not found*/
    @Test
    @DirtiesContext
    void attemptCreateAnSizeFailDueBrandOrCategoryNotFound() {
        Size newSize = new Size();
        newSize.setName(Size.Type.XL);
        newSize.setWidth(200);
        newSize.setChest(100);
        newSize.setLength(400);
        Brand brand = new Brand();
        brand.setId(9999L); // brand not found
        Category category = new Category();
        category.setId(103L);
        newSize.setBrand(brand);
        newSize.setCategory(category);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/sizes", newSize, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /*POST Size: Bad request because info is null */
    @Test
    @DirtiesContext
    void attemptCreateAnSizeFailDueNullFieldRequired() {
        Size newSize = new Size();
//        newSize.setName(Size.Type.XL);
        newSize.setWidth(200);
        newSize.setChest(100);
        newSize.setLength(400);
        Brand brand = new Brand();
        brand.setId(102L);
        Category category = new Category();
        category.setId(152L);
        newSize.setBrand(brand);
        newSize.setCategory(category);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/sizes", newSize, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    /*POST Size: Unauthorized */
    @Test
    @DirtiesContext
    void attemptCreateAnSizeButNotLogin() {
        Size newSize = new Size();
        newSize.setName(Size.Type.XL);
        newSize.setWidth(200);
        newSize.setChest(100);
        newSize.setLength(400);
        Brand brand = new Brand();
        brand.setId(52L);
        Category category = new Category();
        category.setId(103L);
        newSize.setBrand(brand);
        newSize.setCategory(category);

        ResponseEntity<Void> response = restTemplate
                .postForEntity("/api/sizes", newSize, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }



    /*Patch Size: Update Size success*/
    @Test
    void shouldReturnOKWhenDataIsPatched() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "S");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/sizes/252", HttpMethod.PUT, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("employee", "password")
                .getForEntity("/api/sizes/252", String.class);

        DocumentContext doc = JsonPath.parse(getResponse.getBody());

        String name = doc.read("$.data.name");
        Number categoryId = doc.read("$.data.category.id");

        assertThat(name).isEqualTo("S");
        assertThat(categoryId).isNotNull();
    }

    /*Patch Size: Update Size failed due category or brand not found*/
    @Test
    void attemptPatchSizeWithBrandOrCategoryNotFound() {
        Map<String, Object> map = new HashMap<>();
        map.put("category", Map.of("id", "1000000"));
        map.put("brand", Map.of("id", "102"));
        /*map.put("width", "2000");
        map.put("chest", "2000");
        map.put("length", "2000");
        map.put("category", "153");
        map.put("brand", "102");
        map.put("brand", Map.of("id", "103"));
        map.put("category", Map.of("id", "154"));*/
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/sizes/252", HttpMethod.PUT, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    /*PATCH Size: Size with that id not found in database*/
    @Test
    void attemptPatchSizeButIdNotFound() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "S");
        /*map.put("width", "2000");
        map.put("chest", "2000");
        map.put("length", "2000");
        map.put("category", "153");
        map.put("brand", "102");
        map.put("brand", Map.of("id", "103"));
        map.put("category", Map.of("id", "154"));*/
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/sizes/801042", HttpMethod.PUT, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    /*PATCH Size: Bad Cridential*/
    @Test
    void attemptPatchSizeWithBadCridential() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "S");
        /*map.put("width", "2000");
        map.put("chest", "2000");
        map.put("length", "2000");
        map.put("category", "153");
        map.put("brand", "102");
        map.put("brand", Map.of("id", "103"));
        map.put("category", Map.of("id", "154"));*/
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .exchange("/api/sizes/252", HttpMethod.PUT, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }


    /*PATCH Size: Unauthorized*/
    @Test
    void attemptPatchSizeButNotLogin() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "S");
        /*map.put("width", "2000");
        map.put("chest", "2000");
        map.put("length", "2000");
        map.put("category", "153");
        map.put("brand", "102");
        map.put("brand", Map.of("id", "103"));
        map.put("category", Map.of("id", "154"));*/
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);
        ResponseEntity<String> response = restTemplate
                .exchange("/api/sizes/252", HttpMethod.PUT, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

}
