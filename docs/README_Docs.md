# Документация API
## Проект: Управление банковскими картами
#### Версия: 1.0.0

---

Базовый URL: (http://localhost:8080)

---

### 📘 Содержание
#### Аутентификация
#### Карты (USER и ADMIN)
#### Администрирование (только ADMIN)
#### Формат ответов
#### Коды ошибок
#### Авторизация

---

### 🔐 Аутентификация

```POST /api/auth/login```

#### Аутентифицирует пользователя и возвращает JWT токен.

### Запрос

JSON
{
  "username": "string",
  "password": "string"
}

#### Ответ (200 OK)

JSON
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.x...",
  "userId": 1,
  "username": "ivan",
  "role": "USER"
}

#### Ошибки

401 Unauthorized — Неверные логин или пароль



# 🃏 Карты (доступно для USER и ADMIN)
GET /api/cards
Получить свои карты с пагинацией.

Параметры
Параметр	Тип	По умолчанию	Описание
page	integer	0	Номер страницы
size	integer	10	Размер страницы
sort	string	id,asc	Поле и направление: id,desc, expiryDate,asc
Ответ (200 OK)
JSON
{
  "content": [
    {
      "id": 1,
      "cardNumber": "1234 **** **** 5678",
      "cardHolderName": "IVAN PETROV",
      "expiryDate": "2029-01-01",
      "status": "ACTIVE",
      "balance": 5000.00,
      "userId": 1
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
POST /api/cards
Создать новую карту для текущего пользователя.


⚠️ Только владелец или ADMIN может создать.


Запрос
JSON
{
  "userId": 1
}
Ответ (200 OK)
JSON
{
  "id": 2,
  "cardNumber": "4123567890123456",
  "cardHolderName": "IVAN PETROV",
  "cvv": "123",
  "expiryDate": "2029-01-01",
  "status": "ACTIVE",
  "balance": 0.00,
  "userId": 1
}
GET /api/cards/{id}
Получить карту по ID.

Ответ (200 OK)
JSON
{
  "id": 1,
  "cardNumber": "1234 **** **** 5678",
  "cardHolderName": "IVAN PETROV",
  "expiryDate": "2029-01-01",
  "status": "ACTIVE",
  "balance": 5000.00,
  "userId": 1
}
Ошибки
404 Not Found — Карта не найдена или не принадлежит пользователю
403 Forbidden — Доступ запрещён
POST /api/cards/{id}/block
Заблокировать карту.

Ответ (200 OK)
JSON
{
  "id": 1,
  "status": "BLOCKED",
  "balance": 5000.00
}
Ошибки
400 Bad Request — Карта уже заблокирована
404 Not Found — Карта не найдена
POST /api/cards/{id}/activate
Активировать карту.

Ответ (200 OK)
JSON
{
  "id": 1,
  "status": "ACTIVE",
  "balance": 5000.00
}
Ошибки
400 Bad Request — Карта уже активна или просрочена
404 Not Found — Карта не найдена
DELETE /api/cards/{id}
Удалить карту (только если статус ≠ ACTIVE и баланс = 0).

Ответ (204 No Content)
Ошибки
400 Bad Request — Карта активна или на ней есть средства
404 Not Found — Карта не найдена
POST /api/cards/transfers
Перевод средств между своими картами.

Запрос
JSON
{
  "fromCardId": 1,
  "toCardId": 2,
  "amount": 100.00
}
Ответ (200 OK) — пустой
Ошибки
400 Bad Request — Недостаточно средств, карта не найдена, сумма ≤ 0
403 Forbidden — Карты не принадлежат пользователю
👮 Администрирование (только ADMIN)
GET /api/admin/users
Получить всех пользователей.

Параметры
Параметр	Тип	По умолчанию	Описание
page	integer	0	Страница
size	integer	10	Размер
sort	string	id,asc	Поле и направление
Ответ (200 OK)
JSON
{
  "content": [
    {
      "id": 1,
      "username": "ivan",
      "role": "USER",
      "status": "ACTIVE"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
GET /api/admin/users/{id}
Получить пользователя по ID.

Ответ (200 OK)
JSON
{
  "id": 1,
  "username": "ivan",
  "role": "USER",
  "status": "ACTIVE"
}
Ошибки
404 Not Found — Пользователь не найден
POST /api/admin/users
Создать нового пользователя.

Запрос
JSON
{
  "username": "anna",
  "password": "secret123"
}
Ответ (200 OK)
JSON
{
  "id": 2,
  "username": "anna",
  "role": "USER",
  "status": "ACTIVE"
}
Ошибки
400 Bad Request — Пользователь с таким именем уже существует
PUT /api/admin/users/{id}/status
Изменить статус пользователя.

Параметры
Параметр	Описание
status	ACTIVE, BLOCKED, DELETED
Ответ (200 OK)
JSON
{
  "id": 1,
  "status": "BLOCKED"
}
Ошибки
400 Bad Request — Некорректный статус
404 Not Found — Пользователь не найден
DELETE /api/admin/users/{id}
Удалить пользователя из БД.

Условия:
Статус должен быть DELETED
У пользователя не должно быть карт
Ответ (204 No Content)
Ошибки
400 Bad Request — Пользователь не в статусе DELETED или есть карты
404 Not Found — Пользователь не найден
GET /api/admin/cards
Получить все карты системы.

Параметры
Параметр	Тип	По умолчанию	Описание
page	integer	0	Страница
size	integer	10	Размер
sort	string	id,asc	Поле и направление
Ответ (200 OK)
JSON
{
  "content": [
    {
      "id": 1,
      "cardNumber": "1234 **** **** 5678",
      "cardHolderName": "IVAN PETROV",
      "expiryDate": "2029-01-01",
      "status": "ACTIVE",
      "balance": 5000.00,
      "userId": 1
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
🧩 Формат ответов
Пагинация
Все списковые ответы используют формат Page<T>:

JSON
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 10,
  "number": 0,
  "size": 10
}
❌ Коды ошибок
Код	Описание
400 Bad Request	Некорректные данные, бизнес-ограничение
401 Unauthorized	Необходима аутентификация
403 Forbidden	Доступ запрещён (не та роль)
404 Not Found	Ресурс не найден
500 Internal Server Error	Внутренняя ошибка сервера
🔐 Авторизация
Все эндпоинты (кроме /api/auth/login) требуют заголовка:

Authorization: Bearer <ваш_jwt_токен>
Токен получается при входе.

📘 Swagger UI
Полная интерактивная документация доступна по адресу:

👉 http://localhost:8080/swagger-ui.html

✅ API полностью протестировано, безопасно, готово к интеграции.

📅 Обновлено: 03.2026г.
