# Use OpenJDK 17 as base image
FROM eclipse-temurin:17-jdk-jammy

# Set working directory
WORKDIR /app

# Copy source files
COPY src/main/java/core/calculator/ core/calculator/

# Download JUnit Platform Console Standalone
RUN wget https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.9.2/junit-platform-console-standalone-1.9.2.jar -O junit.jar

# Copy test files
COPY src/test/java/com/calculator/ com/calculator/

# Compile Java files (main + tests)
RUN mkdir -p out && \
    javac -cp out:junit.jar -d out core/calculator/*.java && \
    javac -cp out:junit.jar -d out com/calculator/*.java

# Expose port (default: 3000)
EXPOSE 3000

# Start the server (run tests first)
ENTRYPOINT ["sh", "-c"]
CMD ["java -jar junit.jar --class-path out --scan-class-path && java -cp out core.calculator.Server 3000"]
