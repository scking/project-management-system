package com.songchao.project.modules.auth.controller;

import com.songchao.project.common.api.ApiResponse;
import com.songchao.project.common.exception.BizException;
import com.songchao.project.modules.auth.dto.SsoLoginRequest;
import com.songchao.project.modules.auth.vo.AuthUserVO;
import com.songchao.project.security.auth.AuthContext;
import com.songchao.project.security.auth.CurrentUser;
import com.songchao.project.security.auth.TokenService;
import com.songchao.project.security.sso.PortalSsoClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final PortalSsoClient portalSsoClient;
    private final TokenService tokenService;

    public AuthController(PortalSsoClient portalSsoClient, TokenService tokenService) {
        this.portalSsoClient = portalSsoClient;
        this.tokenService = tokenService;
    }

    @PostMapping("/sso-login")
    public ApiResponse<AuthUserVO> ssoLogin(@Valid @RequestBody SsoLoginRequest request, HttpServletRequest httpRequest) {
        PortalSsoClient.PortalExchangeResult exchangeResult;
        PortalSsoClient.PortalValidateResult validated;
        try {
            exchangeResult = portalSsoClient.exchangeTicket(request.getTicket());
            validated = portalSsoClient.validateToken(exchangeResult.token());
        } catch (Exception exception) {
            throw new BizException("ticket 无效或已使用", 401);
        }
        CurrentUser currentUser = new CurrentUser(
                validated.userId(),
                validated.username(),
                validated.realName(),
                validated.deptId(),
                validated.deptName(),
                validated.roleCodes(),
                validated.permissionCodes(),
                validated.dataScope()
        );
        String accessToken = tokenService.createToken(currentUser);
        httpRequest.getSession(true).setAttribute("CURRENT_USER", currentUser);
        return ApiResponse.ok(new AuthUserVO(
                currentUser.userId(),
                currentUser.username(),
                currentUser.realName(),
                currentUser.deptId(),
                currentUser.deptName(),
                currentUser.roleCodes(),
                currentUser.permissionCodes(),
                currentUser.dataScope(),
                accessToken
        ));
    }

    @GetMapping("/current-user")
    public ApiResponse<AuthUserVO> currentUser(HttpServletRequest request) {
        Object userObject = request.getSession().getAttribute("CURRENT_USER");
        CurrentUser currentUser;
        if (userObject instanceof CurrentUser sessionUser) {
            currentUser = sessionUser;
        } else {
            currentUser = AuthContext.get();
        }
        if (currentUser == null) {
            throw new BizException("NOT_AUTHENTICATED", 401);
        }
        return ApiResponse.ok(new AuthUserVO(
                currentUser.userId(),
                currentUser.username(),
                currentUser.realName(),
                currentUser.deptId(),
                currentUser.deptName(),
                currentUser.roleCodes(),
                currentUser.permissionCodes(),
                currentUser.dataScope(),
                null
        ));
    }
}
