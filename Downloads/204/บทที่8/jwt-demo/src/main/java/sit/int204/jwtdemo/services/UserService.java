package sit.int204.jwtdemo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int204.jwtdemo.dtos.AccessToken;
import sit.int204.jwtdemo.dtos.JwtRequestUser;
import sit.int204.jwtdemo.entities.User;
import sit.int204.jwtdemo.repositories.UserRepository;
import sit.int204.jwtdemo.utils.JwtUtils;
import sit.int204.jwtdemo.utils.TokenType;

import java.util.List;
import java.util.Map;

@Service
public class UserService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepo;
    private Argon2PasswordEncoder passwordEncoder =
            new Argon2PasswordEncoder(
                    16, 16,
                    8, 1024 * 128, 2);

    public User findUserById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    private void checkDuplication(User user) {
        if (userRepo.existsUserByUsernameOrEmail(
                user.getUsername(), user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST
                    , "User name or Email already exist !!! ("
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

    public Map<String, Object> authenticateUser(JwtRequestUser user) {
        UsernamePasswordAuthenticationToken upat = new
                UsernamePasswordAuthenticationToken(
                user.getUsername(), user.getPassword());
        authenticationManager.authenticate(upat);
        //Exception occurred (401) if failed
        UserDetails userDetails = jwtUserDetailsService
                .loadUserByUsername(user.getUsername());
        long refreshTokenAgeInMinute = 8 * 60 * 60 * 1000;
        return Map.of(
                "access_token", jwtUtils.generateToken(userDetails)
                , "refresh_token", jwtUtils.generateToken(
                        userDetails, refreshTokenAgeInMinute, TokenType.REFRESH_TOKEN)
        );
    }

    public Map<String, Object> refreshToken(String refreshToken) {
        jwtUtils.verifyToken(refreshToken);
        Map<String, Object> claims = jwtUtils.getJWTClaimsSet(refreshToken);
        jwtUtils.isExpired(claims);
        if (!jwtUtils.isValidClaims(claims) || !"REFRESH_TOKEN".equals(claims.get("typ"))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        UserDetails userDetails = jwtUserDetailsService.loadUserById((Long) claims.get("uid"));
        return Map.of("access_token", jwtUtils.generateToken(userDetails));
    }

}