package thainguyen.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.JoinType;
import org.hibernate.Session;
import org.hibernate.query.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import thainguyen.address.Address;
import thainguyen.detailproduct.DetailProduct;
import thainguyen.detailproduct.DetailProduct_;
import thainguyen.lineitem.LineItem_;
import thainguyen.order.OrderDetailDto;
import thainguyen.order.OrderSimpleDto;
import thainguyen.lineitem.LineItem;
import thainguyen.order.Order;
import thainguyen.order.Order_;
import thainguyen.payment.Payment;
import thainguyen.product.Product;
import thainguyen.generic.GenericServiceImpl;
import thainguyen.shipment.Shipment;
import thainguyen.size.Size;
import thainguyen.tracking.Tracking;
import thainguyen.tracking.TrackingDAO;
import thainguyen.valuetype.Status;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends GenericServiceImpl<User>
        implements UserService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager em;
    private final TrackingDAO trackingDAO;

    @Autowired
    public UserServiceImpl(EntityManager em, UserRepository repo
            , PasswordEncoder passwordEncoder, TrackingDAO trackingDAO) {
        super(em, User.class);
        this.em = em;
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.trackingDAO = trackingDAO;
    }


    @Override
    public User findByUsername(String username) {
        Optional<User> userOpt = repo.findByUsername(username);
        return userOpt.orElseThrow(() -> new NoResultException("Invalid UserID, user not found"));
    }


    @Override
    public User create(User user) throws SQLIntegrityConstraintViolationException {
        boolean isEmailExist = repo.existsUserByEmail(user.getEmail());
        boolean isUsernameExist = repo.existsUserByUsername(user.getUsername());
        if (isEmailExist)
            throw new SQLIntegrityConstraintViolationException("User with email "
                    + user.getEmail() + " already existed");
        if (isUsernameExist)
            throw new SQLIntegrityConstraintViolationException("User with username "
                    + user.getUsername() + " already existed");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repo.save(user);
    }


    @Override
    public User updateUser(Long id, User user) {
        User userPersist = findById(id);
        if (user.getFullname() != null) {
            userPersist.setFullname(user.getFullname());
        }
        if (user.getUsername() != null) {
            userPersist.setUsername(user.getUsername());
        }
        if (user.getPassword() != null) {
            userPersist.setPassword(user.getPassword());
        }
        if (user.getAvatar() != null) {
            userPersist.setAvatar(user.getAvatar());
        }
        if (user.getEmail() != null) {
            userPersist.setEmail(user.getEmail());
        }
        if (user.getAge() != null) {
            userPersist.setAge(user.getAge());
        }
        if (user.getPosition() != null) {
            userPersist.setPosition(user.getPosition());
        }
        if (user.getGender() != null) {
            userPersist.setGender(user.getGender());
        }
        return repo.save(userPersist);
    }


    private JpaSubQuery<Long> createSubQueryCalcSumLineItem(
            JpaCriteriaQuery<OrderSimpleDto> mainQuery,
            JpaRoot<Order> orderRoot, HibernateCriteriaBuilder builder) {
        // (select count(l.id) from line_item l where l.order_id = o.id)
        var subQueryCountLineItem = mainQuery.subquery(Long.class);
        var subCountLineItemRoot = subQueryCountLineItem.from(LineItem.class);
        subQueryCountLineItem
                .select(builder.count(subCountLineItemRoot.get("id")))
                .where(builder.equal(
                        subCountLineItemRoot.get("order").get("id"),
                        orderRoot.get("id")
                ));
        return subQueryCountLineItem;
    }




    @Override
    public List<OrderSimpleDto> findAllOrder(String username, int start, int offset) {
        HibernateCriteriaBuilder builder = em.unwrap(Session.class).getCriteriaBuilder();
        JpaCriteriaQuery<OrderSimpleDto> mainQuery = builder.createQuery(OrderSimpleDto.class);
        var orderRoot = mainQuery.from(Order.class);
        // Joins
        var userJoin = orderRoot.join(Order_.USER);
        var lineItemJoin = orderRoot.join(Order_.LINE_ITEMS);

        var detailProductJoin = lineItemJoin.join(LineItem_.DETAIL_PRODUCT);
        var productJoin = detailProductJoin.join(DetailProduct_.PRODUCT);
        var sizeJoin = detailProductJoin.join(DetailProduct_.SIZE);

        var subQueryCountLineItem = createSubQueryCalcSumLineItem(mainQuery, orderRoot, builder);

        // subQuery to get limit 1 lineItem for each order
        // join line_item l1 on l1.id = (select l2.id from line_item l2 where o.id = l2.order_id limit 1)
        var subQueryLimit = mainQuery.subquery(Long.class);
        var subLineItemRoot = subQueryLimit.from(LineItem.class);
        subQueryLimit.select(subLineItemRoot.get(LineItem_.ID))
                .where(builder.equal(
                                orderRoot.get(Order_.ID)
                                , subLineItemRoot.get(LineItem_.ORDER).get("id")
                        )
                ).fetch(1);

        lineItemJoin.on(builder.equal(
                lineItemJoin.get(LineItem_.ID), subQueryLimit
        ));

        mainQuery.select(builder.construct(
                OrderSimpleDto.class,
                orderRoot.get("id").alias("orderId"),
                subQueryCountLineItem.alias("numberOfLineItem"),
                productJoin.get("id").alias("productId"),
                productJoin.get("name").alias("productName"),
                productJoin.get("picture").alias("productPicture"),
                sizeJoin.get("name").alias("sizeName"),
                lineItemJoin.get("amount").alias("amount"),
                lineItemJoin.get("quantity").alias("quantity"),
                orderRoot.get("totalPriceBeforeDiscount"),
                orderRoot.get("totalPriceAfterDiscount"),
                orderRoot.get("status"),
                orderRoot.get("placedAt"),
                orderRoot.get("modifiedAt")
        )).where(builder.equal(
                userJoin.get("username"),
                username
        ));


        // execute
        List<OrderSimpleDto> result = em.createQuery(mainQuery)
                .setMaxResults(offset)
                .setFirstResult(start)
                .getResultList();
        if (result.isEmpty()) {
            throw new NoResultException("Orders list not found");
        }
        return result;
    }

    @Override
    public OrderDetailDto findOrderById(UUID id, String username) {
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

        mainQuery
                .multiselect(
                        orderRoot.alias("order"),
                        paymentJoin.alias("payment"),
                        shipmentJoin.alias("shipment"),
                        productJoin.alias("product"),
                        sizeJoin.alias("size"),
                        lineItemJoin.alias("lineItems"),
                        addressJoin.alias("address"),
                        userRoot.alias("user")
                ).where(builder.and(
                        builder.equal(orderRoot.get("id"), id),
                        builder.equal(userRoot.get("username"), username)
                ));

        List<Tuple> tuples = em.createQuery(mainQuery).getResultList();

        OrderDetailDto orderDetailDto = new OrderDetailDto();
        if (!tuples.isEmpty()) {
            tuples.forEach(tuple -> {
                System.out.println("info detail: " + tuple.toString());
                orderDetailDto.setOrderId(tuple.get("orderId", UUID.class));
                OrderDetailDto.LineItem lineItem = OrderDetailDto.LineItem.builder()
                        .id(tuple.get("lineItemId", Long.class))
                        .productId(tuple.get("productId", Long.class))
                        .productName(tuple.get("productName", String.class))
                        .productPicture(tuple.get("productPicture", String.class))
                        .sizeName(tuple.get("sizeName", Size.Name.class))
                        .amount(tuple.get("amount", Integer.class))
                        .quantity(tuple.get("quantity", Integer.class))
                        .build();
                orderDetailDto.getLineItems().add(lineItem);
                OrderDetailDto.Address address = OrderDetailDto.Address.builder()
                        .province(tuple.get("province", String.class))
                        .district(tuple.get("district", String.class))
                        .ward(tuple.get("ward", String.class))
                        .detailAddress(tuple.get("detailAddress", String.class))
                        .phoneNumber(tuple.get("phoneNumber", String.class))
                        .fullname(tuple.get("fullname", String.class)).build();
                orderDetailDto.setAddress(address);
                OrderDetailDto.Shipment shipment = OrderDetailDto.Shipment.builder()
                        .fee(tuple.get("fee", Integer.class))
                        .insuranceFee(tuple.get("insuranceFee", Integer.class))
                        .labelCode(tuple.get("labelCode", String.class)).build();
                orderDetailDto.setShipment(shipment);

                // get tracking
                try {
                    List<Tracking> trackings = trackingDAO
                            .findAllTrackingByShipmentId(tuple.get("shipmentId", Long.class));
                    List<OrderDetailDto.Tracking> trackingDto = trackings.stream().map(tracking -> OrderDetailDto.Tracking.builder()
                            .statusNumber(tracking.getStatusNumber())
                            .statusText(tracking.getStatusText())
                            .date(tracking.getDate()).build())
                            .toList();
                    orderDetailDto.setTrackings(trackingDto);
                } catch (NoResultException ex) {
                    System.out.println(ex.getMessage());
                }

                orderDetailDto.setCurrentStatus(tuple.get("currentStatus", Status.class));
                orderDetailDto.setPaymentMethod(tuple.get("paymentMethod", Payment.PaymentMethod.class));
                orderDetailDto.setTotalPriceBeforeDiscount(tuple.get("totalBeforeDisc", Integer.class));
                orderDetailDto.setTotalPriceAfterDiscount(tuple.get("totalAfterDisc", Integer.class));
                orderDetailDto.setPlacedAt(tuple.get("placedAt", LocalDateTime.class));
                orderDetailDto.setPlacedAt(tuple.get("modifiedAt", LocalDateTime.class));
            });
            return orderDetailDto;
        } else {
            throw new NoResultException("Invalid OrderID, order not found");
        }
    }
}
