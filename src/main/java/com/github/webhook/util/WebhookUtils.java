package com.github.webhook.util;

import org.apache.commons.codec.binary.Hex;
import com.github.webhook.model.MaterialType;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class WebhookUtils {

    private static final String SECRET_KEY = "your_secret_key";

    public static boolean isValidSignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] calculatedHash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = "sha256=" + Hex.encodeHexString(calculatedHash);
            return signature.equals(calculatedSignature);
        } catch (Exception e) {
            return false;
        }
    }

    public static MaterialType getMaterialType(String filePath, boolean isAdded) {
        if (isAdded) {
            // Если файл добавлен, проверяем его путь
            if (filePath.toLowerCase().contains("code")) {
                return MaterialType.CODE; // Тип: код
            }
            return MaterialType.SUPPORTING_FILES; // Тип: вспомогательные файлы
        }
        return null; // Для измененных файлов не возвращаем тип
    }
}
