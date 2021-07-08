package com.samsungds.mem.oauth2.openid.model;

import java.util.Objects;

public class Tokens {

    private final String accessToken;
    private final String idToken;
    private final String refreshToken;
    private final String tokenType;
    private final long expiresIn;

    public Tokens(String accessToken, String idToken, String refreshToken, String tokenType, long expiresIn) {
        this.accessToken = accessToken;
        this.idToken = idToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tokens tokens = (Tokens) o;
        return expiresIn == tokens.expiresIn &&
                Objects.equals(accessToken, tokens.accessToken) &&
                Objects.equals(idToken, tokens.idToken) &&
                Objects.equals(refreshToken, tokens.refreshToken) &&
                Objects.equals(tokenType, tokens.tokenType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, idToken, refreshToken, tokenType, expiresIn);
    }
}
