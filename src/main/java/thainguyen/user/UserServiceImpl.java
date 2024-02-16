package thainguyen.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.JoinType;
import org.hibernate.Session;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaJoin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import thainguyen.detailproduct.DetailProduct;
import thainguyen.order.OrderSimpleDto;
import thainguyen.lineitem.LineItem;
import thainguyen.order.Order;
import thainguyen.product.Product;
import thainguyen.generic.GenericServiceImpl;
import thainguyen.size.Size;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl extends GenericServiceImpl<User>
        implements UserService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager em;

    @Autowired
    public UserServiceImpl(EntityManager em, UserRepository repo, PasswordEncoder passwordEncoder) {
        super(em, User.class);
        this.em = em;
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public User findByUsername(String username) {
        Optional<User> userOpt = repo.findByUsername(username);
        String messageError = "Invalid " + " UserID, " + " user not found";
        return userOpt.orElseThrow(() -> new NoResultException(messageError));
    }


    @Override
    public User create(User user) throws SQLIntegrityConstraintViolationException {
        boolean isEmailExist = repo.existsUserByEmail(user.getEmail());
        boolean isUsernameExist = repo.existsUserByUsername(user.getUsername());
        if (isEmailExist)
            throw new SQLIntegrityConstraintViolationException("User with email " + user.getEmail() + " already existed");
        if (isUsernameExist)
            throw new SQLIntegrityConstraintViolationException("User with username " + user.getUsername() + " already existed");
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


    @Override
    public List<OrderSimpleDto> findAllOrderSimpleDtoOwn(String username) {
        HibernateCriteriaBuilder builder = em.unwrap(Session.class).getCriteriaBuilder();
        JpaCriteriaQuery<OrderSimpleDto> mainQuery = builder.createQuery(OrderSimpleDto.class);
        var orderRoot = mainQuery.from(Order.class);
        var lineItemJoin = orderRoot.join(LineItem.class);

        var subQueryCountLineItem = mainQuery.subquery(Long.class);
        var subCountLineItemRoot = subQueryCountLineItem.from(LineItem.class);
        subQueryCountLineItem
                .select(builder.count(subCountLineItemRoot.get("id")))
                .where(builder.equal(
                        subCountLineItemRoot.get("order").get("id"),
                        orderRoot.get("id")
                ));

        // subQuery to get limit 1 lineItem for each order
        var subQueryLimit = mainQuery.subquery(Long.class);
        var subLineItemRoot = subQueryLimit.from(LineItem.class);
        subQueryLimit.select(subLineItemRoot.get("id"))
                .where(builder.equal(
                                orderRoot.get("id")
                                , subLineItemRoot.get("order").get("id")
                        )
                ).fetch(1);

        lineItemJoin.on(builder.equal(
                lineItemJoin.get("id")
                , subQueryLimit
        ));

        // Joins
        var userJoin = orderRoot.join("user");
        JpaJoin<LineItem, DetailProduct> detailProductJoin = lineItemJoin.join("detailProduct", JoinType.LEFT);
        JpaJoin<DetailProduct, Product> productJoin = detailProductJoin.join("product");
        JpaJoin<Size, DetailProduct> sizeJoin = detailProductJoin.join("size");

        mainQuery.select(builder.construct(
                OrderSimpleDto.class,
                orderRoot.get("id").alias("orderId"),
                subQueryCountLineItem.alias("numberOfLineItem"),
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
        List<OrderSimpleDto> result = em.createQuery(mainQuery).getResultList();
        if (result.isEmpty()) {
            throw new NoResultException("Orders list not found");
        }
        return result;
    }

}
