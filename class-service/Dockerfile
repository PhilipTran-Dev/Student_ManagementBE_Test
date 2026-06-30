FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY pom.xml .

COPY class-service/pom.xml ./class-service/
RUN mvn -f class-service/pom.xml dependency:go-offline

COPY class-service/src ./class-service/src
RUN mvn -f class-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/class-service/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]