# 🌿 Plant Identifier Backend

Enterprise mobile backend для определения растений по фото.

## Технологии
- Java 17 + Spring Boot 3.2
- PostgreSQL 16 (3 схемы: app / analytics / audit)
- Docker + Docker Compose
- JWT Authentication
- Flyway Migrations

## Быстрый старт

### 1. Клонируй репозиторий
```bash
git clone git@github.com:ivan4udak/MVP_PlantsDetecter.git
cd MVP_PlantsDetecter
```

### 2. Настрой переменные окружения
```bash
cp .env.example .env
# Отредактируй .env — заполни пароли и секреты
```

### 3. Запусти всё одной командой
```bash
docker-compose up -d
```

### 4. Проверь что работает
```bash
# Статус контейнеров
docker-compose ps

# Health check
curl http://localhost:8080/api/v1/system/health

# pgAdmin UI
open http://localhost:5050
```

## Структура БД

| Схема | Назначение |
|-------|-----------|
| `app` | Основные таблицы (users, plant_requests) |
| `analytics` | Бизнес-метрики (ai_usage_stats, rate_limit_log) |
| `audit` | Иммутабельный аудит (только триггеры пишут) |

## API

Базовый URL: `http://localhost:8080/api/v1`

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/session/guest` | Создать гостевую сессию |
| POST | `/auth/register` | Регистрация |
| POST | `/auth/login` | Вход |
| POST | `/plants/analyze` | Анализ растения |
| GET | `/plants/history` | История запросов |

## Команды разработки
```bash
# Посмотреть логи
docker-compose logs -f backend

# Пересобрать после изменений
docker-compose up -d --build backend

# Зайти в БД
docker exec -it plant-postgres psql -U plant_user -d plant_identifier_db

# Полный сброс
docker-compose down -v

Ниже — аккуратно оформленная **Markdown-документация API**, которую можно прямо отправить фронту или положить в `README.md`.

---

# Plant Identifier API

**Base URL**

```
http://localhost:8080/api/v1
```

Все ответы и запросы используют формат **JSON**.

---

# Аутентификация

Для защищённых эндпойнтов требуется заголовок:

```
Authorization: Bearer <access_token>
```

Access token — JWT, получаемый при логине или создании гостевой сессии.

---

# Роли пользователей

| Роль        | Описание                        |
| ----------- | ------------------------------- |
| ROLE_GUEST  | Гостевая сессия                 |
| ROLE_USER   | Зарегистрированный пользователь |
| ROLE_ADMIN  | Администратор                   |
| ROLE_SYSTEM | Системная роль                  |

---

# Формат ошибок

```json
{
  "timestamp": "2026-03-09T19:00:00",
  "requestId": "uuid",
  "errorCode": "VALIDATION_ERROR",
  "message": "Ошибка валидации",
  "details": {
    "field": "Описание ошибки"
  }
}
```

| Поле      | Описание      |
| --------- | ------------- |
| timestamp | время ошибки  |
| requestId | ID запроса    |
| errorCode | код ошибки    |
| message   | сообщение     |
| details   | детали ошибки |

---

# System API

## Health check

### GET `/system/health`

Проверка доступности сервиса.

### Response

```json
{
  "status": "UP",
  "timestamp": "2026-03-09T19:00:00",
  "service": "plant-identifier"
}
```

---

## Deep health

### GET `/system/health/deep`

Требует **ADMIN**.

Проверяет:

* базу данных
* AI provider

### Response

```json
{
  "timestamp": "2026-03-09T19:08:38",
  "database": {
    "status": "UP"
  },
  "aiProvider": {
    "status": "UP",
    "provider": "openai",
    "model": "gpt-4.1-mini"
  },
  "status": "UP"
}
```

---

# Auth API

## Регистрация

### POST `/auth/register`

### Request

```json
{
  "email": "user@example.com",
  "password": "test1234",
  "language": "ru"
}
```

### Response

```json
{
  "userId": "4930bb47-29b6-45d5-9e4d-60f934f65e46",
  "createdDate": "2026-03-09T18:45:28"
}
```

---

## Login

### POST `/auth/login`

### Request

```json
{
  "email": "user@example.com",
  "password": "test1234"
}
```

### Response

```json
{
  "accessToken": "jwt",
  "refreshToken": "jwt",
  "expiresIn": 900000,
  "role": "ROLE_USER",
  "language": "ru"
}
```

---

## Refresh token

### POST `/auth/refresh`

### Request

```json
{
  "refreshToken": "jwt"
}
```

### Response

```json
{
  "accessToken": "jwt",
  "expiresIn": 900000
}
```

---

## Upgrade guest → user

### POST `/auth/upgrade`

Требует **guest token**.

### Request

```json
{
  "email": "user@example.com",
  "password": "test1234"
}
```

### Response

```
200 OK
```

---

# Session API

## Создание гостевой сессии

### POST `/session/guest`

### Request

```json
{
  "deviceId": "device-123",
  "preferredLanguage": "ru"
}
```

### Response

```json
{
  "accessToken": "jwt",
  "refreshToken": "jwt",
  "userId": "uuid",
  "role": "ROLE_GUEST",
  "language": "ru",
  "expiresIn": 900000,
  "limitPerDay": 5
}
```

---

# User API

## Обновление языка

### PATCH `/users/language`

### Headers

```
Authorization: Bearer token
```

### Request

```json
{
  "preferredLanguage": "ru"
}
```

### Response

```
204 No Content
```

---

# Plant API

## Анализ растения

### POST `/plants/analyze`

### Headers

```
Authorization: Bearer token
```

### Request

```json
{
  "imageUrl": "https://example.com/plant.jpg",
  "latitude": 55.75,
  "longitude": 37.61
}
```

### Response

```json
{
  "requestId": "uuid",
  "isPlant": true,
  "confidence": 0.94,
  "primaryResult": {
    "name": "Rose",
    "latinName": "Rosa",
    "family": "Rosaceae",
    "rarity": "common",
    "habitat": "gardens",
    "facts": [
      "Fact 1",
      "Fact 2"
    ]
  },
  "alternatives": [
    {
      "name": "Peony",
      "confidence": 0.37
    }
  ],
  "modelInfo": {
    "provider": "openai",
    "model": "gpt-4.1-mini"
  },
  "processingTimeMs": 1342
}
```

---

## История анализов

### GET `/plants/history`

### Query

```
?page=0
&size=20
```

### Response

```json
{
  "content": [
    {
      "requestId": "uuid",
      "primaryName": "Rose",
      "isPlant": true,
      "confidence": 0.94,
      "aiProvider": "openai",
      "createdDate": "2026-03-09T19:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

---

## Получение результата анализа

### GET `/plants/{id}`

### Response

```json
{
  "requestId": "uuid",
  "isPlant": true,
  "confidence": 0.94,
  "primaryResult": {
    "name": "Rose",
    "latinName": "Rosa",
    "family": "Rosaceae",
    "rarity": "common",
    "habitat": "gardens"
  },
  "alternatives": [],
  "modelInfo": {
    "provider": "openai",
    "model": "gpt-4.1-mini"
  },
  "processingTimeMs": 1342
}
```

---

## Удаление анализа

### DELETE `/plants/{id}`

### Response

```
204 No Content
```

---

# Admin API

Требует **ROLE_ADMIN**.

---

## Usage statistics

### GET `/admin/stats/usage`

### Response

```json
{
  "todayRequests": 42,
  "timestamp": "2026-03-09T19:00:00"
}
```

---

## AI cost

### GET `/admin/stats/ai-cost`

### Response

```json
{
  "costUSD": 12.34,
  "since": "2026-03-01T00:00:00",
  "timestamp": "2026-03-09T19:00:00"
}
```

---

## Проверка rate limit

### GET `/admin/rate-limit/{userId}`

### Response

```json
{
  "userId": "uuid",
  "remaining": 3,
  "unlimited": false
}
```

---

## Сброс rate limit

### POST `/admin/rate-limit/reset/{userId}`

### Response

```
200 OK
```

---

## Ошибки системы

### GET `/admin/errors`

### Response

```json
{
  "errors": [
    {
      "id": "uuid",
      "requestId": "uuid",
      "errorCode": "INTERNAL_ERROR",
      "message": "Some error",
      "stackTrace": "...",
      "createdDate": "2026-03-09T19:00:00"
    }
  ],
  "total": 12,
  "timestamp": "2026-03-09T19:00:00"
}
```

---

# Коды статусов

| Код | Значение              |
| --- | --------------------- |
| 200 | OK                    |
| 201 | Created               |
| 204 | No Content            |
| 400 | Bad Request           |
| 401 | Unauthorized          |
| 403 | Forbidden             |
| 404 | Not Found             |
| 429 | Too Many Requests     |
| 500 | Internal Server Error |
| 503 | Service Unavailable   |

---

# Валидация

### Email

Должен быть валидным email.

---

### Password

Требования:

* минимум 8 символов
* минимум 1 буква
* минимум 1 цифра

---

### Language

Формат:

```
ru
en
de
zh-CN
```

Regex:

```
^[a-z]{2}(-[A-Z]{2})?$
```

---

# Архитектура запросов

```
Client
   │
   ▼
Spring Boot API
   │
   ├── PostgreSQL
   │
   └── OpenAI API
```


```
