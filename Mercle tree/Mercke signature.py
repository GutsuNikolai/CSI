import hashlib
import secrets

# Получение хэша от переданного значения (строки)
def generate_hash(value):
    # Преобразуем значение в байты, если оно еще не в байтах
    if isinstance(value, str):
        value = value.encode('utf-8')
    # Создаем хэш-объект SHA-256 и возвращаем хэш
    return hashlib.sha256(value).hexdigest()

# Генерация случайных пар для секретного числа (256 пар, так как 256 битный хэш)
def generate_secret_key():
    secret_key = []  # Array of secret pairs
    for i in range(256):
        a = secrets.token_bytes(16) #128 bites
        b = secrets.token_bytes(16)
        secret_key.append((a, b))
    return secret_key

# Создание публичного ключа на основе секретного
def generate_public_key(secret_key):
    public_key = [[generate_hash(x), generate_hash(y)] for x,y in secret_key]
    return public_key

# Создание подписи Лемпорта
def generate_lamport_signature(document, secret_key):
    signature = []
    document_hash = hashlib.sha256(document.encode()).hexdigest()
    bit_string = bin(int(document_hash, 16))[2:].zfill(256)

    for i, bit  in enumerate(bit_string):
        a, b = secret_key[i]
        signature.append(a if bit == "0" else b)

    return signature

# Генерация хэша публичного ключа
def get_public_key_hash(public_key):
    serialized_key = ""
    for a, b in public_key:
        serialized_key += a + b
    key_hash = hashlib.sha256(serialized_key.encode()).hexdigest()
    return key_hash


def create_signature_lamport(text):
    secret_key = generate_secret_key()
    public_key = generate_public_key(secret_key)
    signature = generate_lamport_signature(text, secret_key)
    public_key_hash = get_public_key_hash(public_key)
    return (signature, public_key_hash)

# Функция для создания дерева Меркла
def build_merkle_tree(leaves):
    # Начальный уровень — листья, сохраняем их как первый уровень дерева
    tree_levels = [leaves]
    current_level = leaves

    # Пока не достигнем корня
    while len(current_level) > 1:
        next_level = []

        # Проверка на четное количество элементов
        if len(current_level) % 2 != 0:
            current_level.append(current_level[-1])  # Дублируем последний элемент, если нечетное количество

        # Создаем следующий уровень
        for i in range(0, len(current_level), 2):
            combined_hash = generate_hash(current_level[i] + current_level[i + 1])
            next_level.append(combined_hash)

        # Добавляем следующий уровень в список уровней дерева
        tree_levels.append(next_level)
        current_level = next_level

    # Возвращаем дерево и корневой хэш
    return tree_levels, current_level[0] if current_level else None

def create_public_hash(list_of_signatures):
    return [y for x, y in list_of_signatures]


def get_proof(mercle_tree, index):

    trace = []

    for i in range(len(mercle_tree) - 1 ):
        if index % 2 == 0:
            trace.append((mercle_tree[i][index], mercle_tree[i][index + 1]))
        else:
            trace.append((mercle_tree[i][index - 1], mercle_tree[i][index]))
        index = int(index / 2 )
    trace.append(mercle_tree[-1])
    return trace


def create_signatures(documents):
    list_of_signatures = []

    for i in documents:
        list_of_signatures.append(create_signature_lamport(i))

    return list_of_signatures


# -------------------------
Documents = ["Hello, world!", "Hi!", "Lamport signature", "Mercle Tree","123", " 345", "fdg", "df"]# Список докуметов
list_of_signatures = create_signatures(Documents)
list_of_pbc_hash = create_public_hash(list_of_signatures)  # список публичных ключей подписей

mercle_tree = build_merkle_tree(list_of_pbc_hash)[0]  # Дерево Меркла
root_hash = build_merkle_tree(list_of_pbc_hash)[1]  # Root-hash
proof = get_proof(mercle_tree, 3)

print("List of signatures:", list_of_signatures)
print("List of public hashes:", list_of_pbc_hash)
print("\nMercle Tree:")
for i in mercle_tree:
    print(i)
print("\nRoot Hash: ", root_hash)
print("Proof for document №3: ", proof)
print(generate_hash(proof[-2][0] + proof[-2][1]))
