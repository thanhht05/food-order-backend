package com.thanh.foodorder.feature.auth.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.thanh.foodorder.core.util.exception.CommonException;
import com.thanh.foodorder.feature.auth.dto.GoogleUserInfo;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class GoogleAuthService {

    @Value("${google.client-id:}")
    private String googleClientId;

    public GoogleUserInfo verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier.Builder verifierBuilder = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance());

            if (googleClientId != null && !googleClientId.isBlank()) {
                verifierBuilder.setAudience(Collections.singletonList(googleClientId.trim()));
            }

            GoogleIdTokenVerifier verifier = verifierBuilder.build();
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                log.warn("Invalid Google ID token supplied");
                throw new CommonException("Xác thực Google ID Token thất bại hoặc token đã hết hạn");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");
            String sub = payload.getSubject();
            Boolean emailVerified = payload.getEmailVerified();

            log.info("Google Token verified successfully for email: {}", email);

            return GoogleUserInfo.builder()
                    .sub(sub)
                    .email(email)
                    .name(name != null ? name : email)
                    .picture(picture)
                    .emailVerified(Boolean.TRUE.equals(emailVerified))
                    .build();

        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error verifying Google ID token: {}", e.getMessage(), e);
            throw new CommonException("Lỗi xác thực Google ID Token: " + e.getMessage());
        }
    }
}
