# Build
FROM eclipse-temurin:25-jdk-noble AS build

WORKDIR /workspace

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY .editorconfig ./

RUN chmod +x gradlew

COPY src ./src

RUN ./gradlew --no-daemon bootJar

# Run
FROM eclipse-temurin:25-jre-noble AS runtime

WORKDIR /app

RUN groupadd --gid 10001 app && useradd --uid 10001 --gid app --no-create-home --shell /usr/sbin/nologin app

COPY --from=build /workspace/build/libs/app.jar /app/app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]