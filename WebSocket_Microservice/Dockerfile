FROM maven:3.9-eclipse-temurin-22 AS build

WORKDIR /build

COPY demo/pom.xml .
RUN mvn -B -q dependency:go-offline

COPY demo/src src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:22-jre

WORKDIR /code

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "/code/app.jar"]
