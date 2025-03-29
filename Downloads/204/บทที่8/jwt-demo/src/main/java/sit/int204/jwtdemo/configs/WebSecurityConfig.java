package sit.int204.jwtdemo.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sit.int204.jwtdemo.exceptions.JwtAuthenticationEntryPoint;
import sit.int204.jwtdemo.filters.JwtAuthFilter;
import sit.int204.jwtdemo.services.JwtUserDetailsService;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class WebSecurityConfig {
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;
    @Autowired
    private JwtAuthFilter jwtAuthFilter;
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {
        http.headers(httpSecurityHeadersConfigurer ->
                httpSecurityHeadersConfigurer
                        .frameOptions(frameOptionsConfig ->
                                frameOptionsConfig.disable()));

        http.csrf(crsf -> crsf.disable())
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/authentications/**", "/h2/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/groups").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users")
                        .hasAnyAuthority("MANAGER", "STAFF")
                        .requestMatchers("/api/resources/**").not().hasAuthority("GUEST")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider(jwtUserDetailsService))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
//                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            JwtUserDetailsService jwtUserDetailsService) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(jwtUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
}