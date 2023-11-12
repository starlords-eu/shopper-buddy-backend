# Use an official Java 17 runtime as a parent image
FROM openjdk:17-jre-slim

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Set the working directory in the container
WORKDIR /app

# Copy the jar file into the container
ARG JAR_FILE=target/shopper-buddy.jar
COPY ${JAR_FILE} shopper-buddy.jar

# Run the jar file
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/shopper-buddy.jar"]