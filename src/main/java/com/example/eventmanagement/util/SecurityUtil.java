package com.example.eventmanagement.util;

import com.example.eventmanagement.entity.User;
import com.example.eventmanagement.exception.UnAuthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    private SecurityUtil () {}

    public static User getAuthUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user;
        }
        throw new UnAuthorizedException("No authenticated user found");
    }

}
