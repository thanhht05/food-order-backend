package com.thanh.foodorder.core.configuration;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import com.thanh.foodorder.core.util.JwtUtil;
import com.thanh.foodorder.feature.user.domain.User;
import com.thanh.foodorder.feature.user.service.UserService;

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

    /**
     * Request attribute containing authentication error code (AuthErrorCode) for
     * JwtAuthenticationEntryPoint.
     */
    public static final String AUTH_ERROR_ATTR = "AUTH_ERROR_CODE";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        boolean skip = path.equals("/api/v1/confirm-webhook");

        return path.equals("/api/v1/confirm-webhook") || path.equals(
                "/api/v1/payos_transfer_handler");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        String token = null;
        String username = null;

        try {
            // 1. Extract token from Authorization header for standard HTTP requests
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
            // 2. If not in header, try query parameter for WebSocket handshake
            // /ws/info?token=...)
            else if (request.getParameter("token") != null) {
                token = request.getParameter("token");
            }
            // Fallback query parameter if passed via access_token
            else if (request.getParameter("access_token") != null) {
                token = request.getParameter("access_token");
            }

            // 3. Extract username once token is retrieved
            if (token != null) {
                username = jwtUtil.extractUsername(token); // if token invalid throws exception
            }

            // 4. Validate token and set SecurityContext
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                User user = userService.getUserByEmail(username);

                boolean versionOk = jwtUtil.validateToken(token, username, user.getTokenVersion());
                boolean sessionOk = jwtUtil.isSessionActive(token, user.getActiveSessionId());
                if (versionOk && sessionOk) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    // Do not block requests here so permitAll endpoints continue to work.
                    // JwtAuthenticationEntryPoint reads error code when endpoint requires
                    // authentication.
                    request.setAttribute(AUTH_ERROR_ATTR,
                            !versionOk ? AuthErrorCode.PASSWORD_CHANGED : AuthErrorCode.LOGGED_IN_ELSEWHERE);
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