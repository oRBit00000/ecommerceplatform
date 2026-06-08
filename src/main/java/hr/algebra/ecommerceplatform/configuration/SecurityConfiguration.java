package hr.algebra.ecommerceplatform.configuration;

import hr.algebra.ecommerceplatform.filter.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final String ADMIN_ROLE = "ADMIN";

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
}
