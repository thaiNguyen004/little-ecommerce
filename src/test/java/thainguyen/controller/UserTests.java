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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class UserTests {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    PasswordEncoder encoder;

    /*GET: Get User by id success*/
    @Test
    void shouldReturn200WhenIFindAUserExist() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/users/1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext doc = JsonPath.parse(response.getBody());
        Number id = doc.read("$.id");
        String fullname = doc.read("$.fullname");
        String username = doc.read("$.username");
        String password = doc.read("$.password");
        Number age = doc.read("$.age");

        assertThat(id).isEqualTo(1);
        assertThat(fullname).isEqualTo("Nguyen admin");
        assertThat(username).isEqualTo("admin");
        assertThat(password).isNotNull();
        assertThat(age).isEqualTo(19);
    }

    /*GET: User with that id not found in database*/
    @Test
    void shouldReturn404WhenIFindAUserNotExist() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/users/9999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /*GET:Get User by Id,  User Unauthorized*/
    @Test
    void shouldReturn401WhenFindUserButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/users/9999", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /*GET: Get all Users success*/
    @Test
    void shouldReturnListWhenFindAll() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/users", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext doc = JsonPath.parse(response.getBody());
        Number length = doc.read("$.length()");
        assertThat(length).isEqualTo(4);
    }

    /*GET: Get all users, User Unauthorized*/
    @Test
    void attemptGetAllUsersButNotLogin() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/users", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    /*POST User: Create User success */
    @Test
    @DirtiesContext
    void shouldReturn201WhenCreatedAUserSuccess() {
        User user = new User();
        user.setFullname("Demo new fullname");
        user.setUsername("olivia");
        user.setPassword(encoder.encode("olivia123"));
        user.setPosition("CUSTOMER");
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
        String fullname = doc.read("$.fullname");
        String username = doc.read("$.username");
        assertThat(fullname).isEqualTo("Demo new fullname");
        assertThat(username).isEqualTo("olivia");

    }

    /*POST User: Forbiden because cridential info is bad*/
    @Test
    @DirtiesContext
    void shouldCreateFailWhenBadCridential() {
        User user = new User();
        user.setFullname("Demo new fullname");
        user.setUsername("olivia");
        user.setPassword(encoder.encode("olivia123"));
        user.setPosition("CUSTOMER");
        user.setGender("Female");
        user.setEmail("oliviarodrigo@gmail.com");
        user.setAge(20);
        user.setAvatar("Link avatar demo");

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("customer", "password")
                .postForEntity("/api/users", user, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    /*POST User: Bad request because info is null */
    @Test
    @DirtiesContext
    void shouldReturn400WhenCreateAnUserThatMissingInfo() {
        User user = new User();
        user.setFullname("Demo new fullname");
        user.setPassword(encoder.encode("olivia123"));
        user.setPosition("CUSTOMER");
        user.setEmail("oliviarodrigo@gmail.com");
        user.setAge(20);
        user.setAvatar("Link avatar demo");

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .postForEntity("/api/users", user, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    /*POST User: Unauthorized */
    @Test
    @DirtiesContext
    void attemptReturn401WhenCreateAnUserButNotLogin() {
        User user = new User();
        user.setFullname("Demo new fullname");
        user.setPassword(encoder.encode("olivia123"));
        user.setPosition("CUSTOMER");
        user.setEmail("oliviarodrigo@gmail.com");
        user.setAge(20);
        user.setAvatar("Link avatar demo");

        ResponseEntity<Void> response = restTemplate
                .postForEntity("/api/users", user, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    /*PUT User: Update User success*/
    @Test
    @DirtiesContext
    void shouldReturnDataUpdatedWhenPuttedAUser() {
        User user = new User();
        // Tạm thời khi put sẽ làm null các field khong duoc set, so rather set those field or bad request
        user.setFullname("Fullname changed");
        user.setUsername("username changed");
        user.setPassword(encoder.encode("password-changed"));
        user.setPosition("ADMIN");
        user.setGender("Female");
        user.setEmail("oliviarodrigochanged@gmail.com");
        user.setAge(19);

        HttpEntity<User> request = new HttpEntity<>(user);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("employee", "password")
                .exchange("/api/users/53", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("employee", "password")
                .getForEntity("/api/users/53", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext doc = JsonPath.parse(getResponse.getBody());
        String username = doc.read("$.username");
        String fullname = doc.read("$.fullname");
        String avatar = doc.read("$.avatar");
        assertThat(username).isEqualTo("username changed");
        assertThat(fullname).isEqualTo("Fullname changed");
        assertThat(avatar).isNull();
    }

    /*PUT: User with that id not found in database*/
    @Test
    @DirtiesContext
    void shouldReturn404WhenPuttedAnUserNotExist() {
        User user = new User();
        // Tạm thời khi put sẽ làm null các field khong duoc set, so rather set those field or bad request
        user.setFullname("Fullname changed");
        user.setUsername("username changed");
        user.setPassword(encoder.encode("password-changed"));
        user.setPosition("ADMIN");
        user.setGender("Female");
        user.setEmail("oliviarodrigochanged@gmail.com");
        user.setAge(19);

        HttpEntity<User> request = new HttpEntity<>(user);

        ResponseEntity<String> response =
                restTemplate.withBasicAuth("employee", "password")
                        .exchange("/api/users/9999", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /*PUT User: Bad Cridential*/
    @Test
    @DirtiesContext
    void shouldReturnForbidenWhenPuttedAnUserWithBadCridential() {
        User user = new User();
        // Tạm thời khi put sẽ làm null các field khong duoc set, so rather set those field or bad request
        user.setFullname("Fullname changed");
        user.setUsername("username changed");
        user.setPassword(encoder.encode("password-changed"));
        user.setPosition("ADMIN");
        user.setGender("Female");
        user.setEmail("oliviarodrigochanged@gmail.com");
        user.setAge(19);

        HttpEntity<User> request = new HttpEntity<>(user);

        ResponseEntity<String> response =
                restTemplate.withBasicAuth("customer", "password")
                        .exchange("/api/users/3", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    /*PUT User: Unauthorized*/
    @Test
    @DirtiesContext
    void attemptUpdateUserButNotLogin() {
        User user = new User();
        // Tạm thời khi put sẽ làm null các field khong duoc set, so rather set those field or bad request
        user.setFullname("Fullname changed");
        user.setUsername("username changed");
        user.setPassword(encoder.encode("password-changed"));
        user.setPosition("ADMIN");
        user.setGender("Female");
        user.setEmail("oliviarodrigochanged@gmail.com");
        user.setAge(19);

        HttpEntity<User> request = new HttpEntity<>(user);

        ResponseEntity<String> response =
                restTemplate
                        .exchange("/api/users/3", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    /*Patch User: Update User success*/
    @Test
    @DirtiesContext
    void shouldReturnDataUpdatedWhenPatchedAProduct() {
        User user = new User();
        // Tạm thời khi put sẽ làm null các field khong duoc set, so rather set those field or bad request
        user.setFullname("fullname change");
        user.setAvatar("avatar change demo");

        HttpEntity<User> request = new HttpEntity<>(user);

        ResponseEntity<String> response =
                restTemplate.withBasicAuth("employee", "password")
                        .exchange("/api/users/53", HttpMethod.PATCH, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("admin", "password")
                .getForEntity("/api/users/53", String.class);
        DocumentContext doc = JsonPath.parse(getResponse.getBody());

        String fullname = doc.read("$.fullname");
        String position = doc.read("$.position");
        assertThat(fullname).isEqualTo("fullname change");
        assertThat(position).isEqualTo("CUSTOMER");

        String username = doc.read("$.username");
        Number age = doc.read("$.age");
        String avatar = doc.read("$.avatar");
        assertThat(username).isEqualTo("customer");
        assertThat(age).isEqualTo(19);
        assertThat(avatar).isEqualTo("avatar change demo");
    }

    /*PATCH User: User with that id not found in database*/
    @Test
    @DirtiesContext
    void shouldReturn404WhenPatchedAUserNotExist() {
        User user = new User();
        // Tạm thời khi put sẽ làm null các field khong duoc set, so rather set those field or bad request
        user.setFullname("fullname change");
        user.setAvatar("avatar change demo");

        HttpEntity<User> request = new HttpEntity<>(user);

        ResponseEntity<String> response =
                restTemplate.withBasicAuth("employee", "password")
                        .exchange("/api/users/5555", HttpMethod.PATCH, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }


    /*PATCH User: Bad Cridential*/
    @Test
    @DirtiesContext
    void shouldReturn403WhenPatchUserButBadCridential() {
        User user = new User();
        // Tạm thời khi put sẽ làm null các field khong duoc set, so rather set those field or bad request
        user.setFullname("fullname change");
        user.setAvatar("avatar change demo");

        HttpEntity<User> request = new HttpEntity<>(user);

        ResponseEntity<String> response =
                restTemplate.withBasicAuth("customer", "password")
                        .exchange("/api/users/53", HttpMethod.PATCH, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    /*PATCH User: Unauthorized*/
    @Test
    @DirtiesContext
    void attemptUpdateUserByPatchButNotLogin() {
        User user = new User();
        // Tạm thời khi put sẽ làm null các field khong duoc set, so rather set those field or bad request
        user.setFullname("fullname change");
        user.setAvatar("avatar change demo");

        HttpEntity<User> request = new HttpEntity<>(user);

        ResponseEntity<String> response =
                restTemplate.withBasicAuth("customer", "password")
                        .exchange("/api/users/53", HttpMethod.PATCH, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }


}
