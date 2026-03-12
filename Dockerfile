FROM eclipse-temurin:17-jre-focal

# Установка локали (опционально)
ENV LANG=C.UTF-8

# Рабочая директория
WORKDIR /app

# Копируем JAR
COPY target/*.jar app.jar

# Открываем порт
EXPOSE 8080

# Запуск приложения
ENTRYPOINT ["java", "-jar", "app.jar"]