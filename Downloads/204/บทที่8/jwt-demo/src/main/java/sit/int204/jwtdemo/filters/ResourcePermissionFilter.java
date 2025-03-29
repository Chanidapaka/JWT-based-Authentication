package sit.int204.jwtdemo.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import sit.int204.jwtdemo.entities.AuthUserDetail;

import java.io.IOException;
import java.security.Principal;

@Component
@Order(1)
public class ResourcePermissionFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse
            , FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        if (request.getRequestURI().matches("/api/resources/\\S+")) {
            Principal principal = request.getUserPrincipal();
            AuthUserDetail user = principal == null ? null :
                    (AuthUserDetail) ((UsernamePasswordAuthenticationToken) principal)
                            .getPrincipal();
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED
                        , "You do not have permission to access this resource");
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
