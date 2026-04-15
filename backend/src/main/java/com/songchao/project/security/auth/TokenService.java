package com.songchao.project.security.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class TokenService {
    @Value("${app.jwt-secret}")
    private String secret;

    @Value("${app.token-expire-seconds}")
    private long expireSeconds;

    public String createToken(CurrentUser user) {
        long exp = Instant.now().getEpochSecond() + expireSeconds;
        String payload = user.userId() + "|" + user.username() + "|" + exp;
        String sign = hmacSha256(payload, secret);
        return Base64.getUrlEncoder().withoutPadding().encodeToString((payload + "|" + sign).getBytes(StandardCharsets.UTF_8));
    }

    public Long parseUserId(String token) {
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
}

