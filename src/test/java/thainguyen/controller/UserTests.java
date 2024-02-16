package thainguyen.controller;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class UserTests {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    EntityManager em;

    // GET: find all orders own by user - success(OK)
    @Test
    void findAllOrderOwnByUserSuccess() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("oliviarodrigo", "password")
                .getForEntity("/api/users/orders", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    // GET: find all orders own by user - fail(NOT_FOUND)
    @Test
    void findAllOrderOwnByUserNotfound() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer2", "password")
                .getForEntity("/api/users/orders", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    // GET: find all orders own by user - fail(UNAUTHORIZED)
    @Test
    void findAllOrderOwnByUserUnauthorized() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/users/orders", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

}
