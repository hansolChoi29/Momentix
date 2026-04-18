package com.example.momentix.config;

import java.util.List;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.example.momentix.domain.common.filter.JwtAuthorizationFilter;
import lombok.RequiredArgsConstructor;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthorizationFilter jwtAuthorizationFilter,
            AuthenticationProvider authenticationProvider
    ) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/**"
                        ).permitAll()
                        .requestMatchers(
                                "/auth/sign-in/**",
                                "/auth/sign-in/callback/**"
                        ).permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // host 공연등록 가능
                        .requestMatchers(HttpMethod.POST, "/events/*/*/seats").hasRole("HOST")
                        .requestMatchers(HttpMethod.PATCH, "/events/*/*/seats").hasRole("HOST")
                        .requestMatchers(HttpMethod.DELETE, "/events/*/*/seats").hasRole("HOST")
                        .requestMatchers(HttpMethod.PUT, "/events/**").hasRole("HOST")
                        // websocket 모두 허용
                        .requestMatchers("/ws/**").permitAll()
                        // 대기열 모두 허용
                        .requestMatchers("/queue/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/reviews/**").hasAnyRole("CONSUMER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/events/**").hasRole("ADMIN")
                        // ADMIN 공연삭제
                        .requestMatchers(HttpMethod.DELETE, "/tickets/**").hasRole("ADMIN")
                        // 알림
                        .requestMatchers(HttpMethod.POST, "/notifications/broadcast").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/notifications/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/notifications/favorites/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/notifications/events/**").hasAnyRole("ADMIN",
                                "HOST")
                        // 테스트용 api 주소
                        .requestMatchers("/error").permitAll()
                        // 예약은 CONSUMER만
                        .requestMatchers("/reservations/**").hasRole("CONSUMER")
                        // 결제는 CONSUMER만
                        .requestMatchers("/payment/**").hasRole("CONSUMER")
                        // 특정 조건을 지정하지 않은 나머지 모든 API는 로그인만 했으면 호출 가능함
                        .requestMatchers(HttpMethod.GET, "/events/**").permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // SignInService 에서 주입받아 쓰고 있으므로 제공 필요
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    //DB기반 로그인 처리
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                         PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
