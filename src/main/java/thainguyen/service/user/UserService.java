package thainguyen.service.user;

import thainguyen.domain.User;
import thainguyen.service.generic.GenericService;

import java.util.Optional;

public interface UserService extends GenericService<User> {

    Optional<User> findByUsername(String username);

    User create (User user);

    User updateByPut(Long id, User user);

    User updateByPatch(Long id, User user);

}
