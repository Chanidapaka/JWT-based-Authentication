package sit.int204.jwtdemo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sit.int204.jwtdemo.entities.User;
import sit.int204.jwtdemo.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/groups")
    public ResponseEntity<List<User>> createUsers(
            @RequestBody List<User> users) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUsers(users));
    }

    @PostMapping("")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(user));
    }
}
