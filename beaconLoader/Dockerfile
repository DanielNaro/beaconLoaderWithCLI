# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml first for better caching
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Download dependencies (cached if pom.xml hasn't changed)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Stage 2: Run the application
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Create a directory for data files
RUN mkdir -p /data

ENV BEACON_DATA_PATH=/data

# Expose port if needed (optional, for any web endpoints)
EXPOSE 8080

# Entrypoint script to allow passing the data path
ENTRYPOINT ["java", "-jar", "app.jar"]

# Default command arguments (can be overridden)
CMD ["--beacon.data.path=/data"]
