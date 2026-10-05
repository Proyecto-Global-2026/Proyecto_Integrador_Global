package com.example.proyecto.global.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.proyecto.global.domain.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	public static final String CLAIM_ROL = "rol";
	public static final String CLAIM_NOMBRE = "nombre";

	private final SecretKey key;
	private final long expirationSeconds;

	public JwtService(@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration-minutes:60}") long expirationMinutes) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationSeconds = expirationMinutes * 60;
	}

	public String generateToken(Usuario usuario) {
		Instant ahora = Instant.now();
		return Jwts.builder()
				.subject(usuario.getEmail())
				.claim(CLAIM_NOMBRE, usuario.getNombre())
				.claim(CLAIM_ROL, usuario.getRol().getNombre())
				.issuedAt(Date.from(ahora))
				.expiration(Date.from(ahora.plusSeconds(expirationSeconds)))
				.signWith(key)
				.compact();
	}

	public String extractUsername(String token) {
		return claims(token).getSubject();
	}

	public String extractRol(String token) {
		return claims(token).get(CLAIM_ROL, String.class);
	}

	public long getExpirationSeconds() {
		return expirationSeconds;
	}

	public boolean isTokenValido(String token, String username) {
		try {
			Claims claims = claims(token);
			return username.equals(claims.getSubject()) && claims.getExpiration().after(new Date());
		} catch (RuntimeException ex) {
			return false;
		}
	}

	private Claims claims(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}