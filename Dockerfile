# Use GraalVM image as base
FROM ghcr.io/graalvm/jdk-community:23

# Set environment variable to enable JavaScript
ENV LANGUAGES="js,python"

# Set up the working directory
WORKDIR /app

COPY ./rate-distribution-engine/build/libs/rate-distribution-engine-0.0.1.jar RateDistributionEngine.jar

# Run the Java program
CMD ["java", "-jar", "RateDistributionEngine.jar"]
