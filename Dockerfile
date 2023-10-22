FROM gradle:8.2.1-jdk17 AS builder-stage
WORKDIR /app
COPY settings.gradle.kts build.gradle.kts  ./
COPY src ./src/
RUN gradle build -x test --no-daemon --parallel


FROM ghcr.io/graalvm/jdk-community:17 AS production-stage
ENV ARTIFACT_NAME="nicol-0.1.0.jar"
WORKDIR /app
COPY --from=builder-stage /app/build/libs/$ARTIFACT_NAME .
COPY start.sh .

EXPOSE 9005
CMD ["sh", "-c", "java -jar /app/$ARTIFACT_NAME"]
ENTRYPOINT ["/app/start.sh"]
