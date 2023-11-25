# Use an official Java 17 runtime as a parent image
FROM eclipse-temurin:17-jre-ubi9-minimal
ARG JAR_FILE

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Set the working directory in the container
WORKDIR /app

# Copy the jar file into the container
COPY ${JAR_FILE} ./shopper-buddy.jar

# Run the jar file
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/shopper-buddy.jar"]
