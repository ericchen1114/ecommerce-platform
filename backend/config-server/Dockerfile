# ─── Stage 1: dependency cache ────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS deps
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# ─── Stage 2: build ───────────────────────────────────────────────────────────
FROM deps AS builder
COPY src ./src
RUN ./mvnw package -DskipTests -q

# ─── Stage 3: runtime（最小 image）───────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# 非 root 使用者執行（安全最佳實踐）
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
