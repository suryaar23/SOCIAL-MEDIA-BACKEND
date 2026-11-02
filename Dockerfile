# Stage 1: Build
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

COPY aiven/aiven-ca.pem /app/aiven-ca.pem

RUN keytool -importcert \
    -alias aiven-ca \
    -file /app/aiven-ca.pem \
    -keystore /app/aiven-truststore.jks \
    -storepass changeit \
    -noprompt


# Stage 2: Runtime
FROM eclipse-temurin:25-jre AS runtime

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

COPY --from=build /app/aiven-truststore.jks /app/aiven-truststore.jks

ENTRYPOINT ["java", "-jar", "app.jar"]
