package tiger.hash.algorithm;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class AESCipherExample {

    public static void main(String[] args) throws Exception {
        // Данные для шифрования
        String plainText = "Hello, world! (encrypting with AES)";

        // Генерация случайного ключа
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // 128-битный ключ для AES-128
        SecretKey secretKey = keyGen.generateKey();
        System.out.println("secretKey= " +  secretKey);

        // Генерация вектора инициализации (IV)
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        System.out.println("IV= " + ivSpec);

        // Шифрование данных
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
        String encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);
        System.out.println("Зашифрованный текст: " + encryptedText);

        // Дешифрование данных
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        String decryptedText = new String(decryptedBytes, "UTF-8");
        System.out.println("Расшифрованный текст: " + decryptedText);


    }

}

