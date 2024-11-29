package com.github.webhook.util;

import org.apache.commons.codec.binary.Hex;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class WebhookUtils {

    private static final String SECRET_KEY = "your_secret_key";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    public static boolean isValidSignature(String payload, String signature) {
        try {
            String calculatedSignature = calculateSignature(payload);
            return signature.equals(calculatedSignature);
        } catch (Exception e) {
            return false;
        }
    }

    private static String calculateSignature(String payload) throws Exception {
        Mac mac = getMacInstance();
        byte[] calculatedHash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return "sha256=" + Hex.encodeHexString(calculatedHash);
    }

    private static Mac getMacInstance() throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        mac.init(secretKeySpec);
        return mac;
    }
}
