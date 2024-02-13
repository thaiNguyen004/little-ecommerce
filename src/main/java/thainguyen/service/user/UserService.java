package thainguyen.service.user;

import thainguyen.domain.Order;
import thainguyen.domain.User;
import thainguyen.service.generic.GenericService;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public interface UserService extends GenericService<User> {

    User findByUsername(String username);

    User create (User user) throws SQLIntegrityConstraintViolationException;

    User updateUser(Long id, User user);

    List<Order> findAllOrdersOwn(String username);

}
