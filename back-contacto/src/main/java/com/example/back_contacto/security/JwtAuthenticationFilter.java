package com.example.back_contacto.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String SECRET_KEY = "VGhpcy1pcy1hLXZlcnktc2VjdXJlLWtleS1mb3ItamF2YS1zcHJpbmctYm9vdC1qd3Q=";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);

            Claims claims = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();

            String rut = claims.getSubject();
            String permisos = claims.get("permisos", String.class);

            // Si permisos viene null, asignamos USER por defecto y evitamos el NullPointerException
            String rol = (permisos != null && !permisos.isBlank()) 
                    ? "ROLE_" + permisos.toUpperCase() 
                    : "ROLE_USER";

            if (rut != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        rut,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(rol))
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println(">>> [CONTACTOS AUTH EXITOSA] Peticion permitida para RUT: " + rut);
            }
        } catch (Exception e) {
            System.err.println(">>> [CONTACTOS ERROR AUTH] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private SecretKey getSignInKey() {
        try {
            byte[] keyBytes = Decoders.BASE64URL.decode(SECRET_KEY);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e1) {
            try {
                byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
                return Keys.hmacShaKeyFor(keyBytes);
            } catch (Exception e2) {
                return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}