package erp;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CryptoUtils {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    public static SecretKey getKeyFromString(String keyString) {
        // Garante que a chave tenha exatamente 16 bytes
        byte[] keyBytes = new byte[16];
        byte[] keyInput = keyString.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(keyInput, 0, keyBytes, 0, Math.min(keyBytes.length, keyInput.length));
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    public static String encrypt(String data, String keyString) throws Exception {
        SecretKey key = getKeyFromString(keyString);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encryptedData, String keyString) throws Exception {
        SecretKey key = getKeyFromString(keyString);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedData = cipher.doFinal(decodedData);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }
    

}
