package thainguyen.user;

import thainguyen.order.OrderSimpleDto;
import thainguyen.generic.GenericService;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public interface UserService extends GenericService<User> {

    User findByUsername(String username);

    User create (User user) throws SQLIntegrityConstraintViolationException;

    User updateUser(Long id, User user);

    List<OrderSimpleDto> findAllOrderSimpleDtoOwn(String username);

}
