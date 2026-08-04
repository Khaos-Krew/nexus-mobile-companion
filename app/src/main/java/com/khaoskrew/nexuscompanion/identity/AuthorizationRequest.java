package com.khaoskrew.nexuscompanion.identity;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Pure contract for an OAuth authorization-code request with PKCE. */
public final class AuthorizationRequest {
    private final URI authorizationEndpoint;
    private final String clientId;
    private final URI redirectUri;
    private final String scope;
    private final AuthorizationAttempt attempt;

    public AuthorizationRequest(
        URI authorizationEndpoint,
        String clientId,
        URI redirectUri,
        String scope,
        AuthorizationAttempt attempt
    ) {
        this.authorizationEndpoint = requireHttpsEndpoint(authorizationEndpoint);
        this.clientId = requireNonBlank(clientId, "clientId");
        this.redirectUri = Objects.requireNonNull(redirectUri, "redirectUri");
        this.scope = requireNonBlank(scope, "scope");
        this.attempt = Objects.requireNonNull(attempt, "attempt");
        if (redirectUri.getScheme() == null || redirectUri.getScheme().trim().isEmpty()) {
            throw new IllegalArgumentException("redirectUri must have a scheme");
        }
    }

    public URI toUri() {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("response_type", "code");
        parameters.put("client_id", clientId);
        parameters.put("redirect_uri", redirectUri.toString());
        parameters.put("scope", scope);
        parameters.put("state", attempt.state());
        parameters.put("code_challenge", attempt.challenge());
        parameters.put("code_challenge_method", "S256");

        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (query.length() > 0) {
                query.append('&');
            }
            query.append(encode(entry.getKey()))
                .append('=')
                .append(encode(entry.getValue()));
        }
        String separator = authorizationEndpoint.getQuery() == null ? "?" : "&";
        return URI.create(authorizationEndpoint + separator + query);
    }

    public AuthorizationAttempt attempt() {
        return attempt;
    }

    public URI redirectUri() {
        return redirectUri;
    }

    private static URI requireHttpsEndpoint(URI endpoint) {
        Objects.requireNonNull(endpoint, "authorizationEndpoint");
        if (!"https".equalsIgnoreCase(endpoint.getScheme())
            || endpoint.getHost() == null
            || endpoint.getUserInfo() != null) {
            throw new IllegalArgumentException(
                "authorizationEndpoint must be certificate-valid HTTPS without embedded credentials"
            );
        }
        return endpoint;
    }

    private static String requireNonBlank(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return value;
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name())
                .replace("+", "%20");
        } catch (UnsupportedEncodingException impossible) {
            throw new IllegalStateException("UTF-8 is unavailable", impossible);
        }
    }

    @Override
    public String toString() {
        return "AuthorizationRequest{authorizationEndpoint="
            + authorizationEndpoint.getScheme()
            + "://"
            + authorizationEndpoint.getHost()
            + ", redirectScheme="
            + redirectUri.getScheme()
            + "}";
    }
}
