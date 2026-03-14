# Примеры всех API-запросов для curl (Bash)

Ниже — готовые команды curl для тестирования всех эндпоинтов API.

## 🔐 1. Аутентификация

### Вход (получение JWT токена)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }'
```

Сохраните токен из ответа, например:

```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.x..."
```

## 🃏 2. Карты (USER и ADMIN)

Установите токен:

```bash
TOKEN="ваш_jwt_токен_из_ответа_выше"
```

### Получить свои карты (с пагинацией)

```bash
curl -X GET "http://localhost:8080/api/cards?page=0&size=10&sort=id,asc" \
  -H "Authorization: Bearer $TOKEN"
```

### Создать новую карту

```bash
curl -X POST http://localhost:8080/api/cards \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1
  }'
```

### Получить карту по ID

```bash
curl -X GET http://localhost:8080/api/cards/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Заблокировать карту

```bash
curl -X POST http://localhost:8080/api/cards/1/block \
  -H "Authorization: Bearer $TOKEN"
```

### Активировать карту

```bash
curl -X POST http://localhost:8080/api/cards/1/activate \
  -H "Authorization: Bearer $TOKEN"
```

### Перевод средств между своими картами

```bash
curl -X POST http://localhost:8080/api/cards/transfers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fromCardId": 1,
    "toCardId": 2,
    "amount": 100.00
  }'
```

## 👮 3. Администрирование (только ADMIN)

### Получить всех пользователей

```bash
curl -X GET "http://localhost:8080/api/admin/users?page=0&size=10&sort=id,asc" \
  -H "Authorization: Bearer $TOKEN"
```

### Получить пользователя по ID

```bash
curl -X GET http://localhost:8080/api/admin/users/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Создать нового пользователя

```bash
curl -X POST http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "secret123"
  }'
```

### Изменить статус пользователя

```bash
curl -X PUT http://localhost:8080/api/admin/users/1/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '"BLOCKED"'
```

### Удалить пользователя

```bash
curl -X DELETE http://localhost:8080/api/admin/users/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Получить все карты системы

```bash
curl -X GET "http://localhost:8080/api/admin/cards?page=0&size=10&sort=id,desc" \
  -H "Authorization: Bearer $TOKEN"
```

### Заблокировать карту администратором

```bash
curl -X POST http://localhost:8080/api/admin/cards/1/block \
  -H "Authorization: Bearer $TOKEN"
```

### Активировать карту администратором

```bash
curl -X POST http://localhost:8080/api/admin/cards/1/activate \
  -H "Authorization: Bearer $TOKEN"
```

### Перевод средств между любыми картами (администратор)

```bash
curl -X POST http://localhost:8080/api/admin/cards/transfers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fromCardId": 1,
    "toCardId": 2,
    "amount": 100.00
  }'
```

### Получить карту по ID (администратор)

```bash
curl -X GET http://localhost:8080/api/admin/cards/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Удалить карту по ID (администратор)

```bash
curl -X DELETE http://localhost:8080/api/admin/cards/1 \
  -H "Authorization: Bearer $TOKEN"
```

## 🧪 Полный пример скрипта (api-test.sh)

```bash
#!/bin/bash

echo "=== Вход в систему ==="
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}')

TOKEN=$(echo $RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "❌ Ошибка авторизации"
  exit 1
fi

echo "✅ Авторизация успешна"

echo "=== Получить свои карты ==="
curl -s -X GET "http://localhost:8080/api/cards" \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo "=== Получить всех пользователей ==="
curl -s -X GET "http://localhost:8080/api/admin/users" \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo "=== Получить все карты ==="
curl -s -X GET "http://localhost:8080/api/admin/cards" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
```

> ⚠️ Требуется `jq` для красивого вывода JSON:
>
> Установка: `sudo apt install jq` (Linux) / `brew install jq` (macOS)

## ✅ Как использовать

- Сохраните команды в файл `api-examples.sh`
- Сделайте исполняемым:
  ```bash
  chmod +x api-examples.sh
  ```
- Запустите:
  ```bash
  ./api-examples.sh
  ```

## 📌 Примечания

- Замените `admin / admin` на реальные учётные данные
- Убедитесь, что приложение запущено: `docker-compose up`
- Все запросы требуют валидный JWT токен
- Для USER-роли некоторые эндпоинты вернут 403 Forbidden
