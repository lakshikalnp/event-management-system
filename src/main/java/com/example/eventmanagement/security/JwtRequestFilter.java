package com.example.eventmanagement.security;

import com.example.eventmanagement.entity.User;
import com.example.eventmanagement.exception.HttpStatusEntryPoint;
import com.example.eventmanagement.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@AllArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

     private JwtUtil jwtUtil;
     private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException{
        try {
            final String authHeader = request.getHeader("Authorization");

            String userId;
            String jwt;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);

                if (jwtUtil.validateToken(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    userId = jwtUtil.extractUserId(jwt);
                    User user = userService.getUserById(UUID.fromString(userId));
                    String roleName = "ROLE_"+user.getRole().name();
                    List<GrantedAuthority> grantedAuthorities = List.of(new SimpleGrantedAuthority(roleName));
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(user, null, grantedAuthorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (AuthenticationException ex) {
            // Save exception for later retrieval in AuthenticationEntryPoint
            request.setAttribute("auth_exception", ex);
            SecurityContextHolder.clearContext();
            // Now delegate to AuthenticationEntryPoint
            AuthenticationEntryPoint entryPoint = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
            entryPoint.commence(request, response, ex);
            return;
        }


        chain.doFilter(request, response);
    }
}
