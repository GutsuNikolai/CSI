package tiger.hash.algorithm;
import java.util.Base64;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Arrays;

public class AESCipherStepByStep {

    private static final int Nb = 4; // Размер блока в словах: AES использует Nb = 4
    private static final int Nk = 4; // Длина ключа в словах: AES-128 использует Nk = 4
    private static final int Nr = 10; // Число раундов: AES-128 использует Nr = 10

    // S-box для шага SubBytes
    private static final int[] sbox = {
            0x63, 0x7C, 0x77, 0x7B, 0xF2, 0x6B, 0x6F, 0xC5, 0x30, 0x01, 0x67, 0x2B, 0xFE, 0xD7, 0xAB, 0x76,
            0xCA, 0x82, 0xC9, 0x7D, 0xFA, 0x59, 0x47, 0xF0, 0xAD, 0xD4, 0xA2, 0xAF, 0x9C, 0xA4, 0x72, 0xC0,
            0xB7, 0xFD, 0x93, 0x26, 0x36, 0x3F, 0xF7, 0xCC, 0x34, 0xA5, 0xE5, 0xF1, 0x71, 0xD8, 0x31, 0x15,
            0x04, 0xC7, 0x23, 0xC3, 0x18, 0x96, 0x05, 0x9A, 0x07, 0x12, 0x80, 0xE2, 0xEB, 0x27, 0xB2, 0x75,
            0x09, 0x83, 0x2C, 0x1A, 0x1B, 0x6E, 0x5A, 0xA0, 0x52, 0x3B, 0xD6, 0xB3, 0x29, 0xE3, 0x2F, 0x84,
            0x53, 0xD1, 0x00, 0xED, 0x20, 0xFC, 0xB1, 0x5B, 0x6A, 0xCB, 0xBE, 0x39, 0x4A, 0x4C, 0x58, 0xCF,
            0xD0, 0xEF, 0xAA, 0xFB, 0x43, 0x4D, 0x33, 0x85, 0x45, 0xF9, 0x02, 0x7F, 0x50, 0x3C, 0x9F, 0xA8,
            0x51, 0xA3, 0x40, 0x8F, 0x92, 0x9D, 0x38, 0xF5, 0xBC, 0xB6, 0xDA, 0x21, 0x10, 0xFF, 0xF3, 0xD2,
            0xCD, 0x0C, 0x13, 0xEC, 0x5F, 0x97, 0x44, 0x17, 0xC4, 0xA7, 0x7E, 0x3D, 0x64, 0x5D, 0x19, 0x73,
            0x60, 0x81, 0x4F, 0xDC, 0x22, 0x2A, 0x90, 0x88, 0x46, 0xEE, 0xB8, 0x14, 0xDE, 0x5E, 0x0B, 0xDB,
            0xE0, 0x32, 0x3A, 0x0A, 0x49, 0x06, 0x24, 0x5C, 0xC2, 0xD3, 0xAC, 0x62, 0x91, 0x95, 0xE4, 0x79,
            0xE7, 0xC8, 0x37, 0x6D, 0x8D, 0xD5, 0x4E, 0xA9, 0x6C, 0x56, 0xF4, 0xEA, 0x65, 0x7A, 0xAE, 0x08,
            0xBA, 0x78, 0x25, 0x2E, 0x1C, 0xA6, 0xB4, 0xC6, 0xE8, 0xDD, 0x74, 0x1F, 0x4B, 0xBD, 0x8B, 0x8A,
            0x70, 0x3E, 0xB5, 0x66, 0x48, 0x03, 0xF6, 0x0E, 0x61, 0x35, 0x57, 0xB9, 0x86, 0xC1, 0x1D, 0x9E,
            0xE1, 0xF8, 0x98, 0x11, 0x69, 0xD9, 0x8E, 0x94, 0x9B, 0x1E, 0x87, 0xE9, 0xCE, 0x55, 0x28, 0xDF,
            0x8C, 0xA1, 0x89, 0x0D, 0xBF, 0xE6, 0x42, 0x68, 0x41, 0x99, 0x2D, 0x0F, 0xB0, 0x54, 0xBB, 0x16
    };

    // Пример метода для расширения ключа и демонстрации AddRoundKey
    public static int[][] expandKey(byte[] key) {
        int[][] roundKeys = new int[Nr + 1][Nb * 4];

        // Заполните `roundKeys` значениями для каждого раунда.
        // Для демонстрации здесь можно использовать простые значения.
        for (int i = 0; i < roundKeys.length; i++) {
            for (int j = 0; j < roundKeys[i].length; j++) {
                roundKeys[i][j] = key[j % key.length] & 0xFF; // Заполняем ключи тестовыми значениями
            }
        }

        return roundKeys;
    }

    // Метод для SubBytes
    public static void subBytes(int[][] state) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] = sbox[state[i][j]]; // замените state на соответствующий байт из S-box
            }
        }
    }

    // Метод для ShiftRows
    public static void shiftRows(int[][] state) {
        for (int i = 1; i < 4; i++) {
            state[i] = rotateRowLeft(state[i], i); // Поворачиваем строку i на i позиций
        }
    }

    // Вспомогательный метод для вращения строки влево
    private static int[] rotateRowLeft(int[] row, int n) {
        int[] newRow = new int[4];
        for (int i = 0; i < 4; i++) {
            newRow[i] = row[(i + n) % 4];
        }
        return newRow;
    }

    // Метод для MixColumns
    public static void mixColumns(int[][] state) {
        // Выполните MixColumns, используя стандартное матричное умножение в поле GF(2^8)
    }

    // Метод для AddRoundKey
    public static void addRoundKey(int[][] state, int[][] roundKey) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] ^= roundKey[i][j];
            }
        }
    }

    // Главный метод для демонстрации первого раунда AES
    public static void main(String[] args) throws Exception {
        // 1. Инициализация параметров AES
        byte[] keyBytes = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(keyBytes);
        SecretKey secretKey = KeyGenerator.getInstance("AES").generateKey();

        // 2. Generate and print initial round key
        int[][] roundKeys = expandKey(keyBytes);
        System.out.println("Initial round key: " + Arrays.toString(roundKeys[0]));


        // 3. Инициализация состояния с примерным текстовым блоком
        int[][] state = {
                {0x32, 0x43, 0xf6, 0xa8},
                {0x88, 0x5a, 0x30, 0x8d},
                {0x31, 0x31, 0x98, 0xa2},
                {0xe0, 0x37, 0x07, 0x34}
        };
        System.out.println("Initial state: " + Arrays.deepToString(state));

        // 4. AddRoundKey
        addRoundKey(state, roundKeys);
        System.out.println("After AddRoundKey: " + Arrays.deepToString(state));

        // 5. SubBytes
        subBytes(state);
        System.out.println("After SubBytes: " + Arrays.deepToString(state));

        // 6. ShiftRows
        shiftRows(state);
        System.out.println("After ShiftRows: " + Arrays.deepToString(state));

        // 7. MixColumns (только для раундов 1 до Nr-1)
        mixColumns(state);
        System.out.println("After MixColumns: " + Arrays.deepToString(state));

        // Вывод финального состояния для раунда 1
        System.out.println("State after Round 1: " + Arrays.deepToString(state));

        String base64EncodedResult = "aJZ/BlqJc/OEwzmKTKfqzJ+N52JPAPtdA3qNH6voP8aCnLUo/K/J3EDDCn5RwWe6";

        // Декодируем Base64 в байты
        byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedResult);

        // Преобразуем байты в шестнадцатиричную строку
        StringBuilder hexString = new StringBuilder();
        for (byte b : decodedBytes) {
            // Форматируем каждый байт в шестнадцатиричное значение
            hexString.append(String.format("%02x", b));
        }

        // Результат в шестнадцатиричном формате
        System.out.println("Hexadecimal representation: " + hexString.toString());
    }
}
