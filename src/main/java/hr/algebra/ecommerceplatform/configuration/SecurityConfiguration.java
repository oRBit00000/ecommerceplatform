package hr.algebra.ecommerceplatform.configuration;

import hr.algebra.ecommerceplatform.filter.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final RequestMatcher REST_API_MATCHER = request -> request.getServletPath().startsWith("/rest/");

    private final JwtAuthFilter jwtAuthFilter;
    private final MvcAuthenticationSuccessHandler mvcAuthenticationSuccessHandler;
    private final MvcLogoutHandler mvcLogoutHandler;

    public SecurityConfiguration(JwtAuthFilter jwtAuthFilter,
                                 MvcAuthenticationSuccessHandler mvcAuthenticationSuccessHandler,
                                 MvcLogoutHandler mvcLogoutHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.mvcAuthenticationSuccessHandler = mvcAuthenticationSuccessHandler;
        this.mvcLogoutHandler = mvcLogoutHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/rest/auth/login", "/rest/auth/refresh", "/rest/auth/logout", "/mvc/products/search", "/mvc/orders/checkout", "/rest/products/all", "/rest/products/filter/**", "/rest/categories/all", "/rest/cart/**", "/mvc/cart/**").permitAll()
                        .requestMatchers("/mvc/admin/**", "/mvc/products/new", "/mvc/products/edit/**", "/mvc/categories/manage/**").hasRole(ADMIN_ROLE)
                        .requestMatchers("/rest/products/new", "/rest/products/delete/**", "/rest/categories/new", "/rest/categories/delete/**", "/rest/admin/**").hasRole(ADMIN_ROLE)
                        .requestMatchers("/mvc/orders/history", "/mvc/orders/admin", "/rest/orders/**", "/rest/paypal/**").hasAnyRole(ADMIN_ROLE, "USER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(mvcAuthenticationSuccessHandler)
                        .permitAll()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .defaultAuthenticationEntryPointFor(restAuthenticationEntryPoint(), REST_API_MATCHER)
                        .defaultAccessDeniedHandlerFor(restAccessDeniedHandler(), REST_API_MATCHER)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler(mvcLogoutHandler)
                        .logoutSuccessUrl("/login?logout")
                )
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return (request, response, authException) -> writeJsonError(
                response,
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "Authentication is required to access this endpoint."
        );
    }

    private AccessDeniedHandler restAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> writeJsonError(
                response,
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "You do not have permission to access this endpoint."
        );
    }

    private void writeJsonError(jakarta.servlet.http.HttpServletResponse response,
                                HttpStatus status,
                                String error,
                                String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        new ObjectMapper().writeValue(response.getWriter(), Map.of(
                "status", status.value(),
                "error", error,
                "message", message
        ));
    }
}
