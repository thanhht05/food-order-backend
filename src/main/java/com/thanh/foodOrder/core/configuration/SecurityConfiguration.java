package com.thanh.foodorder.core.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import com.thanh.foodorder.core.util.JwtUtil;
import com.thanh.foodorder.feature.user.service.UserService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    SecurityConfiguration(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            CustomLogoutSuccessHandler customLogoutSuccessHandler) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.customLogoutSuccessHandler = customLogoutSuccessHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public JwtFilter jwtFilter(UserDetailsService userDetailsService, JwtUtil jwtUtil, UserService userService) {
        return new JwtFilter(userDetailsService, jwtUtil, userService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. Cho phép xem sản phẩm và danh mục công khai (chỉ GET)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/categories/**").permitAll()

                        // 2. Các endpoint xác thực công khai
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/api/v1/auth/refreshToken")
                        .permitAll()

                        // 3. Webhook PayOS (server-to-server callback)
                        .requestMatchers(
                                "/api/v1/payos_transfer_handler",
                                "/api/v1/confirm-webhook")
                        .permitAll()

                        // 4. WebSocket & Swagger Docs
                        .requestMatchers(
                                "/ws/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**")
                        .permitAll()

                        // 5. Chat AI hỗ trợ khách hàng
                        .requestMatchers("/api/v1/ai/**").permitAll()

                        // 6. Mọi request khác đều phải được xác thực (phân quyền chi tiết tại
                        // Controller bằng @PreAuthorize)
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.disable())
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .logoutSuccessHandler(customLogoutSuccessHandler));

        return http.build();
    }

}
