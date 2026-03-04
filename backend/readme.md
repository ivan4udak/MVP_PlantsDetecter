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
```
