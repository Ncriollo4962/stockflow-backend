package com.stockflow.core.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

/**
 * Esta clase es el Proveedor de Tokens (Provider). 
 * Pertenece a la infraestructura de seguridad y su única responsabilidad es 
 * la creación, lectura y validación técnica de los JSON Web Tokens.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    // La llave secreta se inyecta desde application.properties por seguridad.
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    // Tiempo de vida del token (ej. 86400000 ms para 24h).
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    private final long REFRESH_TOKEN_EXPIRATION = 24 * 60 * 60 * 1000; // 1 día

    /**
     * MÉTODO: Generar Token
     * Uso: Se llama desde el AuthService cuando el usuario pone bien su contraseña.
     * @param userDetails Objeto de Spring Security que contiene los datos del usuario.
     * @return Un String que representa el JWT firmado.
     */
    public String generateToken(UserDetails userDetails) {
        // Map.of() es una forma moderna de Java para crear un mapa vacío (sin claims extra).
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "ACCESS");
        return buildToken(claims, userDetails, jwtExpiration);
    }

     /**
     * MÉTODO: Generar Refresh Token
     * Uso: Se llama desde el AuthService cuando el usuario quiere refrescar su token.
     * @param userDetails Objeto de Spring Security que contiene los datos del usuario.
     * @return Un String que representa el JWT firmado.
     */
    public String generateRefreshToken(UserDetails userDetails) {
        // Map.of() es una forma moderna de Java para crear un mapa vacío (sin claims extra).
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "REFRESH");
        return buildToken(claims, userDetails, REFRESH_TOKEN_EXPIRATION);
    }

    /**
     * MÉTODO: Construir Token (Interno)
     * Qué hace: Utiliza el Builder de JJWT para armar las tres partes del token.
     */
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .claims(extraClaims) // 1. Payload: Datos personalizados.
                .subject(userDetails.getUsername()) // 1. Payload: El "sub" (quién es el dueño).
                .issuedAt(new Date(System.currentTimeMillis())) // 1. Payload: Fecha de emisión.
                .expiration(new Date(System.currentTimeMillis() + expiration)) // 1. Payload: Fecha de caducidad.
                .signWith(getSignInKey()) // 3. Signature: Firma el token con nuestra llave secreta.
                .compact(); // Convierte todo a la cadena de texto final (Header.Payload.Signature).
    }

    /**
     * MÉTODO: Validar Token
     * Uso: Se llama desde el Filtro en cada petición.
     * Qué hace: Verifica que el usuario del token coincida con el de la DB y que no haya expirado.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Comparación de seguridad: El nombre debe coincidir y el tiempo no debe haber pasado.
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * MÉTODO: Extraer Usuario (Subject)
     * Qué hace: Es un atajo para obtener el nombre del usuario sin leer todo el token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * MÉTODO: Extraer Claim Específico
     * Qué hace: Recibe una función (referencia a método) para extraer un dato puntual.
     * Es programación funcional pura de Java 8+.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * MÉTODO: Extraer TODOS los Claims (El más importante)
     * Qué hace: Usa la llave secreta para "abrir" el JWT.
     * Si el token fue alterado o la firma no coincide, este método lanza una excepción.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey()) // Configura la llave de verificación.
                .build() // Crea el motor del parser (Inmutable/Thread-safe).
                .parseSignedClaims(token) // Intenta leer el token.
                .getPayload(); // Devuelve el contenido (Cuerpo) del mensaje.
    }

    /**
     * MÉTODO: Obtener Llave de Firma
     * Qué hace: Convierte tu String de configuración en un objeto SecretKey de Java.
     * Se usa UTF-8 para garantizar que la llave se lea igual en cualquier servidor.
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * MÉTODO: Verificar Expiración (Interno)
     */
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Verifica si el token tiene el claim "type" con valor "REFRESH".
     * Esto evita que alguien use un Access Token para generar nuevos tokens.
     */
    public boolean isRefreshToken(String token) {
        try {
            final Claims claims = extractAllClaims(token);
            String type = claims.get("type", String.class);
            return "REFRESH".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

}
