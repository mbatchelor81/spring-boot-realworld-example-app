package com.ftgo.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JwtTokenProvider {

  private final RSAKey rsaKey;
  private final JWSSigner signer;
  private final JWSVerifier verifier;
  private final String issuer;
  private final long tokenValiditySeconds;

  public JwtTokenProvider(RSAKey rsaKey, String issuer, long tokenValiditySeconds) {
    this.rsaKey = rsaKey;
    this.issuer = issuer;
    this.tokenValiditySeconds = tokenValiditySeconds;
    try {
      this.signer = new RSASSASigner(rsaKey);
      this.verifier = new RSASSAVerifier(rsaKey.toRSAPublicKey());
    } catch (JOSEException e) {
      throw new IllegalStateException("Failed to initialize JWT signer/verifier", e);
    }
  }

  public static JwtTokenProvider withGeneratedKey(String issuer, long tokenValiditySeconds) {
    try {
      RSAKey rsaKey = new RSAKeyGenerator(2048).keyID(UUID.randomUUID().toString()).generate();
      return new JwtTokenProvider(rsaKey, issuer, tokenValiditySeconds);
    } catch (JOSEException e) {
      throw new IllegalStateException("Failed to generate RSA key", e);
    }
  }

  public String createToken(
      String subject, String username, List<String> roles, Map<String, Object> additionalClaims) {

    Instant now = Instant.now();
    Instant expiry = now.plusSeconds(tokenValiditySeconds);

    JWTClaimsSet.Builder claimsBuilder =
        new JWTClaimsSet.Builder()
            .subject(subject)
            .issuer(issuer)
            .claim("preferred_username", username)
            .claim("realm_access", Map.of("roles", roles))
            .issueTime(Date.from(now))
            .expirationTime(Date.from(expiry))
            .jwtID(UUID.randomUUID().toString());

    if (additionalClaims != null) {
      additionalClaims.forEach(claimsBuilder::claim);
    }

    SignedJWT signedJWT =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
            claimsBuilder.build());

    try {
      signedJWT.sign(signer);
    } catch (JOSEException e) {
      throw new IllegalStateException("Failed to sign JWT", e);
    }

    return signedJWT.serialize();
  }

  public String createToken(String subject, String username, List<String> roles) {
    return createToken(subject, username, roles, null);
  }

  public boolean validateToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      if (!signedJWT.verify(verifier)) {
        return false;
      }
      Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
      return expirationTime != null && expirationTime.after(new Date());
    } catch (ParseException | JOSEException e) {
      return false;
    }
  }

  public JWTClaimsSet parseToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      return signedJWT.getJWTClaimsSet();
    } catch (ParseException e) {
      throw new IllegalArgumentException("Invalid JWT token", e);
    }
  }

  public RSAKey getRsaKey() {
    return rsaKey;
  }

  public String getIssuer() {
    return issuer;
  }
}
