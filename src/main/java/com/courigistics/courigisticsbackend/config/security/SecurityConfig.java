package com.courigistics.courigisticsbackend.config.security;

import com.courigistics.courigisticsbackend.config.SecurityHeadersConfig;
import com.courigistics.courigisticsbackend.entities.enums.AccountType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configures Spring Security: which routes are public, which require roles, where JwtAuthFilter sits in the filter chain
 * password encoders
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final SecurityHeadersConfig securityHeadersConfig;
    private final Environment environment;
    private final CorsConfigurationSource corsConfigurationSource;

    // public endpoints
    private static final String[] PUBLIC_ENDPONTS ={
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/verify",
            "/api/v1/auth/forgot-password", // for password reset request
            "/api/v1/auth/reset-password" // for password reset success
    };

    // Dev test endpoints
    private static final String[] DEV_TEST_ENDPOINTS = {
            "/api/v1/auth/health",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/h2-console/**",
            "/actuator/**"
    };


    @Bean // So spring can manage this as a bean
    /*
     * returns Authentication provider, which is a code Spring security interface responsible for handling a specific type of authentication
     *
     */
    public AuthenticationProvider authenticationProvider (){
        // Connect the auth provider to our custom logic for finding users
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder()); // password hashing algorithm to use (BCrypt)
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws  Exception{
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    /*
     * This method configures the SecurityFilterChain, which is is a sequence of filters that Spring security applies to
     * incoming HTTP requests.
     * It defines which endpoints are public, which are protected, what roles are needed to access them and how auth is
     * performed
     */
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        // create a list of endpoints that should always be public
        List<String> permList = new ArrayList<>(List.of(PUBLIC_ENDPONTS));
        // Check active Spring profile (if dev or test env)
        boolean isDevOrTest = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(p -> p.equalsIgnoreCase("dev") || p.equalsIgnoreCase("test"));

        // If in dev or test, add development specific endpoints to the public list
        if (isDevOrTest){
            permList.addAll(List.of(DEV_TEST_ENDPOINTS));
        }

        // our application uses stateless JWT, we turn off CSRF
        http
                .csrf(csrf -> csrf.disable()).cors(cors -> {
                    if (isDevOrTest){
                        cors.configurationSource(request -> {
                            if (request.getRequestURI().startsWith("/h2-console")){
                                return new CorsConfiguration().applyPermitDefaultValues();
                            }
                            return corsConfigurationSource.getCorsConfiguration(request);
                        });
                    } else {
                        cors.configurationSource(corsConfigurationSource);
                    }
                }).authorizeHttpRequests(auth -> auth
                        .requestMatchers(permList.toArray(new String[0])).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole(AccountType.ADMIN.toString())
                        .requestMatchers("/api/v1/customer/**").hasRole(AccountType.CUSTOMER.toString())
                        .requestMatchers("/api/v1/courier/**").hasRole(AccountType.COURIER.toString())
                        .requestMatchers("/api/v1/depo-admin/**").hasRole(AccountType.DEPOT_ADMIN.toString())
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(securityHeadersConfig, UsernamePasswordAuthenticationFilter.class);

        if (isDevOrTest){
            http.headers(headers -> headers
                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                    .httpStrictTransportSecurity(HeadersConfigurer.HstsConfig::disable)
            );
        }

        return http.build();
    }

}
