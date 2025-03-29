package sit.int204.jwtdemo.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AccessToken {
    @JsonIgnore
    private final String token;
    public AccessToken(String token) {
        this.token = token;
    }

    public String getAccess_token() {
        return token;
    }
}
