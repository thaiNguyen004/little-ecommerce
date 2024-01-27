package thainguyen.service.user;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import thainguyen.data.UserRepository;
import thainguyen.domain.User;
import thainguyen.service.generic.GenericServiceImpl;

import java.util.Optional;


@Service
public class UserServiceImpl extends GenericServiceImpl<User> implements UserService {

    private final UserRepository repo;

    @Autowired
    public UserServiceImpl(EntityManager em, UserRepository repo) {
        super(em, User.class);
        this.repo = repo;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repo.findByUsername(username);
    }

    @Override
    public User create(User user) {
        return repo.save(user);
    }

    @Override
    public User updateByPut(Long id, User user) {
        return repo.findById(id).map(userPersist -> {
            user.setId(id);
            user.setVersion(userPersist.getVersion());
            return repo.save(user);
        }).orElseGet(() -> null);
    }

    @Override
    public User updateByPatch(Long id, User user) {
        return repo.findById(id).map(userPersist -> {
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
        }).orElseGet(() -> null);
    }
}
