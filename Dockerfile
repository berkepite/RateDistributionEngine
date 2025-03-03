# Use GraalVM image as base
FROM ghcr.io/graalvm/jdk-community:23

# Set environment variable to enable JavaScript
ENV LANGUAGES="js,python"
ENV SPRING_PROFILES_ACTIVE="prod"

# Set up the working directory
WORKDIR /app

# Copy the vendor folder containing dependencies (decimal.js and others)
COPY ./vendor /app/vendor
COPY ./build/libs/MainApplication32Bit-0.0.1-SNAPSHOT.jar /app/MainApplication32Bit.jar

# Run the Java program
CMD ["java", "-jar", "/app/MainApplication32Bit.jar"]
