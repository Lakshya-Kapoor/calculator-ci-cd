# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Copy the POM alone first so the dependency layer stays cached
# as long as pom.xml is unchanged, even when src/ changes.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Run as an unprivileged user instead of root
RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build --chown=spring:spring /build/target/*.jar app.jar

USER spring

EXPOSE 5000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
