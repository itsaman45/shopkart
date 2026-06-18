package com.Shopkart.Application.security.jwt;

import com.Shopkart.Application.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private Long jwtExpirationMs;

    @Value("${spring.app.jwtCookie}")
    private String jwtCookie;

    private static Logger logger = LoggerFactory.getLogger(JwtUtils.class);

//    public String getJWTTokenFromHeader(HttpServletRequest request){
//        String bearerToken = request.getHeader("Authorization");
//        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
//            return bearerToken.substring(7);
//        }
//        return null;
//    }

    public String getJwtFromCookie(HttpServletRequest request){
        Cookie cookie = WebUtils.getCookie(request,jwtCookie);
        if(cookie != null)
            return cookie.getValue();
        else
            return null;
    }

    public ResponseCookie generateJwtCookie(UserDetailsImpl userDetails){
        String jwt = getTokenFromUsername(userDetails.getUsername());
        ResponseCookie cookie = ResponseCookie.from(jwtCookie,jwt)
                .path("/api")
                .maxAge( 24*60*60)
                .httpOnly(false)
                .build();

        return cookie;
    }

    public ResponseCookie getCleanCookie(){
        ResponseCookie cookie = ResponseCookie.from(jwtCookie,null)
                .path("/api")
                .build();
        return cookie;
    }

    public String getTokenFromUsername(String username){
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date().getTime() + jwtExpirationMs)))
                .signWith(key())
                .compact();
    }

    public String getUsernameFromJWTToken(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }

    public Key key(){
        return Keys.hmacShaKeyFor(
                Decoders.BASE64URL.decode(jwtSecret)
        );
    }

    public boolean validateJwtToken(String authToken){
        try{
            System.out.println("Validate");
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        }
        catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}",e.getMessage());
        }
        catch (ExpiredJwtException e) {
            logger.error("JWT Token is expired: {}",e.getMessage());
        }
        catch (UnsupportedJwtException e) {
            logger.error("JWT Token is unsupported: {}",e.getMessage());
        }
        catch (IllegalArgumentException e){
            logger.error("JWT claims string is empty: {}",e.getMessage());
        }

        return false;
    }
}
