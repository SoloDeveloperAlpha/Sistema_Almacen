package com.almacen.stock_flow;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, UsuarioService usuarioService) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
            (request, response, exception) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registro").permitAll()
            .requestMatchers("/api/usuarios/**").hasRole(Rol.ADMINISTRADOR.name())
            .requestMatchers("/api/**").authenticated()
            .anyRequest().denyAll())
        .addFilterBefore(new BearerTokenAuthenticationFilter(usuarioService),
            UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(
      @Value("${app.cors.allowed-origins:http://localhost:4200}") String allowedOrigins) {
    CorsConfiguration configuration = new CorsConfiguration();
    List<String> origins = new ArrayList<>();
    for (String origin : allowedOrigins.split(",")) {
      if (origin != null) {
        String trimmedOrigin = origin.trim();
        if (!trimmedOrigin.isEmpty()) {
          origins.add(trimmedOrigin);
        }
      }
    }
    configuration.setAllowedOrigins(origins);
    configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Content-Type", "Authorization"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
  }
}
