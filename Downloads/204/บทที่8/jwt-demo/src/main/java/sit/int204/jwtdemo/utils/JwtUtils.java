package sit.int204.jwtdemo.utils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import sit.int204.jwtdemo.dtos.AccessToken;
import sit.int204.jwtdemo.entities.AuthUserDetail;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {
    @Value("#{${app.security.jwt.token-max-interval-in-minute}*1000*60}")
    private long MAX_TOKEN_INTERVAL;
    @Value("${app.security.jwt.key-id}")
    private String KEY_ID;
    private RSAKey rsaPrivateJWK;
    private RSAKey rsaPublicJWK;

    public RSAKey getRsaPublicJWK() {
        return this.rsaPublicJWK;
    }

    public JwtUtils() {
        try {
            rsaPrivateJWK = new RSAKeyGenerator(2048).keyID(KEY_ID).generate();
            rsaPublicJWK = rsaPrivateJWK.toPublicJWK();
            System.out.println(rsaPublicJWK.toJSONString());
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }
    public String generateToken(UserDetails user) {
        return generateToken(user,MAX_TOKEN_INTERVAL,TokenType.ACCESS_TOKEN);
    }

    public String generateToken(UserDetails user
    , long ageInMinute, TokenType tokenType) {
        try {
            JWSSigner signer = new RSASSASigner(rsaPrivateJWK);
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issuer("https://int204.sit.kmutt.ac.th")
                    .expirationTime(new Date(new Date().getTime() + ageInMinute))
                    .issueTime(new Date(new Date().getTime()))
                    .claim("authorities", user.getAuthorities())
                    .claim("typ", tokenType.toString())
                    .claim("uid", ((AuthUserDetail) user).getId())
                    .build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(rsaPrivateJWK.getKeyID()).build(), claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public void verifyToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new RSASSAVerifier(rsaPublicJWK);
            boolean passed =  signedJWT.verify(verifier);
            System.out.println("Token verified: " + passed);
            if (!passed) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED
                        , "Verify token failed");
            }
        } catch (JOSEException | ParseException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED
                    , "Verified Error, Invalid JWT", ex);
        }
    }

    public Map<String, Object> getJWTClaimsSet(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getClaims();
        } catch (
                ParseException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED
                    , "Invalid JWT (Can't parsed)", ex);
        }
    }

    public boolean isExpired(Map<String, Object> jwtClaims) {
        Date expDate = (Date) jwtClaims.get("exp");
        return expDate.before(new Date());
    }

    public boolean isValidClaims(Map<String, Object> jwtClaims) {
        System.out.println(jwtClaims);
        return jwtClaims.containsKey("iat") && "https://int204.sit.kmutt.ac.th"
                .equals(jwtClaims.get("iss")) && jwtClaims.containsKey("uid") &&
                (Long) jwtClaims.get("uid") > 0;
    }
}