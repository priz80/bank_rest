#!/bin/bash

# Примеры всех API-запросов для curl (Bash)
# Назначение: тестирование всех эндпоинтов API

# 🔐 1. Аутентификация

# Вход (получение JWT токена)
# Замените admin/admin на реальные учетные данные
echo "=== Вход в систему ==="
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}')

# Извлекаем токен
TOKEN=$(echo $RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "❌ Ошибка авторизации: неверные логин или пароль"
  echo "$RESPONSE"
  exit 1
fi

echo "✅ Авторизация успешна"
echo "Получен токен: $TOKEN"

echo

# 🃏 2. Карты (USER и ADMIN)

# Установите токен (если не используете автоматическое извлечение)
# TOKEN="ваш_jwt_токен_из_ответа_выше"

# Получить свои карты (с пагинацией)
echo "=== Получить свои карты ==="
curl -s -X GET "http://localhost:8080/api/cards?page=0&size=10&sort=id,asc" \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo

# Создать новую карту
echo "=== Создать новую карту ==="
curl -s -X POST http://localhost:8080/api/cards \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}' | jq '.'

echo

# Получить карту по ID (пример для ID=1)
echo "=== Получить карту по ID ==="
curl -s -X GET http://localhost:8080/api/cards/1 \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo

# Заблокировать карту (пример для ID=1)
echo "=== Заблокировать карту ==="
curl -s -X POST http://localhost:8080/api/cards/1/block \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo

# Активировать карту (пример для ID=1)
echo "=== Активировать карту ==="
curl -s -X POST http://localhost:8080/api/cards/1/activate \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo

# Перевод средств между своими картами
echo "=== Перевод средств между своими картами ==="
curl -s -X POST http://localhost:8080/api/cards/transfers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fromCardId": 1,
    "toCardId": 2,
    "amount": 100.00
  }' | jq '.'

echo

# 👮 3. Администрирование (только ADMIN)

# Получить всех пользователей
echo "=== Получить всех пользователей ==="
curl -s -X GET "http://localhost:8080/api/admin/users?page=0&size=10&sort=id,asc" \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo

# Получить пользователя по ID (пример для ID=1)
echo "=== Получить пользователя по ID ==="
curl -s -X GET http://localhost:8080/api/admin/users/1 \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo

# Создать нового пользователя
echo "=== Создать нового пользователя ==="
curl -s -X POST http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "secret123"
  }' | jq '.'

echo

# Изменить статус пользователя (пример для ID=1)
echo "=== Изменить статус пользователя ==="
curl -s -X PUT http://localhost:8080/api/admin/users/1/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '"BLOCKED"' | jq '.'

echo

# Удалить пользователя (пример для ID=1)
echo "=== Удалить пользователя ==="
curl -s -X DELETE http://localhost:8080/api/admin/users/1 \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nHTTP Status: %{http_code}\n"

echo

# Получить все карты системы
echo "=== Получить все карты системы ==="
curl -s -X GET "http://localhost:8080/api/admin/cards?page=0&size=10&sort=id,desc" \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo

# Заблокировать карту администратором (пример для ID=1)
echo "=== Заблокировать карту администратором ==="
curl -s -X POST http://localhost:8080/api/admin/cards/1/block \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo

# Активировать карту администратором (пример для ID=1)
echo "=== Активировать карту администратором ==="
curl -s -X POST http://localhost:8080/api/admin/cards/1/activate \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo

# Перевод средств между любыми картами (администратор)
echo "=== Перевод средств между любыми картами (администратор) ==="
curl -s -X POST http://localhost:8080/api/admin/cards/transfers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fromCardId": 1,
    "toCardId": 2,
    "amount": 100.00
  }' | jq '.'

echo

# Получить карту по ID (администратор) (пример для ID=1)
echo "=== Получить карту по ID (администратор) ==="
curl -s -X GET http://localhost:8080/api/admin/cards/1 \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo

# Удалить карту по ID (администратор) (пример для ID=1)
echo "=== Удалить карту по ID (администратор) ==="
curl -s -X DELETE http://localhost:8080/api/admin/cards/1 \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nHTTP Status: %{http_code}\n"

# 📌 Примечания

# - Для USER-роли некоторые эндпоинты вернут 403 Forbidden
# - Убедитесь, что приложение запущено: docker-compose up
# - Требуется jq для красивого вывода JSON (установка: sudo apt install jq или brew install jq)
