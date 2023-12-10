package com.lazo.couriers.utils;

import com.lazo.couriers.app.main.models.AuthenticationResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtils {

    @Value("${co.module.secret_key}")
    private String SECRET_KEY;

    public static Long EXPIRES_IN_MILLIS = 300_000L;
    public static Long REFRESH_EXPIRES_IN_MILLIS = 60000*60*24L;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Boolean extractAccessTokenStatus(String token) {
        final Claims claims = extractAllClaims(token);
        return claims.get("access_token", Boolean.class);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(SECRET_KEY.getBytes()).build().parseClaimsJws(token).getBody();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public AuthenticationResponse generateToken(UserDetails userDetails) {
        Map<String, Object> accessTokenClaims = new HashMap<>();
        accessTokenClaims.put("access_token", true);

        Map<String, Object> refreshTokenClaims = new HashMap<>();
        refreshTokenClaims.put("access_token", false);

        return new AuthenticationResponse(createToken(accessTokenClaims, userDetails.getUsername()), System.currentTimeMillis() + EXPIRES_IN_MILLIS, createRefreshToken(refreshTokenClaims, userDetails.getUsername()), System.currentTimeMillis() + REFRESH_EXPIRES_IN_MILLIS);
    }

    private String createToken(Map<String, Object> claims, String subject) {

        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRES_IN_MILLIS))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS512).compact();

    }

    private String createRefreshToken(Map<String, Object> claims, String subject) {

        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRES_IN_MILLIS))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS512).compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        if (userDetails == null)
            return false;
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String getUserNameViaToken(String token) {

        if (StringUtils.isEmpty(token))
            return null;

        var userName = extractUsername(token.substring(7));

        if (StringUtils.isEmpty(userName))
            return null;

        return userName;
    }

}
