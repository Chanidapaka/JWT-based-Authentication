package sit.int204.jwtdemo.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;
import sit.int204.jwtdemo.services.JwtUserDetailsService;
import sit.int204.jwtdemo.utils.JwtUtils;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request
            , HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        response.setHeader("request-uri", request.getRequestURI());
        final String requestTokenHeader = request.getHeader("Authorization");
        Long userId = null;
        String jwtToken = null;
        Map<String, Object> claims = null;
        if (requestTokenHeader != null) {
            if (requestTokenHeader.startsWith("Bearer ")) {
                jwtToken = requestTokenHeader.substring(7);
                jwtUtils.verifyToken(jwtToken);
                claims = jwtUtils.getJWTClaimsSet(jwtToken);
                if (jwtUtils.isExpired(claims)) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED
                            , "JWT token has expired");
                }
                if (!jwtUtils.isValidClaims(claims) || !"ACCESS_TOKEN".equals(claims.get("typ"))) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED
                            , "Invalid JWT access token");
                }
                userId = (Long) claims.get("uid");
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED
                        , "JWT Token does not begin with Bearer String");
            }
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (userId != null && authentication == null) {
            UserDetails userDetails = this.jwtUserDetailsService.loadUserById(userId);
            if (userDetails == null || !userDetails.getUsername().equals(claims.get("sub"))) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED
                        , "Invalid JWT Token");
            }
            UsernamePasswordAuthenticationToken upAuthToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
            upAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(upAuthToken);
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }
        chain.doFilter(request, response);
    }
}