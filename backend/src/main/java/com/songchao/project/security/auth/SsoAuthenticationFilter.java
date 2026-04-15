package com.songchao.project.security.auth;

import com.songchao.project.security.sso.PortalSsoClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SsoAuthenticationFilter extends OncePerRequestFilter {
    private final PortalSsoClient portalSsoClient;
    private final TokenService tokenService;

    public SsoAuthenticationFilter(PortalSsoClient portalSsoClient, TokenService tokenService) {
        this.portalSsoClient = portalSsoClient;
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            Long userId = tokenService.parseUserId(authHeader.substring(7));
            if (userId != null) {
                Object cached = request.getSession().getAttribute("CURRENT_USER");
                if (cached instanceof CurrentUser currentUser && currentUser.userId().equals(userId)) {
                    AuthContext.set(currentUser);
                }
            }
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            AuthContext.clear();
        }
    }
}

