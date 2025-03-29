package sit.int204.jwtdemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sit.int204.jwtdemo.entities.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsUserByUsernameOrEmail(String username, String email);

    @Query(value = "select u from User u where u.username =:usernameOrEmail or u.email =:usernameOrEmail")
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);
}
