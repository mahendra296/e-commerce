# ---------- Stage 1: Build ----------
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Install common-service to local Maven repo
COPY common-service /app/common-service
RUN cd /app/common-service && mvn clean install -DskipTests

# Copy service pom and resolve dependencies
WORKDIR /app/service
COPY customer-service/pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY customer-service/src ./src
RUN mvn clean package -DskipTests

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
