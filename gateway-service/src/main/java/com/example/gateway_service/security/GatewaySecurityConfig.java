package com.example.gateway_service.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;

import java.util.*;

@Configuration
public class GatewaySecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/eureka/**").permitAll()

                        // Appointments
                        .requestMatchers(HttpMethod.POST, "/api/appointments/**")
                        .hasAnyRole("APPT_ADMIN", "ADMIN", "PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/appointments/**")
                        .hasAnyRole("APPT_READ", "ADMIN", "DOCTOR", "NURSE", "PATIENT")

                        // Prescriptions
                        .requestMatchers(HttpMethod.POST, "/api/prescriptions/**")
                        .hasAnyRole("PRESC_WRITE", "ADMIN", "DOCTOR")
                        .requestMatchers( "/api/records/**")
                        .hasAnyRole( "PRESC_READ","PRESC_WRITE","PRESC_ADMIN","ADMIN", "DOCTOR", "NURSE", "PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/prescriptions/**")
                        .hasAnyRole("PRESC_READ", "ADMIN", "DOCTOR", "PATIENT")

                        // Payments
                        .requestMatchers(HttpMethod.POST, "/api/payments/**")
                        .hasAnyRole("PAYMENT_MAKE",  "PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/payments/**")
                        .hasAnyRole("PAYMENT_READ", "ADMIN", "PATIENT")

                        // User Management (admin only)
                        .requestMatchers("/api/rooms/**", "/api/departments/**")
                        .hasAnyRole("USER_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE,"/api/users/**")
                        .hasAnyRole("USER_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.POST,"/api/users/staff")
                        .hasAnyRole("USER_ADMIN", "ADMIN")


                        // Notifications
                        .requestMatchers(HttpMethod.POST, "/api/notifications/**")
                        .hasAnyRole("NOTIF_CREATE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/notifications/**")
                        .hasAnyRole("NOTIF_RECEIVE", "ADMIN", "DOCTOR", "NURSE", "PATIENT")

                        // any other endpoint → must be authenticated
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));

        return http.build();
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // resource_access (client roles)
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        /*
        "gateway-client": {
      "roles": [
        "ADMIN"
      ]
    },
    "product-service": {
      "roles": [
        "PRODUCT_VIEW",
        "PRODUCT_EDIT"
      ]
    }
    map<String(ClientID),map<String("roles"),List<String>>>
         */
        if (resourceAccess != null) {
            for (Object value : resourceAccess.values()) {
                //value = map<String("roles"),List<String>>
                if (value instanceof Map<?, ?> map) {
                    Object rolesObj = map.get("roles");
                    //rolesObj = List<String>>
                    if (rolesObj instanceof Collection<?> roles) {
                        for (Object role : roles) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                        }
                    }
                }
            }
        }

        // realm_access (realm roles)
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            Object rolesObj = realmAccess.get("roles");
            if (rolesObj instanceof Collection<?> roles) {
                for (Object role : roles) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }
        }

        return authorities;
    }
}
