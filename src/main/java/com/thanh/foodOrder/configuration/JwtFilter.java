package com.thanh.foodOrder.configuration;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.thanh.foodOrder.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JwtFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public JwtFilter(UserDetailsService userDetailsService, JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        String token = null;
        String username = null;

        try {
            // 1. Ưu tiên lấy token từ Header (cho các request HTTP thông thường)
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
            // 2. Nếu không có ở Header, thử lấy từ Parameter (cho WebSocket
            // /ws/info?token=...)
            else if (request.getParameter("token") != null) {
                token = request.getParameter("token");
            }
            // (Tùy chọn) Dự phòng nếu truyền bằng access_token
            else if (request.getParameter("access_token") != null) {
                token = request.getParameter("access_token");
            }

            // 3. Nếu lấy được token, tiến hành extract username
            if (token != null) {
                username = jwtUtil.extractUsername(token); // if token invalid throws exception
            }

            // 4. Validate và set SecurityContext
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                boolean isVailid = jwtUtil.validateToken(token, username);
                if (isVailid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        } catch (Exception e) { // catch error
            logger.warn("Token is invalid: " + e.getMessage());
            // set authentication null for spring security call JwtAuthenticationEntryPoint
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}