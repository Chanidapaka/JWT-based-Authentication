package sit.int204.jwtdemo.entities.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int204.jwtdemo.entities.entities.User;
import sit.int204.jwtdemo.entities.repositories.UserRepository;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepo;
    private Argon2PasswordEncoder passwordEncoder =
            new Argon2PasswordEncoder(16, 32, 8, 1024*128, 2);

    public User findUserById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    private void checkDuplication(User user) {
        if (userRepo.existsUserByUsernameOrEmail(
                user.getUsername(), user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "User name or Email already exist !!! ("
                            + user.getUsername() + ", " + user.getEmail() + ')');
        }
    }
        public User createUser(User user) {
            checkDuplication(user);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return userRepo.save(user);
        }

        public List<User> createUsers(List<User> users) {
            for (User user : users) {
                checkDuplication(user);
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            return userRepo.saveAll(users);
        }
    }
