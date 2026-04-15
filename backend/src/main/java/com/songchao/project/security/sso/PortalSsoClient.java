package com.songchao.project.security.sso;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class PortalSsoClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public PortalSsoClient(PortalSsoProperties properties, ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().baseUrl(properties.getSsoBaseUrl()).build();
        this.objectMapper = objectMapper;
    }

    public PortalExchangeResult exchangeTicket(String ticket) {
        JsonNode root = restClient.post()
                .uri("/sso/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TicketRequest(ticket))
                .retrieve()
                .body(JsonNode.class);
        JsonNode data = root.path("data");
        return new PortalExchangeResult(
                data.path("token").asText(),
                data.path("refreshToken").asText(),
                data.path("user")
        );
    }

    public PortalValidateResult validateToken(String token) {
        JsonNode root = restClient.post()
                .uri("/sso/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TokenRequest(token))
                .retrieve()
                .body(JsonNode.class);
        JsonNode user = root.path("data").path("user");
        List<String> roleCodes = new ArrayList<>();
        user.path("roles").forEach(node -> roleCodes.add(node.path("code").asText()));
        List<String> permissionCodes = new ArrayList<>();
        user.path("permissions").forEach(node -> permissionCodes.add(node.asText()));
        return new PortalValidateResult(
                user.path("id").asLong(),
                user.path("username").asText(),
                user.path("realName").asText(),
                user.path("deptId").isMissingNode() ? null : user.path("deptId").asLong(),
                roleCodes,
                permissionCodes,
                user.path("dataScope").asInt(1)
        );
    }

    private record TicketRequest(String ticket) {
    }

    private record TokenRequest(String token) {
    }

    public record PortalExchangeResult(String token, String refreshToken, JsonNode user) {
    }

    public record PortalValidateResult(
            Long userId,
            String username,
            String realName,
            Long deptId,
            List<String> roleCodes,
            List<String> permissionCodes,
            Integer dataScope
    ) {
    }
}

