package com.melnikov.TicketBookingService.configuration;

import com.melnikov.TicketBookingService.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Разрешить preflight-запросы для всех
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Эндпоинты авторизации – доступны без аутентификации
                        .requestMatchers("/auth/**").permitAll()
                        // Эндпоинты, где требуется токен – доступны только для аутентифицированных пользователей
                        .requestMatchers("/api/bookings/**").authenticated()
                        .requestMatchers("/deposits/**").authenticated()
                        .requestMatchers("/users/**").authenticated()
                        .requestMatchers("/bookings/**").authenticated()
                        // Для /tickets: только POST запрос на /tickets/create доступен только с ролью ADMIN
                        .requestMatchers(HttpMethod.POST, "/tickets/create").hasRole("ADMIN")
                        // Остальные эндпоинты /tickets/** (например, поиск) – оставляем открытыми или можно требовать аутентификации,
                        // если это необходимо. Здесь оставляем открытыми:
                        .requestMatchers("/tickets/**").permitAll()
                        // Запретить все прочие запросы
                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Разрешаем оба origin (localhost и 127.0.0.1)
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:5500",
                "http://127.0.0.1:5500"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Headers",
                "Authorization, x-xsrf-token, Access-Control-Allow-Headers, " +
                        "Origin, Accept, X-Requested-With, Content-Type, " +
                        "Access-Control-Request-Method, Access-Control-Request-Headers"
        ));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Хеширование паролей
    }
}



