FROM clojure:temurin-23-tools-deps-1.12.0.1501-noble AS builder

WORKDIR /opt

COPY . .

RUN clojure -T:build ci

FROM eclipse-temurin:23-jre-ubi9-minimal AS runtime
COPY --from=builder /opt/target/net.clojars.htmx-faster/core-0.1.0-SNAPSHOT.jar /app.jar
COPY --from=builder /opt/.images /.images

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
