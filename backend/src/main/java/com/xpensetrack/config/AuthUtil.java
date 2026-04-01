package com.xpensetrack.config;

import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {
    public static String currentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
