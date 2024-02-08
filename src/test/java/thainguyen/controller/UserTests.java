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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import thainguyen.domain.User;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class UserTests {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    PasswordEncoder encoder;

//GET: find by id - success(OK)

    @Test
    void shouldReturn200WhenIFindAUserExist() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/users/1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext doc = JsonPath.parse(response.getBody());
        Number id = doc.read("$.data.id");
        String fullname = doc.read("$.data.fullname");
        String username = doc.read("$.data.username");
        String password = doc.read("$.data.password");
        Number age = doc.read("$.data.age");

        assertThat(id).isEqualTo(1);
        assertThat(fullname).isEqualTo("Nguyen admin");
        assertThat(username).isEqualTo("admin");
        assertThat(password).isNotNull();
        assertThat(age).isEqualTo(19);
    }

//GET: find by id - fail(NOT_FOUND) - id not found in database

    @Test
    void shouldReturn404WhenIFindAUserNotExist() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/users/9999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

//GET: find by id - fail(UNAUTHORIZED) - not login

    @Test
    void shouldReturn401WhenFindUserButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/users/9999", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

//GET: find all - success(OK)

    @Test
    void shouldReturnListWhenFindAll() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/users", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext doc = JsonPath.parse(response.getBody());
        Number length = doc.read("$.length()");
        assertThat(length).isEqualTo(3);
    }

//GET: find all - fail(UNAUTHORIZED) - because not login

    @Test
    void attemptGetAllUsersButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/users", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


//POST: create a new User - success(CREATED)

    @Test
    @DirtiesContext
    void shouldReturn201WhenCreatedAUserSuccess() {
        User user = new User();
        user.setFullname("Demo new fullname");
        user.setUsername("olivia");
        user.setPassword(encoder.encode("olivia123"));
        user.setPosition(User.Position.CUSTOMER);
        user.setGender("Female");
        user.setEmail("oliviarodrigo@gmail.com");
        user.setAge(20);
        user.setAvatar("Link avatar demo");

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/users", user, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewUser = response.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity(locationOfNewUser, String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext doc = JsonPath.parse(getResponse.getBody());
        String fullname = doc.read("$.data.fullname");
        String username = doc.read("$.data.username");
        assertThat(fullname).isEqualTo("Demo new fullname");
        assertThat(username).isEqualTo("olivia");

    }

//POST: create a new User - fail(FORBIDDEN) - because credential info is bad

    @Test
    @DirtiesContext
    void shouldCreateFailWhenBadCridential() {
        User user = new User();
        user.setFullname("Demo new fullname");
        user.setUsername("olivia");
        user.setPassword(encoder.encode("olivia123"));
        user.setPosition(User.Position.CUSTOMER);
        user.setGender("Female");
        user.setEmail("oliviarodrigo@gmail.com");
        user.setAge(20);
        user.setAvatar("Link avatar demo");

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("customer", "password")
                .postForEntity("/api/users", user, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

//POST: create a new User - fail(BAD_REQUEST) - because info is null

    @Test
    @DirtiesContext
    void shouldReturn400WhenCreateAnUserThatMissingInfo() {
        User user = new User();
        user.setFullname("Demo new fullname");
        user.setPassword(encoder.encode("olivia123"));
        user.setPosition(User.Position.CUSTOMER);
        user.setEmail("oliviarodrigo@gmail.com");
        user.setAge(20);
        user.setAvatar("Link avatar demo");

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/users", user, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
//POST: create a new User - fail(UNAUTHORIZED) - not login

    @Test
    @DirtiesContext
    void attemptReturn401WhenCreateAnUserButNotLogin() {
        User user = new User();
        user.setFullname("Demo new fullname");
        user.setPassword(encoder.encode("olivia123"));
        user.setPosition(User.Position.CUSTOMER);
        user.setEmail("oliviarodrigo@gmail.com");
        user.setAge(20);
        user.setAvatar("Link avatar demo");

        ResponseEntity<Void> response = restTemplate
                .postForEntity("/api/users", user, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


//PUT: update User - success(OK)

    @Test
    @DirtiesContext
    void shouldReturnDataUpdatedWhenPuttedAUser() {
        Map<String, Object> map = new HashMap<>();
        map.put("fullname", "Fullname changed");
        map.put("username", "username changed");
        map.put("password", "password-changed");
        map.put("position", "ADMIN");
        map.put("gender", "female");
        map.put("email", "oliviarodrigochanged@gmail.com");
        map.put("age", 19);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/users/53", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("employee", "password")
                .getForEntity("/api/users/53", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext doc = JsonPath.parse(getResponse.getBody());
        String username = doc.read("$.data.username");
        String fullname = doc.read("$.data.fullname");
        String avatar = doc.read("$.data.avatar");
        assertThat(username).isEqualTo("username changed");
        assertThat(fullname).isEqualTo("Fullname changed");
        assertThat(avatar).isNotNull();
    }

//PUT: update User - fail(NOT_FOUND) - id not found in database

    @Test
    @DirtiesContext
    void shouldReturn404WhenPuttedAnUserNotExist() {
        Map<String, Object> map = new HashMap<>();
        map.put("fullname", "Fullname changed");
        map.put("username", "username changed");
        map.put("password", "password-changed");
        map.put("position", "ADMIN");
        map.put("gender", "female");
        map.put("email", "oliviarodrigochanged@gmail.com");
        map.put("age", 19);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);

        ResponseEntity<String> response =
                restTemplate.withBasicAuth("employee", "password")
                        .exchange("/api/users/9999", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

//PUT: update User - fail(FORBIDDEN) - credential info is bad

    @Test
    @DirtiesContext
    void shouldReturnForbidenWhenPuttedAnUserWithBadCridential() {
        Map<String, Object> map = new HashMap<>();
        map.put("fullname", "Fullname changed");
        map.put("username", "username changed");
        map.put("password", "password-changed");
        map.put("position", "ADMIN");
        map.put("gender", "female");
        map.put("email", "oliviarodrigochanged@gmail.com");
        map.put("age", 19);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);

        ResponseEntity<String> response =
                restTemplate.withBasicAuth("customer", "password")
                        .exchange("/api/users/3", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

//PUT: update User - fail(UNAUTHORIZED) - not login

    @Test
    @DirtiesContext
    void attemptUpdateUserButNotLogin() {
        Map<String, Object> map = new HashMap<>();
        map.put("fullname", "Fullname changed");
        map.put("username", "username changed");
        map.put("password", "password-changed");
        map.put("position", "ADMIN");
        map.put("gender", "female");
        map.put("email", "oliviarodrigochanged@gmail.com");
        map.put("age", 19);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map);

        ResponseEntity<String> response =
                restTemplate
                        .exchange("/api/users/3", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

}
