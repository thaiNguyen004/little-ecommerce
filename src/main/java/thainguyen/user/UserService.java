package thainguyen.user;

import thainguyen.order.OrderDetailDto;
import thainguyen.order.OrderSimpleDto;
import thainguyen.generic.GenericService;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.UUID;

public interface UserService extends GenericService<User> {

    User findByUsername(String username);

    User create (User user) throws SQLIntegrityConstraintViolationException;

    User updateUser(Long id, User user);

    List<OrderSimpleDto> findAllOrder(String username, int start, int offset);

    OrderDetailDto findOrderById(UUID id, String username);
}
