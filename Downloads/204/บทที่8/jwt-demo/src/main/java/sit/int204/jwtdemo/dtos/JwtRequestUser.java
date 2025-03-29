package sit.int204.jwtdemo.dtos;

import lombok.Data;

@Data
public class JwtRequestUser {
    private String username;
    private String password;
}
