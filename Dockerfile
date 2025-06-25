FROM openjdk:11-jre-slim AS build

# Set the working directory
WORKDIR /app

# Copy the Gradle build files
COPY backend/build.gradle.kts backend/settings.gradle.kts ./

# Copy the source code
COPY backend/src ./backend/src

# Copy the Python script
COPY python/pop_general.py ./python/pop_general.py

# Install Gradle
RUN apt-get update && \
    apt-get install -y gradle && \
    gradle build -x test

# Use a smaller image for the final stage
FROM openjdk:11-jre-slim

# Set the working directory
WORKDIR /app

# Copy the built application from the build stage
COPY --from=build /app/backend/build/libs/*.jar app.jar

# Copy the Python script
COPY --from=build /app/python/pop_general.py ./python/pop_general.py

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]