package thainguyen.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import thainguyen.generic.GenericServiceImpl;
import thainguyen.tracking.TrackingDAO;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Optional;

@Service
public class UserServiceImpl extends GenericServiceImpl<User>
        implements UserService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final TrackingDAO trackingDAO;

    @Autowired
    public UserServiceImpl(EntityManager em, UserRepository repo
            , PasswordEncoder passwordEncoder, TrackingDAO trackingDAO) {
        super(em, User.class);
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

}
