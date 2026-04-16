package com.songchao.project.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
public class TokenService {
    private final ObjectMapper objectMapper;

    @Value("${app.jwt-secret}")
    private String secret;

    @Value("${app.token-expire-seconds}")
    private long expireSeconds;

    public TokenService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String createToken(CurrentUser user) {
        try {
            TokenPayload payload = new TokenPayload(
                    user.userId(),
                    user.username(),
                    user.realName(),
                    user.deptId(),
                    user.deptName(),
                    user.roleCodes(),
                    user.permissionCodes(),
                    user.dataScope(),
                    Instant.now().getEpochSecond() + expireSeconds
            );
            String encodedPayload = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(payload));
            String sign = hmacSha256(encodedPayload, secret);
            return encodedPayload + "." + sign;
        } catch (Exception ex) {
            throw new IllegalStateException("failed to create token", ex);
        }
    }

    public Long parseUserId(String token) {
        CurrentUser currentUser = parseCurrentUser(token);
        if (currentUser != null) {
            return currentUser.userId();
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|");
            if (parts.length != 4) {
                return null;
            }
            String payload = parts[0] + "|" + parts[1] + "|" + parts[2];
            if (!hmacSha256(payload, secret).equals(parts[3])) {
                return null;
            }
            long exp = Long.parseLong(parts[2]);
            if (Instant.now().getEpochSecond() > exp) {
                return null;
            }
            return Long.parseLong(parts[0]);
        } catch (Exception ex) {
            return null;
        }
    }

    public CurrentUser parseCurrentUser(String token) {
        try {
            String[] parts = token.split("\\.", 2);
            if (parts.length != 2) {
                return null;
            }
            String encodedPayload = parts[0];
            if (!hmacSha256(encodedPayload, secret).equals(parts[1])) {
                return null;
            }
            TokenPayload payload = objectMapper.readValue(
                    Base64.getUrlDecoder().decode(encodedPayload),
                    TokenPayload.class
            );
            if (payload.exp() == null || Instant.now().getEpochSecond() > payload.exp()) {
                return null;
            }
            return new CurrentUser(
                    payload.userId(),
                    payload.username(),
                    payload.realName(),
                    payload.deptId(),
                    payload.deptName(),
                    payload.roleCodes() == null ? List.of() : payload.roleCodes(),
                    payload.permissionCodes() == null ? List.of() : payload.permissionCodes(),
                    payload.dataScope() == null ? 1 : payload.dataScope()
            );
        } catch (Exception ex) {
            return null;
        }
    }

    private String hmacSha256(String input, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (Exception ex) {
            throw new IllegalStateException("failed to sign token", ex);
        }
    }

    private record TokenPayload(
            Long userId,
            String username,
            String realName,
            Long deptId,
            String deptName,
            List<String> roleCodes,
            List<String> permissionCodes,
            Integer dataScope,
            Long exp
    ) {
    }
}
