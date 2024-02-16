package thainguyen.controller;

import static org.assertj.core.api.Assertions.*;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import thainguyen.data.OrderRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class OrderTests {
    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    OrderRepository repo;

}
