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
            CurrentUser currentUser = tokenService.parseCurrentUser(authHeader.substring(7));
            if (currentUser != null) {
                AuthContext.set(currentUser);
                request.getSession(true).setAttribute("CURRENT_USER", currentUser);
            }
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            AuthContext.clear();
        }
    }
}
