# ==========================================
# GIAI ĐOẠN 1: BIÊN DỊCH ỨNG DỤNG (BUILD)
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy gradle wrapper và các file cấu hình build trước để tận dụng Docker Cache layer
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts gradle.properties ./

# Fix kết thúc dòng CRLF (nếu có trên Windows) và cấp quyền thực thi
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN sed -i '/org.gradle.java.home/d' gradle.properties || true

# Tải trước dependencies (nếu không đổi file gradle thì bước này được cache)
RUN ./gradlew dependencies --no-daemon || true

# Copy toàn bộ mã nguồn
COPY src ./src

# Build file .jar thực thi, bỏ qua unit test để tăng tốc độ build
RUN ./gradlew bootJar --no-daemon -x test

# ==========================================
# GIAI ĐOẠN 2: CHẠY ỨNG DỤNG (RUNTIME)
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Tạo user không có quyền root để đảm bảo an toàn bảo mật
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy file .jar đã build từ Giai đoạn 1 sang
COPY --from=builder /app/build/libs/*.jar app.jar

# Phân quyền cho appuser
RUN chown -R appuser:appgroup /app
USER appuser

# Khai báo cổng ứng dụng
EXPOSE 8081

# Cấu hình tối ưu bộ nhớ RAM cho JVM
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
