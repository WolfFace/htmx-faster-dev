FROM clojure:temurin-23-tools-deps-1.12.0.1501-noble AS builder

RUN apt-get update && apt-get install -y \
  python3 python3-pip \
  fonts-liberation libappindicator3-1 libasound2t64 libatk-bridge2.0-0 \
  libnspr4 libnss3 lsb-release xdg-utils libxss1 libdbus-glib-1-2 \
  curl unzip wget jq \
  xvfb

RUN SERVO_SETUP=servo-setup.tar.xz && \
  wget -O $SERVO_SETUP "https://download.servo.org/nightly/linux/servo-latest.tar.gz" && \
  tar xvf $SERVO_SETUP -C /opt/ && \
  ln -s /opt/servo/servo /usr/bin/servo && \
  rm $SERVO_SETUP

WORKDIR /opt/project

COPY . .
COPY servo-script.sh /usr/bin/servo-script.sh

RUN clojure -T:build ci

FROM eclipse-temurin:23-jre-ubi9-minimal AS runtime
COPY --from=builder /opt/target/net.clojars.htmx-faster/core-0.1.0-SNAPSHOT.jar /app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
