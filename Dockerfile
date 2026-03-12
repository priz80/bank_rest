# Stage 1: Сборка с помощью Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Копируем только pom.xml сначала (для кэширования зависимостей)
COPY pom.xml .

# Загружаем зависимости
RUN mvn dependency:go-offline

# Копируем исходники
COPY src ./src

# Собираем JAR
RUN mvn clean package -DskipTests

# Stage 2: Запуск приложения
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Копируем собранный JAR из stage 1
COPY --from=builder /app/target/bankcards-1.0.0.jar app.jar

EXPOSE 8080

# Запуск
CMD ["java", "-jar", "app.jar"]