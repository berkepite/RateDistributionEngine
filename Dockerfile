# Use GraalVM image as base
FROM ghcr.io/graalvm/jdk-community:23

# Set environment variable to enable JavaScript
ENV LANGUAGES="js,python"

# Set up the working directory
WORKDIR /app

# Copy the vendor folder containing dependencies (decimal.js and others)
COPY ./vendor vendor
COPY ./build/libs/MainApplication32Bit-0.0.1-SNAPSHOT.jar MainApplication32Bit.jar

# Run the Java program
CMD ["java", "-jar", "MainApplication32Bit.jar"]
