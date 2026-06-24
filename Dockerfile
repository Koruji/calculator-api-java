# Use OpenJDK 17 as base image
FROM eclipse-temurin:17-jdk-jammy

# Set working directory
WORKDIR /app

# Copy source files
COPY src/main/java/core/calculator/ .

# Compile Java files
RUN mkdir -p out && javac -d out core/calculator/*.java

# Expose port (default: 3000)
EXPOSE 3000

# Start the server
ENTRYPOINT ["java", "-cp", "out", "core.calculator.Server"]
CMD ["3000"]
