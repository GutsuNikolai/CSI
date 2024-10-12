package Rabbit; // Объявляем пакет, в котором находится класс

import java.nio.charset.StandardCharsets; // Импортируем стандартные наборы символов
import java.util.Arrays; // Импортируем класс Arrays для работы с массивами
import java.util.Scanner; // Импортируем класс Scanner для считывания пользовательского ввода

public class Rabbit {
    private int[] x = new int[8]; // Регистры состояния (x) для алгоритма Rabbit
    private int[] c = new int[8]; // Счетчики (c) для алгоритма Rabbit

    // Константы a_j, используемые в обновлении счетчиков
    private static final int[] a = {
            0x4D34D34D,
            0xD34D34D3,
            0x34D34D34,
            0x4D34D34D,
            0xD34D34D3,
            0x34D34D34,
            0xD34D34D3,
            0x34D34D34
    };

    // Конструктор класса, принимающий ключ в виде массива байтов
    public Rabbit(byte[] key) {
        init(key); // Инициализируем алгоритм с заданным ключом
    }

    // Метод инициализации, который устанавливает начальные значения регистров и счетчиков
    private void init(byte[] key) {
        // Если длина ключа меньше 32 байтов, дополняем его нулями до 32 байтов
        if (key.length < 32) {
            byte[] extendedKey = new byte[32]; // Создаем массив размером 32 байта
            System.arraycopy(key, 0, extendedKey, 0, key.length); // Копируем данные из исходного ключа
            Arrays.fill(extendedKey, key.length, 32, (byte) 0); // Заполняем оставшиеся байты нулями
            key = extendedKey; // Присваиваем новый ключ
        }

        // Инициализация регистров состояния (x) на основе ключа
        for (int i = 0; i < 8; i++) {
            x[i] = ((key[i * 4] & 0xFF) << 24) | // Старший байт
                    ((key[i * 4 + 1] & 0xFF) << 16) | // Второй байт
                    ((key[i * 4 + 2] & 0xFF) << 8) | // Третий байт
                    (key[i * 4 + 3] & 0xFF); // Младший байт
        }

        // Инициализация счетчиков (c) (все элементы равны 0)
        Arrays.fill(c, 0);

        // Выполнение процесса инициализации, вызывая метод next() 4 раза
        for (int i = 0; i < 4; i++) {
            next(); // Генерация первого состояния
        }
    }

    // Получение младших 32 битов из значения
    private int LSW(int value) {
        return value & 0xFFFFFFFF; // Возвращаем младшие 32 бита
    }

    // Получение старших 32 битов из значения
    private int MSW(int value) {
        return (value >> 32) & 0xFFFFFFFF; // Возвращаем старшие 32 бита
    }

    // Генерация следующего состояния для регистров
    private void generateNextState() {
        int[] g = new int[8]; // Временный массив для хранения промежуточных значений

        // Вычисление g[j,i] на основе текущих значений x и c
        for (int j = 0; j < 8; j++) {
            g[j] = LSW(x[j] + c[j]); // Сложение x[j] и c[j]
            g[j] ^= MSW(x[j] + c[j]); // XOR результата с MSW
        }

        int[] newX = new int[8]; // Новый массив для обновленных регистров

        // Обновление регистров состояния (x) на основе g
        newX[0] = g[0] + (g[7] << 16) + (g[6] << 16);
        newX[1] = g[1] + (g[0] << 8) + g[7];
        newX[2] = g[2] + (g[1] << 16) + (g[0] << 16);
        newX[3] = g[3] + (g[2] << 8) + g[1];
        newX[4] = g[4] + (g[3] << 16) + (g[2] << 16);
        newX[5] = g[5] + (g[4] << 8) + g[3];
        newX[6] = g[6] + (g[5] << 16) + (g[4] << 16);
        newX[7] = g[7] + (g[6] << 8) + g[5];

        // Копируем новые значения в регистры состояния
        System.arraycopy(newX, 0, x, 0, 8);
    }

    // Обновление счетчиков (c)
    private void updateCounters() {
        for (int j = 0; j < 8; j++) {
            // Обновляем значение счетчика c[j]
            c[j] += a[j] + ((j == 0) ? (c[7] >>> 32) : (c[j - 1] >>> 32)); // Добавляем a[j] и старшие биты предыдущего счетчика
            c[j] %= (1L << 32); // Ограничиваем значение до 32 бит
        }
    }

    // Генерация 32-битного псевдослучайного числа
    public int next() {
        generateNextState(); // Генерация следующего состояния
        updateCounters(); // Обновление счетчиков
        // Возвращаем 32-битное значение, полученное из регистров
        return (x[0] + x[3] + x[5] + x[7]) & 0xFFFFFFFF;
    }

    // Метод для шифрования текста
    public byte[] encrypt(String plaintext) {
        byte[] data = plaintext.getBytes(StandardCharsets.UTF_8); // Преобразуем текст в массив байтов
        byte[] encrypted = new byte[data.length]; // Массив для хранения зашифрованного текста

        // Процесс шифрования
        for (int i = 0; i < data.length; i++) {
            // Шифрование: XOR байта данных с байтом ключевого потока
            encrypted[i] = (byte) (data[i] ^ (next() & 0xFF));
        }

        return encrypted; // Возвращаем зашифрованный текст
    }

    // Метод для дешифрования текста
    public String decrypt(byte[] encrypted) {
        byte[] decrypted = new byte[encrypted.length]; // Массив для хранения расшифрованного текста

        // Процесс дешифрования
        for (int i = 0; i < encrypted.length; i++) {
            // Дешифрование: XOR зашифрованного байта с байтом ключевого потока
            decrypted[i] = (byte) (encrypted[i] ^ (next() & 0xFF));
        }

        return new String(decrypted, StandardCharsets.UTF_8); // Преобразуем массив байтов в строку и возвращаем
    }

    // Основной метод для тестирования шифрования и дешифрования
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // Создаем объект Scanner для ввода данных
        System.out.println("Enter text for encrypting: "); // Запрос текста для шифрования
        String plaintext = scanner.nextLine(); // Считываем текст
        System.out.println("Enter secret key: "); // Запрос секретного ключа
        String key  = scanner.nextLine(); // Считываем ключ

        Rabbit rabbit = new Rabbit(key.getBytes(StandardCharsets.UTF_8)); // Создаем экземпляр класса Rabbit с ключом

        byte[] encrypted = rabbit.encrypt(plaintext); // Шифруем текст

        System.out.println("Encrypted: " + Arrays.toString(encrypted)); // Выводим зашифрованный текст

        String decrypted = rabbit.decrypt(encrypted); // Дешифруем зашифрованный текст
        System.out.println("Decrypted: " + decrypted); // Выводим расшифрованный текст
        System.out.println("Test passed: " + plaintext.equals(decrypted)); // Проверяем, совпадает ли расшифрованный текст с исходным
    }
}
