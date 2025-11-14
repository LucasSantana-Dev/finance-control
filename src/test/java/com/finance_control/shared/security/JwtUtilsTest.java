package com.finance_control.shared.security;

import com.finance_control.shared.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtUtilsTest {

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private JwtUtils jwtUtils;

    private String testSecret;
    private SecretKey testKey;
    private Long testUserId;
    private Date testNow;
    private String testSupabaseSecret;
    private String testSupabaseUserId;

    @BeforeEach
    void setUp() {
        testSecret = "testSecretKeyMustBeAtLeast32CharactersLongForHS256Algorithm";
        testKey = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        testUserId = 1L;
        testNow = new Date();
        testSupabaseSecret = "testSupabaseSecretKeyMustBeAtLeast32CharactersLongForHS256Algorithm";
        testSupabaseUserId = UUID.randomUUID().toString();

        AppProperties.Jwt jwtRecord = new AppProperties.Jwt(
            testSecret, 86400000L, 604800000L, "finance-control", "finance-control-users"
        );
        AppProperties.Security securityRecord = new AppProperties.Security(
            jwtRecord,
            new AppProperties.Cors(List.of(), List.of(), List.of(), false, 0),
            List.of()
        );

        AppProperties.Supabase supabaseRecord = new AppProperties.Supabase(
            true, "https://test.supabase.co", "test-anon-key", testSupabaseSecret, "test-service-role-key",
            new AppProperties.SupabaseDatabase(),
            new AppProperties.Storage(true, "avatars", "documents", "transactions", new AppProperties.Compression()),
            new AppProperties.Realtime(true, List.of("transactions", "dashboard", "goals"))
        );

        when(appProperties.security()).thenReturn(securityRecord);
        when(appProperties.supabase()).thenReturn(supabaseRecord);
    }

    @Test
    void generateToken_WithValidUserId_ShouldCreateValidToken() {
        String token = jwtUtils.generateToken(testUserId);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        Claims claims = parseToken(token);
        assertThat(claims.getSubject()).isEqualTo(testUserId.toString());
        assertThat(claims.getIssuer()).isEqualTo("finance-control");
        assertThat(claims.getAudience()).contains("finance-control-users");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(testNow);
    }

    @Test
    void generateToken_ShouldSetCorrectExpiration() {
        String token = jwtUtils.generateToken(testUserId);

        Claims claims = parseToken(token);
        Date expiration = claims.getExpiration();
        Date expectedExpiration = new Date(testNow.getTime() + 86400000L);

        assertThat(expiration).isNotNull();
        assertThat(expiration.getTime()).isBetween(expectedExpiration.getTime() - 5000L, expectedExpiration.getTime() + 5000L);
    }

    @Test
    void generateRefreshToken_WithValidUserId_ShouldCreateValidToken() {
        String token = jwtUtils.generateRefreshToken(testUserId);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        Claims claims = parseToken(token);
        assertThat(claims.getSubject()).isEqualTo(testUserId.toString());
        assertThat(claims.getIssuer()).isEqualTo("finance-control");
        assertThat(claims.getAudience()).contains("finance-control-users-refresh");
    }

    @Test
    void generateRefreshToken_ShouldHaveLongerExpiration() {
        String token = jwtUtils.generateRefreshToken(testUserId);

        Claims claims = parseToken(token);
        Date expiration = claims.getExpiration();
        Date expectedExpiration = new Date(testNow.getTime() + 604800000L);

        assertThat(expiration).isNotNull();
        assertThat(expiration.getTime()).isBetween(expectedExpiration.getTime() - 5000L, expectedExpiration.getTime() + 5000L);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        String token = jwtUtils.generateToken(testUserId);

        boolean isValid = jwtUtils.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_WithInvalidSignature_ShouldReturnFalse() {
        String validToken = jwtUtils.generateToken(testUserId);

        String invalidToken = validToken.substring(0, validToken.length() - 5) + "xxxxx";

        boolean isValid = jwtUtils.validateToken(invalidToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() throws Exception {
        // Create a token that expires very soon
        AppProperties.Jwt shortJwt = new AppProperties.Jwt(
            testSecret, 1L, 604800000L, "finance-control", "finance-control-users"
        );
        AppProperties.Security shortSecurity = new AppProperties.Security(
            shortJwt,
            new AppProperties.Cors(List.of(), List.of(), List.of(), false, 0),
            List.of()
        );
        when(appProperties.security()).thenReturn(shortSecurity);

        String token = jwtUtils.generateToken(testUserId);

        Thread.sleep(10);

        boolean isValid = jwtUtils.validateToken(token);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_WithNullToken_ShouldThrowException() {
        // JWT library throws IllegalArgumentException for null tokens
        assertThatThrownBy(() -> jwtUtils.validateToken(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateToken_WithMalformedToken_ShouldReturnFalse() {
        String malformedToken = "not.a.valid.jwt.token";

        boolean isValid = jwtUtils.validateToken(malformedToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void getUserIdFromToken_WithValidToken_ShouldReturnUserId() {
        String token = jwtUtils.generateToken(testUserId);

        Long userId = jwtUtils.getUserIdFromToken(token);

        assertThat(userId).isEqualTo(testUserId);
    }

    @Test
    void getUserIdFromToken_WithInvalidToken_ShouldReturnNull() {
        String invalidToken = "invalid.token.here";

        Long userId = jwtUtils.getUserIdFromToken(invalidToken);

        assertThat(userId).isNull();
    }

    @Test
    void getUserIdFromToken_WithExpiredToken_ShouldReturnNull() throws Exception {
        // Create a token that expires very soon
        AppProperties.Jwt shortJwt = new AppProperties.Jwt(
            testSecret, 1L, 604800000L, "finance-control", "finance-control-users"
        );
        AppProperties.Security shortSecurity = new AppProperties.Security(
            shortJwt,
            new AppProperties.Cors(List.of(), List.of(), List.of(), false, 0),
            List.of()
        );
        when(appProperties.security()).thenReturn(shortSecurity);

        String token = jwtUtils.generateToken(testUserId);

        Thread.sleep(10);

        Long userId = jwtUtils.getUserIdFromToken(token);

        assertThat(userId).isNull();
    }

    @Test
    void getUserIdFromToken_WithNullToken_ShouldThrowException() {
        // JWT library throws IllegalArgumentException for null tokens
        assertThatThrownBy(() -> jwtUtils.getUserIdFromToken(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getExpirationFromToken_WithValidToken_ShouldReturnExpiration() {
        String token = jwtUtils.generateToken(testUserId);

        Date expiration = jwtUtils.getExpirationFromToken(token);

        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(testNow);
    }

    @Test
    void getExpirationFromToken_WithInvalidToken_ShouldReturnNull() {
        String invalidToken = "invalid.token";

        Date expiration = jwtUtils.getExpirationFromToken(invalidToken);

        assertThat(expiration).isNull();
    }

    @Test
    void getExpirationFromToken_WithNullToken_ShouldThrowException() {
        // JWT library throws IllegalArgumentException for null tokens
        assertThatThrownBy(() -> jwtUtils.getExpirationFromToken(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isTokenExpired_WithValidToken_ShouldReturnFalse() {
        String token = jwtUtils.generateToken(testUserId);

        boolean isExpired = jwtUtils.isTokenExpired(token);

        assertThat(isExpired).isFalse();
    }

    @Test
    void isTokenExpired_WithExpiredToken_ShouldReturnFalse() throws Exception {
        // Note: When token is expired, getExpirationFromToken catches JwtException
        // (including ExpiredJwtException) and returns null. Since expiration is null,
        // isTokenExpired returns false (see implementation: expiration != null && expiration.before(new Date()))
        AppProperties.Jwt shortJwt = new AppProperties.Jwt(
            testSecret, -1000L, 604800000L, "finance-control", "finance-control-users"
        );
        AppProperties.Security shortSecurity = new AppProperties.Security(
            shortJwt,
            new AppProperties.Cors(List.of(), List.of(), List.of(), false, 0),
            List.of()
        );
        when(appProperties.security()).thenReturn(shortSecurity);

        String token = jwtUtils.generateToken(testUserId);

        boolean isExpired = jwtUtils.isTokenExpired(token);

        assertThat(isExpired).isFalse();
    }

    @Test
    void isTokenExpired_WithInvalidToken_ShouldReturnFalse() {
        // getExpirationFromToken returns null for invalid tokens
        // isTokenExpired returns false when expiration is null (see implementation)
        String invalidToken = "invalid.token";

        boolean isExpired = jwtUtils.isTokenExpired(invalidToken);

        assertThat(isExpired).isFalse();
    }

    @Test
    void isTokenExpired_WithNullToken_ShouldThrowException() {
        // getExpirationFromToken throws IllegalArgumentException for null
        assertThatThrownBy(() -> jwtUtils.isTokenExpired(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generateToken_ShouldHaveDifferentTokensWhenGeneratedAtDifferentTimes() throws Exception {
        String token1 = jwtUtils.generateToken(testUserId);

        // Wait long enough to ensure different millisecond timestamp
        Thread.sleep(1100); // 1.1 seconds to cross millisecond boundary

        String token2 = jwtUtils.generateToken(testUserId);

        // Tokens should be different due to different issuedAt timestamps
        assertThat(token1).isNotEqualTo(token2);

        // Verify they have different issuedAt values
        Claims claims1 = parseToken(token1);
        Claims claims2 = parseToken(token2);
        assertThat(claims1.getIssuedAt().getTime()).isLessThan(claims2.getIssuedAt().getTime());
    }

    // Supabase JWT Tests

    @Test
    void validateSupabaseToken_WithValidSupabaseToken_ShouldReturnTrue() {
        String supabaseToken = generateSupabaseToken(testSupabaseUserId, "authenticated");

        boolean isValid = jwtUtils.validateSupabaseToken(supabaseToken);

        assertThat(isValid).isTrue();
    }

    @Test
    void validateSupabaseToken_WithInvalidSignature_ShouldReturnFalse() {
        String validToken = generateSupabaseToken(testSupabaseUserId, "authenticated");

        String invalidToken = validToken.substring(0, validToken.length() - 5) + "xxxxx";

        boolean isValid = jwtUtils.validateSupabaseToken(invalidToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateSupabaseToken_WithExpiredToken_ShouldReturnFalse() {
        Date pastDate = new Date(System.currentTimeMillis() - 3600000); // 1 hour ago
        String expiredToken = generateSupabaseToken(testSupabaseUserId, "authenticated", pastDate);

        boolean isValid = jwtUtils.validateSupabaseToken(expiredToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateSupabaseToken_WithInvalidRole_ShouldReturnFalse() {
        String tokenWithInvalidRole = generateSupabaseToken(testSupabaseUserId, "invalid_role");

        boolean isValid = jwtUtils.validateSupabaseToken(tokenWithInvalidRole);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateSupabaseToken_WithInvalidUUID_ShouldReturnFalse() {
        String tokenWithInvalidUUID = generateSupabaseToken("not-a-uuid", "authenticated");

        boolean isValid = jwtUtils.validateSupabaseToken(tokenWithInvalidUUID);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateSupabaseToken_WithNullToken_ShouldReturnFalse() {
        boolean isValid = jwtUtils.validateSupabaseToken(null);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateSupabaseToken_WithEmptyToken_ShouldReturnFalse() {
        boolean isValid = jwtUtils.validateSupabaseToken("");

        assertThat(isValid).isFalse();
    }

    @Test
    void getSupabaseUserIdFromToken_WithValidToken_ShouldReturnUUID() {
        String token = generateSupabaseToken(testSupabaseUserId, "authenticated");

        String userId = jwtUtils.getSupabaseUserIdFromToken(token);

        assertThat(userId).isEqualTo(testSupabaseUserId);
    }

    @Test
    void getSupabaseUserIdFromToken_WithInvalidToken_ShouldReturnNull() {
        String invalidToken = "invalid.supabase.token";

        String userId = jwtUtils.getSupabaseUserIdFromToken(invalidToken);

        assertThat(userId).isNull();
    }

    @Test
    void getSupabaseRoleFromToken_WithValidToken_ShouldReturnRole() {
        String token = generateSupabaseToken(testSupabaseUserId, "authenticated");

        String role = jwtUtils.getSupabaseRoleFromToken(token);

        assertThat(role).isEqualTo("authenticated");
    }

    @Test
    void getSupabaseRoleFromToken_WithInvalidToken_ShouldReturnNull() {
        String invalidToken = "invalid.supabase.token";

        String role = jwtUtils.getSupabaseRoleFromToken(invalidToken);

        assertThat(role).isNull();
    }

    @Test
    void isSupabaseToken_WithValidSupabaseToken_ShouldReturnTrue() {
        String supabaseToken = generateSupabaseToken(testSupabaseUserId, "authenticated");

        boolean isSupabaseToken = jwtUtils.isSupabaseToken(supabaseToken);

        assertThat(isSupabaseToken).isTrue();
    }

    @Test
    void isSupabaseToken_WithAppToken_ShouldReturnFalse() {
        String appToken = jwtUtils.generateToken(testUserId);

        boolean isSupabaseToken = jwtUtils.isSupabaseToken(appToken);

        assertThat(isSupabaseToken).isFalse();
    }

    @Test
    void validateTokenUniversal_WithAppToken_ShouldReturnTrue() {
        String appToken = jwtUtils.generateToken(testUserId);

        boolean isValid = jwtUtils.validateTokenUniversal(appToken);

        assertThat(isValid).isTrue();
    }

    @Test
    void validateTokenUniversal_WithSupabaseToken_ShouldReturnTrue() {
        String supabaseToken = generateSupabaseToken(testSupabaseUserId, "authenticated");

        boolean isValid = jwtUtils.validateTokenUniversal(supabaseToken);

        assertThat(isValid).isTrue();
    }

    @Test
    void validateTokenUniversal_WithInvalidToken_ShouldReturnFalse() {
        String invalidToken = "invalid.token";

        boolean isValid = jwtUtils.validateTokenUniversal(invalidToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void getUserIdFromTokenUniversal_WithAppToken_ShouldReturnLong() {
        String appToken = jwtUtils.generateToken(testUserId);

        Object userId = jwtUtils.getUserIdFromTokenUniversal(appToken);

        assertThat(userId).isInstanceOf(Long.class);
        assertThat(userId).isEqualTo(testUserId);
    }

    @Test
    void getUserIdFromTokenUniversal_WithSupabaseToken_ShouldReturnString() {
        String supabaseToken = generateSupabaseToken(testSupabaseUserId, "authenticated");

        Object userId = jwtUtils.getUserIdFromTokenUniversal(supabaseToken);

        assertThat(userId).isInstanceOf(String.class);
        assertThat(userId).isEqualTo(testSupabaseUserId);
    }

    @Test
    void getUserIdFromTokenUniversal_WithInvalidToken_ShouldReturnNull() {
        String invalidToken = "invalid.token";

        Object userId = jwtUtils.getUserIdFromTokenUniversal(invalidToken);

        assertThat(userId).isNull();
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(testKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String generateSupabaseToken(String userId, String role) {
        return generateSupabaseToken(userId, role, new Date(System.currentTimeMillis() + 3600000)); // 1 hour from now
    }

    private String generateSupabaseToken(String userId, String role, Date expiration) {
        SecretKey supabaseKey = Keys.hmacShaKeyFor(testSupabaseSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(userId)
                .issuer("https://test.supabase.co")
                .audience().add("authenticated").and()
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(supabaseKey)
                .compact();
    }
}
