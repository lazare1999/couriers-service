# syntax=docker/dockerfile:1.0-experimental
FROM gradle:7.2-jdk17 as builder

WORKDIR /app
COPY . .

RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
      /usr/bin/gradle -Dorg.gradle.daemon=false \
      bootWar


FROM openjdk:17-slim
WORKDIR /app
ENV TZ="Asia/Tbilisi"
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt-get update && apt-get install -y --no-install-recommends wget && apt-get clean all
#HEALTHCHECK --interval=20s --timeout=5s --start-period=20s --retries=5 CMD wget -q http://127.0.0.1:8080/ping -O /dev/null || exit 1
CMD ["java", "--illegal-access=warn", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.war"]
USER 1000:1000

COPY --from=builder /app/build/libs/*.war /app/app.war
