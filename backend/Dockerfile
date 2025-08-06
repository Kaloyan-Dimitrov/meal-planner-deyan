FROM gradle:8.14.2-jdk17 AS builder
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
COPY src src
COPY build/generated-src/jooq build/generated-src/jooq
ARG DB_URL
ENV DB_URL=${DB_URL}
ENV SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}
RUN ./gradlew bootJar --no-daemon -x test -x flywayMigrate -x generateJooq

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]