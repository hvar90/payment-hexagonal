package com.payment.gateway.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class WebhookSignatureValidator {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Value("${payment.gateway.webhook.secret-key}")
    private String secretKey;

    public boolean isValidSignature(String payload, String receivedSignature) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            
            byte[] hashBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = HexFormat.of().formatHex(hashBytes);
            
            return java.util.Arrays.equals(calculatedSignature.getBytes(), receivedSignature.getBytes());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return false;
        }
    }
}