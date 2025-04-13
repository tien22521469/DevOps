
# Use Eclipse Temurin with full path
FROM eclipse-temurin:21

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY emartapp/javaapi/target/book-work-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
