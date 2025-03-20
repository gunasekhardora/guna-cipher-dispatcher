FROM maven:3.9.6-eclipse-temurin-17 as builder
WORKDIR /app

COPY . .

RUN mvn clean package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=builder /app/cipher-dispatcher/target/cipher-dispatcher-*.jar app.jar
COPY --from=builder /app/cipher-dispatcher/target/lib/ /app/lib/

EXPOSE 8080

ENTRYPOINT ["java", "-cp", "app.jar:/app/lib/*", "org.guna.cipher.dispatcher.application.CipherDispatcherApplication"]