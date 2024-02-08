package thainguyen.service.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import thainguyen.data.UserRepository;
import thainguyen.domain.User;
import thainguyen.service.generic.GenericServiceImpl;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Optional;


@Service
public class UserServiceImpl extends GenericServiceImpl<User>
        implements UserService {

    private final UserRepository repo;

    @Autowired
    public UserServiceImpl(EntityManager em, UserRepository repo) {
        super(em, User.class);
        this.repo = repo;
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
}
