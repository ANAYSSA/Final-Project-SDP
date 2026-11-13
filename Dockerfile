FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B verify
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S restaurant && adduser -S restaurant -G restaurant
WORKDIR /app
COPY --from=build /workspace/target/hearth-and-table-1.0.0.jar app.jar
USER restaurant
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=65","-XX:+UseSerialGC","-jar","app.jar"]
