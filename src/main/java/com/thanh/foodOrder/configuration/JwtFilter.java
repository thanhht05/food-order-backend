package com.thanh.foodorder.configuration;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.thanh.foodorder.domain.User;
import com.thanh.foodorder.service.UserService;
import com.thanh.foodorder.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JwtFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public JwtFilter(UserDetailsService userDetailsService, JwtUtil jwtUtil, UserService userService) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        boolean skip = path.equals("/api/v1/confirm-webhook");

        System.out.println("URI = " + request.getRequestURI());
        System.out.println("SERVLET PATH = " + path);
        System.out.println("SKIP JWT = " + skip);

        return path.equals("/api/v1/confirm-webhook") || path.equals(
                "/api/v1/payos_transfer_handler");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        String token = null;
        String username = null;
        System.out.println("URI = " + request.getRequestURI());
        System.out.println("SERVLET PATH = " + request.getServletPath());

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
                User user = userService.getUserByEmail(username);

                boolean isVailid = jwtUtil.validateToken(token, username, user.getTokenVersion());
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