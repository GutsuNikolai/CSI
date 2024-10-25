from sbox import *
A = 0x0123456789ABCDEF
B = 0xFEDCBA9876543210
C = 0xF0E1D2C3B4A59687

def preprocess_message(message):
    # Преобразуем сообщение в байты
    message_bytes = message.encode('utf-8')
    original_length = len(message_bytes) * 8  # Длина сообщения в битах

    # Добавляем бит '1'
    message_bytes += b'\x80'

    # Дополняем нулями до 448 бит (56 байт)
    while (len(message_bytes) % 64) != 56:
        message_bytes += b'\x00'

    # Добавляем длину сообщения (64 бита)
    message_bytes += original_length.to_bytes(8, byteorder='big')
    print((message_bytes))
    # Разбиваем на блоки по 64 байта (512 бит)
    blocks = [message_bytes[i:i + 64] for i in range(0, len(message_bytes), 64)]

    print(f"Processed message blocks: {[block.hex() for block in blocks]}") #Вывод результата
    return blocks

def compress_block(block, a, b, c, sbox1, sbox2, sbox3, sbox4):
    # Разделение блока на 8 64-битных частей
    x = [int.from_bytes(block[i:i+8], 'little') for i in range(0, 64, 8)]

    # Начальное сжатие, используя каждый подблок
    for i in range(8):
        # Определяем S-box для текущего блока
        sbox = sbox1 if i < 2 else sbox2 if i < 4 else sbox3 if i < 6 else sbox4

        # Применение подстановок с использованием таблицы S-box
        a += x[i] + sbox[i % len(sbox)]  # Пример использования S-box
        a &= (1 << 64) - 1  # Ограничение до 64 бит

        b ^= a
        c = (c + b) % (1 << 64)  # 64-битное ограничение

        # Выполнение начального преобразования регистров
        a, b, c = c, a, b

    print(f"Compressed block results: A={a:X}, B={b:X}, C={c:X}")  # Вывод результатов
    return a, b, c

def expand_block(block):
    # Разделяем 512-битный блок на 8 64-битных слов
    x = [int.from_bytes(block[i:i + 8], 'little') for i in range(0, 64, 8)]
    return x

def to_binary_string(value):
    """Преобразует число в строку двоичного представления фиксированной длины (64 бита)."""
    return format(value, '064b')  # 64 бита

def compress_function(block, a, b, c, sbox1, sbox2, sbox3, sbox4):
    print("1 round of compression----")
    # Расширение блока
    x = expand_block(block)

    # Первый раунд
    round = 0
    # Определяем текущий S-box в зависимости от номера раунда
    sbox = sbox1  # Используем первый S-box

    print("Initial values:")
    print(f"a = {to_binary_string(a)} (decimal: {a})")
    print(f"b = {to_binary_string(b)} (decimal: {b})")
    print(f"c = {to_binary_string(c)} (decimal: {c})")
    print(f"x[0] = {to_binary_string(x[0])} (decimal: {x[0]})")
    print(f"sbox[0] = {to_binary_string(sbox[0])} (decimal: {sbox[0]})")

    # Выполнение операций для первого раунда
    print(f"\nRound {round + 1} calculations:")

    # Расчет a
    a_prev = a  # Сохраняем предыдущее значение a для вывода
    a = (a + x[round % 8] + sbox[round % 256]) & ((1 << 64) - 1)  # Ограничение до 64 бит
    print(f"a (before) = {to_binary_string(a_prev)} (decimal: {a_prev})")
    print(f"x[round % 8] = {to_binary_string(x[round % 8])} (decimal: {x[round % 8]})")
    print(f"sbox[round % 256] = {to_binary_string(sbox[round % 256])} (decimal: {sbox[round % 256]})")
    print(f"a (after) = {to_binary_string(a)} (decimal: {a})")

    # Расчет b
    b_prev = b  # Сохраняем предыдущее значение b для вывода
    b = (b ^ a) & ((1 << 64) - 1)  # Ограничение до 64 бит
    print(f"b (before) = {to_binary_string(b_prev)} (decimal: {b_prev})")
    print(f"b (after) = {to_binary_string(b)} (decimal: {b})")

    # Расчет c
    c_prev = c  # Сохраняем предыдущее значение c для вывода
    c = (c + b) & ((1 << 64) - 1)  # 64-битное ограничение
    print(f"c (before) = {to_binary_string(c_prev)} (decimal: {c_prev})")
    print(f"c (after) = {to_binary_string(c)} (decimal: {c})")

    # Побитовые сдвиги
    a_prev = a  # Сохраняем предыдущее значение a для вывода
    a = (a >> 7) | (a << (64 - 7))  # Сдвиг вправо на 7 бит
    print(f"Right shift a by 7 bits (before) = {to_binary_string(a_prev)} (decimal: {a_prev})")
    a = a & ((1 << 64) - 1)  # Ограничение до 64 бит

    b = b & ((1 << 64) - 1)  # Ограничение до 64 бит
    c = c & ((1 << 64) - 1)  # Ограничение до 64 бит

    # Обмен регистров
    a, b, c = b, c, a

    print(f"\nValues after first round: A={to_binary_string(a)}, B={to_binary_string(b)}, C={to_binary_string(c)}")
    return a, b, c


def final_compression(a, b, c):
    # Финальное значение хэша
    final_hash = (a.to_bytes(8, 'big') +
                  b.to_bytes(8, 'big') +
                  c.to_bytes(8, 'big'))

    return final_hash

# Пример использования
message = "Hello, Tiger!"
print("Message:", message)
blocks = preprocess_message(message)

# Печать полученных блоков
for i, block in enumerate(blocks):
    print(f"Block {i}: {block.hex()}")

print(compress_block(block,A,B,C,sbox1,sbox2,sbox3,sbox4))



# Использование первого блока
block = blocks[0]  # Используем первый блок из обработанных данных
new_a, new_b, new_c = compress_function(block, A, B, C, sbox1, sbox2, sbox3, sbox4)

# Финальная компрессия
final_hash = final_compression(new_a, new_b, new_c)
print(f"Final hash: {final_hash.hex().upper()}")  # Вывод финального хэша
