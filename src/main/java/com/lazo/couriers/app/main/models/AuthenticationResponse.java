package com.lazo.couriers.app.main.models;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class AuthenticationResponse implements Serializable {

    private final String jwt;
    private final Long expiresIn;
    private final String refreshToken;
    private final Long refreshExpiresIn;

    public AuthenticationResponse(String jwt, Long expiresIn, String refreshToken, Long refreshExpiresIn) {
        this.jwt = jwt;
        this.expiresIn = expiresIn - 5000;
        this.refreshToken = refreshToken;
        this.refreshExpiresIn = refreshExpiresIn;
    }
}
