package com.soa_rest.config;

import java.io.IOException;

import org.apache.tomcat.util.http.parser.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soa_rest.helpers.response.ApiResponse;
import com.soa_rest.models.repos.UserRepo;

import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userUsername;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);
        try {
            userUsername = jwtService.extractUsername(jwt);

        } catch (Exception e) {
            String[] message = { "User not authenticated" };
            ApiResponse apiResponse = new ApiResponse(false, message);
            response.setContentType("application/json");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(new ObjectMapper().writeValueAsString(apiResponse));
            response.getWriter().flush();
            response.getWriter().close();
            return;
        }
        // get usr id based on username by using findByUsername method
        if (userUsername != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userUsername);
            request.setAttribute("userUsername", userUsername);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                String[] message = { "User not authenticated" };
                ApiResponse apiResponse = new ApiResponse(false, message);
                response.setContentType("application/json");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write(new ObjectMapper().writeValueAsString(apiResponse));
                response.getWriter().flush();
                response.getWriter().close();
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

}
