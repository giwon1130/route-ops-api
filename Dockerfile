FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
COPY src src

RUN chmod +x gradlew && ./gradlew bootJar

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
