package thainguyen.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.JoinType;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import thainguyen.detailproduct.DetailProduct_;
import thainguyen.lineitem.LineItem_;
import thainguyen.order.Order;
import thainguyen.order.OrderRepository;
import thainguyen.order.Order_;

import java.util.List;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class OrderTests {
    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    OrderRepository repo;

    @Autowired
    EntityManager em;

    @Test
    void a () {
        HibernateCriteriaBuilder builder = em.unwrap(Session.class).getCriteriaBuilder();
        JpaCriteriaQuery<Tuple> mainQuery = builder.createTupleQuery();
        JpaRoot<Order> orderRoot = mainQuery.from(Order.class);
        var addressJoin = orderRoot.join(Order_.ADDRESS, JoinType.LEFT);
        var userRoot = orderRoot.join(Order_.USER, JoinType.LEFT);
        var paymentJoin = orderRoot.join(Order_.PAYMENT, JoinType.LEFT);
        var shipmentJoin = orderRoot.join(Order_.SHIPMENT, JoinType.LEFT);
        var lineItemJoin = orderRoot.join(Order_.LINE_ITEMS, JoinType.LEFT);

        var detailProductJoin = lineItemJoin.join(LineItem_.DETAIL_PRODUCT);
        var productJoin = detailProductJoin.join(DetailProduct_.PRODUCT);
        var sizeJoin = detailProductJoin.join(DetailProduct_.SIZE);

        mainQuery.multiselect(orderRoot, addressJoin, userRoot
                , paymentJoin, shipmentJoin, lineItemJoin, detailProductJoin
                , productJoin, sizeJoin).where(builder.and(
                builder.equal(orderRoot.get("id"), UUID.fromString("58ee5aec-f38d-496b-920c-1a0aa3caec74")),
                builder.equal(userRoot.get("username"), "oliviarodrigo")
        ));
        List<Tuple> tuples = em.createQuery(mainQuery).getResultList();
    }

}
