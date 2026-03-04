# ============================================
# STAGE 1: BUILD
# Используем Maven образ для сборки проекта
# ============================================
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

# Рабочая директория внутри контейнера
WORKDIR /app

# Сначала копируем только pom.xml
# Docker кэширует слои — если pom.xml не изменился,
# зависимости не будут скачиваться заново
COPY pom.xml .

# Скачиваем зависимости (отдельный слой для кэша)
RUN mvn dependency:go-offline -B

# Теперь копируем исходный код
COPY src ./src

# Собираем JAR файл, пропускаем тесты (тесты в CI)
RUN mvn package -DskipTests

# ============================================
# STAGE 2: RUNTIME
# Используем минимальный образ только для запуска
# builder образ ~500MB, runtime ~150MB
# ============================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Создаём пользователя (не запускаем от root — безопасность!)
RUN addgroup -S plantapp && adduser -S plantapp -G plantapp

# Копируем только JAR из builder стейджа
COPY --from=builder /app/target/*.jar app.jar

# Меняем владельца файла
RUN chown plantapp:plantapp app.jar

# Переключаемся на non-root пользователя
USER plantapp

# Порт который слушает приложение
EXPOSE 8080

# Команда запуска
# -Djava.security.egd — ускоряет старт в контейнере
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]