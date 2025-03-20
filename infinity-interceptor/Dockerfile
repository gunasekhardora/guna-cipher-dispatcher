FROM maven:3.9.6-eclipse-temurin-17 as builder
WORKDIR /app

COPY . .

RUN mvn clean package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=builder /app/infinity-interceptor/target/infinity-interceptor-*.jar app.jar
COPY --from=builder /app/infinity-interceptor/target/lib/ /app/lib/

EXPOSE 8081

ENTRYPOINT ["java", "-cp", "app.jar:/app/lib/*", "org.guna.cipher.interceptor.application.InfinityInterceptorApplication"]