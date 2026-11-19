# HƯỚNG DẪN TOÀN TẬP TRIỂN KHAI DỰ ÁN LÊN VPS VỚI DOCKER & NGINX
> **Dự án:** Food Order System (Frontend: React/Vite - Backend: Spring Boot Java 21 - Database: MySQL)

---

## MỤC LỤC
1. [Lý thuyết nền tảng & Các khái niệm cốt lõi](#1-lý-thuyết-nền-tảng--các-khái-niệm-cốt-lõi)
2. [Kiến trúc triển khai hệ thống (Architecture)](#2-kiến-trúc-triển-khai-hệ-thống-architecture)
3. [Từ điển các câu lệnh Docker & Docker Compose](#3-từ-điển-các-câu-lệnh-docker--docker-compose)
4. [Chuẩn bị file cấu hình Docker trong dự án](#4-chuẩn-bị-file-cấu-hình-docker-trong-dự-án)
5. [Quy trình triển khai thực tế trên VPS (Từng bước chi tiết)](#5-quy-trình-triển-khai-thực-tế-trên-vps-từng-bước-chi-tiết)
6. [Cấu hình Tên miền (Domain) & SSL HTTPS với Let's Encrypt](#6-cấu-hình-tên-miền-domain--ssl-https-với-lets-encrypt)
7. [Tự động hóa triển khai với CI/CD (GitHub Actions)](#7-tự-động-hóa-triển-khai-với-cicd-github-actions)
8. [Các sự cố thường gặp & Cách xử lý (Troubleshooting)](#8-các-sự-cố-thường-gặp--cách-xử-lý-troubleshooting)

---

## 1. LÝ THUYẾT NỀN TẢNG & CÁC KHÁI NIỆM CỐT LÕI

Trước khi gõ lệnh, bạn cần nắm rõ bản chất của các thành phần trong việc vận hành một website trên môi trường mạng internet:

### 1.1. VPS (Virtual Private Server) là gì?
- **VPS** là một máy chủ ảo chạy 24/7 trên trung tâm dữ liệu (Datacenter). Nó có hệ điều hành riêng (thường là Ubuntu Server Linux), CPU, RAM, ổ cứng và một **Địa chỉ IP Public** tĩnh.
- Khác với localhost (chỉ bạn truy cập được trên máy cá nhân), bất kỳ ai có kết nối Internet đều có thể kết nối đến IP Public của VPS thông qua các cổng mở (ports).

### 1.2. IP, Domain, DNS và Port
- **IP Public:** Địa chỉ số định danh VPS (ví dụ: `103.150.12.34`).
- **Domain (Tên miền):** Tên dễ nhớ cho con người (ví dụ: `foodorder.vn`).
- **DNS (Domain Name System):** Hệ thống phân giải tên miền thành địa chỉ IP. 
  - *Bản ghi A (Address Record):* Trỏ trực tiếp tên miền `foodorder.vn` về IP `103.150.12.34`.
- **Port (Cổng mạng):** Giống như số phòng trong một tòa nhà. Một IP có 65.535 cổng:
  - Cổng `22`: SSH (để quản trị VPS từ xa).
  - Cổng `80`: HTTP (web không mã hóa).
  - Cổng `443`: HTTPS (web có chứng chỉ bảo mật SSL).
  - Cổng `3306`: MySQL.
  - Cổng `8081`: Backend Spring Boot.

### 1.3. Firewall (Tường lửa) & Nguyên tắc bảo mật cổng
- **Quy tắc vàng:** Chỉ mở ra ngoài Internet các cổng thật sự cần thiết cho người dùng cuối (`80`, `443`) và cổng quản trị (`22`).
- **Tuyệt đối không public cổng Database (3306) hay cổng Backend trực tiếp ra ngoài**, vì hacker có thể quét IP và tấn công brute-force mật khẩu database của bạn.

### 1.4. Nginx & Mô hình Reverse Proxy
- **Web Server:** Nginx đọc các file HTML/CSS/JS tĩnh đã build từ React Vite và gửi thẳng về cho trình duyệt người dùng với tốc độ cực nhanh.
- **Reverse Proxy (Ủy thác ngược):** Nginx đóng vai trò là "người tiếp tân" đứng ở cổng `80`/`443`:
  - Khi người dùng vào `foodorder.vn/` -> Nginx phục vụ giao diện React.
  - Khi trình duyệt gọi API `foodorder.vn/api/...` -> Nginx âm thầm chuyển tiếp (proxy_pass) request sang cổng `8081` của Spring Boot ở mạng nội bộ.
  - Nginx xử lý mã hóa/giải mã SSL/TLS (HTTPS) tập trung, nén dữ liệu (gzip), chống tấn công DDoS cơ bản.

### 1.5. Docker là gì và vì sao nên dùng?
- **Vấn đề truyền thống:** "Code chạy ngon trên máy tôi nhưng đem lên VPS thì lỗi" (lệch phiên bản Java, thiếu Node, lỗi MySQL charset, xung đột thư viện hệ điều hành).
- **Giải pháp của Docker:** Đóng gói toàn bộ mã nguồn, môi trường thực thi (Java 21, Node 20, thư viện, cấu hình OS) thành một gói duy nhất gọi là **Image**.
- Các khái niệm cốt lõi:
  - **Dockerfile:** Tệp công thức ghi các bước để tạo ra Image.
  - **Docker Image:** "Bản thiết kế" đóng băng (read-only).
  - **Docker Container:** Một instance đang chạy thực tế của Image (như một máy ảo mini siêu nhẹ dùng chung nhân Linux).
  - **Docker Volume:** Vùng lưu trữ bền vững gắn vào Container. Do Container mang tính tạm thời (khi xóa container, dữ liệu bên trong mất hết), dữ liệu MySQL bắt buộc phải lưu vào Volume để dữ liệu tồn tại vĩnh viễn trên ổ đĩa máy chủ.
  - **Docker Network:** Mạng ảo riêng biệt cho phép các Container giao tiếp với nhau bằng tên dịch vụ (ví dụ: Backend gọi DB qua hostname `mysql:3306` thay vì gọi IP).
  - **Multi-stage Build:** Kỹ thuật build image qua nhiều bước để thành phẩm cuối cùng siêu nhẹ. Ví dụ: bước 1 dùng JDK 21 dung lượng 600MB để build file `.jar`, bước 2 chỉ lấy đúng file `.jar` đặt vào JRE 21 tối giản chỉ ~150MB.

---

## 2. KIẾN TRÚC TRIỂN KHAI HỆ THỐNG (ARCHITECTURE)

```
                            INTERNET (Người dùng truy cập)
                                          │
                                          │ (Cổng 80: HTTP / 443: HTTPS)
                                          ▼
                      ┌───────────────────────────────────────┐
                      │            NGINX REVERSE PROXY        │
                      └───────────────────┬───────────────────┘
                                          │
                   ┌──────────────────────┴──────────────────────┐
                   │ (Đường dẫn /)                               │ (Đường dẫn /api, /ws)
                   ▼                                             ▼
       ┌───────────────────────┐                    ┌─────────────────────────┐
       │      FRONTEND         │                    │         BACKEND         │
       │   React 19 / Vite     │                    │  Spring Boot (Java 21)  │
       │ (Phục vụ file tĩnh)   │                    │ (Container Port 8081)   │
       └───────────────────────┘                    └────────────┬────────────┘
                                                                 │
                                                                 │ (Mạng nội bộ Docker)
                                                                 ▼
                                                    ┌─────────────────────────┐
                                                    │         DATABASE        │
                                                    │       MySQL 8.0         │
                                                    │  (Container Port 3306)  │
                                                    │ ┌─────────────────────┐ │
                                                    │ │ Docker Volume (Data)│ │
                                                    │ └─────────────────────┘ │
                                                    └─────────────────────────┘
```

---

## 3. TỪ ĐIỂN CÁC CÂU LỆNH DOCKER & DOCKER COMPOSE

Dưới đây là tập hợp đầy đủ các lệnh thực tế kèm giải thích chi tiết từng tham số (flag):

### 3.1. Các câu lệnh quản lý Container (`docker container ...` hoặc `docker ...`)

#### `docker run [OPTIONS] IMAGE [COMMAND]`
> Tạo và khởi chạy một container mới từ Image.

- **Ví dụ:**
  ```bash
  docker run -d -p 8081:8081 --name backend-app --env-file .env --restart unless-stopped my-backend:1.0
  ```
- **Ý nghĩa từng cờ (flag):**
  - `-d` (*detached mode*): Chạy container ngầm dưới nền, không chiếm màn hình dòng lệnh hiện tại.
  - `-p <cổng_VPS>:<cổng_container>` (*port publishing*): Ánh xạ cổng ngoài máy chủ vào cổng bên trong container. Ví dụ `-p 8081:8081` nghĩa là gửi traffic từ cổng 8081 của VPS vào cổng 8081 của app.
  - `--name <tên>`: Đặt tên gợi nhớ cho container (thay vì để Docker tự sinh tên ngẫu nhiên).
  - `-v <thư_mục_host>:<thư_mục_container>` (*volume mount*): Gắn kết thư mục máy chủ vào trong container để lưu trữ bền vững.
  - `-e KEY=VALUE` hoặc `--env-file <file>`: Nạp biến môi trường vào trong container.
  - `--restart unless-stopped`: Tự động khởi động lại container nếu container bị crash hoặc khi VPS reboot, trừ khi bạn chủ động gõ lệnh dừng.
  - `--network <tên_mạng>`: Kết nối container vào một mạng nội bộ cụ thể.

#### `docker ps [OPTIONS]`
> Liệt kê các container đang chạy.

- **Các cờ hay dùng:**
  - `docker ps`: Chỉ hiển thị container đang chạy (Up).
  - `docker ps -a` (*all*): Hiển thị tất cả container, bao gồm cả những container đã tắt hoặc bị lỗi (Exited).
- **Các cột hiển thị quan trọng:** `CONTAINER ID`, `IMAGE`, `STATUS` (thời gian sống), `PORTS` (ánh xạ cổng), `NAMES`.

#### `docker logs [OPTIONS] CONTAINER`
> Xem log đầu ra của ứng dụng bên trong container (rất quan trọng khi debug lỗi).

- **Ví dụ:**
  ```bash
  docker logs -f --tail 100 backend-app
  ```
- **Ý nghĩa cờ:**
  - `-f` (*follow*): Giữ màn hình xem log realtime trực tiếp theo thời gian thực (giống `tail -f`). Bấm `Ctrl + C` để thoát.
  - `--tail <số_dòng>`: Chỉ in ra N dòng log gần nhất thay vì load hàng triệu dòng từ đầu.

#### `docker exec [OPTIONS] CONTAINER COMMAND`
> Thực thi một lệnh bên trong một container đang chạy.

- **Ví dụ truy cập vào terminal bên trong container:**
  ```bash
  docker exec -it backend-app /bin/bash
  # hoặc nếu container dùng Alpine Linux tối giản:
  docker exec -it backend-app /bin/sh
  ```
- **Ý nghĩa cờ:**
  - `-i` (*interactive*): Giữ luồng STDIN mở để gõ phím.
  - `-t` (*tty*): Cấp phát giao diện dòng lệnh ảo (terminal). Ghép chung là `-it`.

#### `docker stop`, `docker start`, `docker restart`, `docker rm`
- `docker stop <tên_container>`: Dừng một container đang chạy một cách an toàn (gửi tín hiệu SIGTERM).
- `docker start <tên_container>`: Khởi động lại một container đã bị stop trước đó.
- `docker restart <tên_container>`: Tắt đi bật lại container.
- `docker rm <tên_container>`: Xóa hẳn container khỏi hệ thống (lưu ý: phải stop container trước khi xóa, hoặc dùng `docker rm -f`).

---

### 3.2. Các câu lệnh quản lý Image (`docker image ...`)

#### `docker build [OPTIONS] PATH`
> Đóng gói mã nguồn thành một Docker Image dựa theo hướng dẫn trong file `Dockerfile`.

- **Ví dụ:**
  ```bash
  docker build -t food-backend:1.0 -f Dockerfile .
  ```
- **Ý nghĩa cờ:**
  - `-t <name:tag>` (*tag*): Đặt tên cho Image và phiên bản (ví dụ `food-backend:1.0` hoặc `food-backend:latest`).
  - `-f <đường_dẫn_dockerfile>`: Chỉ định file Dockerfile nếu tên khác mặc định.
  - `.` (dấu chấm ở cuối): Ngữ cảnh build (Build Context), nghĩa là toàn bộ file ở thư mục hiện tại sẽ được gửi cho Docker daemon.

#### `docker images`
> Liệt kê danh sách các Docker Image đang được lưu trữ trên ổ đĩa máy chủ.

#### `docker rmi <image_id_hoặc_tên>`
> Xóa một Docker Image khỏi máy chủ để giải phóng dung lượng ổ cứng.

---

### 3.3. Các câu lệnh Docker Compose (`docker compose ...`)
> Docker Compose là công cụ giúp bạn định nghĩa và chạy **toàn bộ hệ thống nhiều container** (Frontend + Backend + DB + Nginx) chỉ bằng một file YAML duy nhất (`docker-compose.yml`).

#### `docker compose up [OPTIONS]`
> Tạo và chạy tất cả các dịch vụ (services) được khai báo trong `docker-compose.yml`.

- **Ví dụ chuẩn hay dùng nhất khi deploy:**
  ```bash
  docker compose up -d --build
  ```
- **Ý nghĩa cờ:**
  - `-d` (*detached*): Khởi chạy tất cả container ngầm dưới nền.
  - `--build`: Bắt buộc Docker biên dịch lại các Dockerfile nếu có thay đổi trong mã nguồn.

#### `docker compose down [OPTIONS]`
> Dừng và xóa toàn bộ container, network được tạo bởi file compose.

- `docker compose down`: Xóa container và network nhưng **giữ nguyên dữ liệu Volume** (Database an toàn).
- `docker compose down -v` (*CỰC KỲ CẨN TRỌNG*): Xóa luôn cả Docker Volumes. **Mất toàn bộ database**!

#### `docker compose ps`
> Xem trạng thái của tất cả các container thuộc dự án hiện tại.

#### `docker compose logs -f [service_name]`
> Xem log realtime của toàn bộ hệ thống hoặc của một dịch vụ cụ thể.
- Ví dụ: `docker compose logs -f backend` (chỉ xem log của service backend).

#### `docker compose restart [service_name]`
> Khởi động lại một service cụ thể mà không làm ảnh hưởng các service khác.
- Ví dụ: `docker compose restart backend`.

---

### 3.4. Dọn dẹp tài nguyên máy chủ (Bảo trì định kỳ)

Sau nhiều lần build code, VPS sẽ bị tích tụ các image rác cũ, làm đầy ổ cứng. Sử dụng lệnh sau để dọn dẹp an toàn:
```bash
# Xóa các image vô danh (dangling images) và container đã tắt:
docker system prune -f

# Xóa toàn bộ image cũ không còn container nào sử dụng (giải phóng hàng GB bộ nhớ):
docker system prune -a --volumes -f  # (Chỉ dùng khi đã chắc chắn dữ liệu db nằm trong named volume được bảo vệ)
```

---

## 4. CHUẨN BỊ FILE CẤU HÌNH DOCKER TRONG DỰ ÁN

Để triển khai dự án này, bạn tạo 4 file cấu hình quan trọng sau đây:

### 4.1. File `food-order-backend/Dockerfile`
Kỹ thuật **Multi-stage Build** tối ưu hóa bộ nhớ cho Java 21 Spring Boot:

```dockerfile
# ==========================================
# GIAI ĐOẠN 1: BIÊN DỊCH ỨNG DỤNG (BUILD)
# ==========================================
FROM gradle:8.7-jdk21-alpine AS builder

WORKDIR /app

# Copy các file cấu hình gradle trước để tận dụng Docker Cache layer
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

# Tải trước dependencies (nếu không đổi file gradle thì bước này được cache)
RUN gradle dependencies --no-daemon || true

# Copy toàn bộ mã nguồn
COPY src ./src

# Build file .jar thực thi, bỏ qua unit test để tăng tốc độ deploy
RUN gradle bootJar --no-daemon -x test

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

# Cấu hình tối ưu bộ nhớ RAM cho máy ảo Java (JVM) trên VPS
# -Xms256m: Cấp phát tối thiểu 256MB RAM
# -Xmx512m: Cấp phát tối đa 512MB RAM (rất phù hợp cho VPS 2GB-4GB)
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

---

### 4.2. File `FoodOrder-Frontend/Dockerfile`
Kỹ thuật Multi-stage Build cho React Vite:

```dockerfile
# ==========================================
# GIAI ĐOẠN 1: BIÊN DỊCH REACT VITE
# ==========================================
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package.json để cache layer cài đặt thư viện
COPY package.json package-lock.json ./
RUN npm ci

# Copy toàn bộ code frontend
COPY . .

# Build sản phẩm tĩnh ra thư mục dist
RUN npm run build

# ==========================================
# GIAI ĐOẠN 2: PHỤC VỤ BẰNG NGINX
# ==========================================
FROM nginx:alpine

# Copy kết quả build sang thư mục web chuẩn của Nginx
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy cấu hình Nginx riêng cho Frontend (xử lý React Router SPA)
COPY nginx-frontend.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

---

### 4.3. File `FoodOrder-Frontend/nginx-frontend.conf`
File cấu hình xử lý việc reload trang trong React Router (tránh lỗi 404):

```nginx
server {
    listen 80;
    server_name localhost;

    location / {
        root /usr/share/nginx/html;
        index index.html index.htm;
        # Nếu không tìm thấy file thực tế, luôn trỏ về index.html để React Router xử lý
        try_files $uri $uri/ /index.html;
    }

    # Bật gzip nén tài nguyên giúp web tải nhanh hơn
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
}
```

---

### 4.4. File `docker-compose.yml` (Nằm ở thư mục gốc của dự án)
Liên kết toàn bộ 3 dịch vụ: MySQL, Backend, Frontend:

```yaml
version: '3.8'

services:
  # ----------------------------------------------------
  # 1. CƠ SỞ DỮ LIỆU MYSQL
  # ----------------------------------------------------
  mysql:
    image: mysql:8.0
    container_name: food_order_mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD:-RootPass123@}
      MYSQL_DATABASE: food_order
      MYSQL_USER: ${DB_USER:-food_user}
      MYSQL_PASSWORD: ${DB_PASSWORD:-FoodPass123@}
    volumes:
      # Gắn volume để lưu dữ liệu vĩnh viễn trên ổ cứng máy chủ
      - mysql_data:/var/lib/mysql
    networks:
      - food_net
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${DB_ROOT_PASSWORD:-RootPass123@}"]
      interval: 10s
      timeout: 5s
      retries: 5

  # ----------------------------------------------------
  # 2. BACKEND SPRING BOOT
  # ----------------------------------------------------
  backend:
    build:
      context: ./food-order-backend
      dockerfile: Dockerfile
    container_name: food_order_backend
    restart: unless-stopped
    depends_on:
      mysql:
        condition: service_healthy # Đợi MySQL chạy xong hẳn mới khởi động backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/food_order?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: ${DB_USER:-food_user}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD:-FoodPass123@}
      SERVER_PORT: 8081
      # Các biến cấu hình từ file .env
      JWT_SECRET_KEY: ${JWT_SECRET_KEY}
      MAIL_USERNAME: ${MAIL_USERNAME}
      MAIL_PASSWORD: ${MAIL_PASSWORD}
      GROQ_API_URL: ${GROQ_API_URL}
      GROQ_API_KEY: ${GROQ_API_KEY}
      GROQ_API_MODEL: ${GROQ_API_MODEL}
      PAYOS_CLIENT_ID: ${PAYOS_CLIENT_ID}
      PAYOS_API_KEY: ${PAYOS_API_KEY}
      PAYOS_CHECKSUM_KEY: ${PAYOS_CHECKSUM_KEY}
      CLOUDINARY_CLOUD_NAME: ${CLOUDINARY_CLOUD_NAME}
      CLOUDINARY_API_KEY: ${CLOUDINARY_API_KEY}
      CLOUDINARY_API_SECRET: ${CLOUDINARY_API_SECRET}
    networks:
      - food_net

  # ----------------------------------------------------
  # 3. FRONTEND REACT
  # ----------------------------------------------------
  frontend:
    build:
      context: ./FoodOrder-Frontend
      dockerfile: Dockerfile
    container_name: food_order_frontend
    restart: unless-stopped
    networks:
      - food_net

networks:
  food_net:
    driver: bridge

volumes:
  mysql_data:
    driver: local
```

---

## 5. QUY TRÌNH TRIỂN KHAI THỰC TẾ TRÊN VPS (TỪNG BƯỚC CHI TIẾT)

### BƯỚC 1: Đăng nhập vào VPS qua SSH
Từ máy cá nhân (Terminal trên Linux/macOS hoặc PowerShell trên Windows):
```bash
ssh root@<IP_CỦA_VPS>
# Nhập mật khẩu VPS được cung cấp
```

---

### BƯỚC 2: Cập nhật hệ thống & Tạo SWAP RAM (Rất quan trọng!)
*Lưu ý:* VPS giá rẻ (1GB - 2GB RAM) khi chạy Java và build mã nguồn rất dễ bị treo do tràn RAM (Out of Memory - OOM). Tạo 2GB-4GB Swap RAM ảo từ ổ cứng SSD sẽ giúp máy chủ không bao giờ bị sập:

```bash
# Cập nhật các gói phần mềm
sudo apt update && sudo apt upgrade -y

# Tạo file swap 4GB
sudo fallocate -l 4G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile

# Lưu cấu hình vĩnh viễn khi khởi động lại VPS
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

---

### BƯỚC 3: Cài đặt Docker & Nginx trên VPS

```bash
# 1. Cài đặt các công cụ hỗ trợ cơ bản
sudo apt install -y curl git ufw nginx certbot python3-certbot-nginx

# 2. Cài đặt Docker engine chính thức từ script của Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 3. Kiểm tra xem Docker và Docker Compose đã cài thành công chưa:
docker --version
docker compose version
```

---

### BƯỚC 4: Cấu hình Tường lửa (UFW Firewall)
Chỉ mở 3 cổng duy nhất:
```bash
sudo ufw allow 22/tcp    # Cổng SSH để bạn đăng nhập
sudo ufw allow 80/tcp    # Cổng HTTP
sudo ufw allow 443/tcp   # Cổng HTTPS
sudo ufw enable          # Kích hoạt tường lửa (chọn 'y')
sudo ufw status          # Kiểm tra trạng thái
```

---

### BƯỚC 5: Đưa mã nguồn lên VPS
Tạo thư mục làm việc trên VPS và clone code về:
```bash
mkdir -p /var/www
cd /var/www

# Clone project từ GitHub / GitLab của bạn
git clone <URL_REPO_CUA_BAN> my-project
cd my-project
```

Tạo file biến môi trường production `.env` tại thư mục gốc:
```bash
nano .env
```
Điền đầy đủ các thông số thực tế của bạn:
```env
DB_ROOT_PASSWORD=MatKhauRootSieuKho123@!
DB_USER=food_admin
DB_PASSWORD=MatKhauFoodApp123@!

JWT_SECRET_KEY=mot_chuoi_that_dai_va_bao_mat_tren_32_ky_tu_ngau_nhien
MAIL_USERNAME=email_cua_ban@gmail.com
MAIL_PASSWORD=mat_khau_ung_dung_gmail_16_ky_tu

GROQ_API_KEY=gsk_...
PAYOS_CLIENT_ID=...
PAYOS_API_KEY=...
PAYOS_CHECKSUM_KEY=...

CLOUDINARY_CLOUD_NAME=...
CLOUDINARY_API_KEY=...
CLOUDINARY_API_SECRET=...
```
*(Bấm `Ctrl + O` rồi `Enter` để lưu, `Ctrl + X` để thoát nano)*.

---

### BƯỚC 6: Khởi chạy dự án bằng Docker Compose
Để tránh lỗi port, ta ánh xạ Frontend ra cổng `3000` và Backend ra cổng `8081` của máy chủ, sau đó dùng Nginx của máy chủ làm Reverse Proxy đón ở cổng `80/443`.

Cập nhật ánh xạ cổng trong `docker-compose.yml`:
- Ở service `frontend`, thêm:
  ```yaml
  ports:
    - "127.0.0.1:3000:80"
  ```
- Ở service `backend`, thêm:
  ```yaml
  ports:
    - "127.0.0.1:8081:8081"
  ```
*(Lưu ý: Chỉ gắn `127.0.0.1:` để chỉ cho phép Nginx nội bộ trên VPS gọi tới, hacker bên ngoài không thể truy cập trực tiếp).*

Bắt đầu khởi chạy hệ thống:
```bash
docker compose up -d --build
```

Kiểm tra xem các container đã hoạt động bình thường chưa:
```bash
docker compose ps
```
Nếu thấy cả 3 container (`mysql`, `backend`, `frontend`) đều có trạng thái **Up**, chúc mừng bạn, tầng ứng dụng đã sẵn sàng!

---

## 6. CẤU HÌNH TÊN MIỀN (DOMAIN) & SSL HTTPS VỚI LET'S ENCRYPT

### 6.1. Trỏ DNS tên miền về VPS
Vào trang quản lý tên miền (như Cloudflare, Namecheap, MatBao, PA Vietnam,...):
- Thêm **Bản ghi A**:
  - Tên: `@` (hoặc domain chính `yourdomain.com`) -> Trỏ về IP của VPS.
  - Tên: `www` -> Trỏ về IP của VPS.

---

### 6.2. Cấu hình Nginx trên máy chủ làm Reverse Proxy
Tạo file cấu hình Nginx mới:
```bash
sudo nano /etc/nginx/sites-available/foodorder
```

Dán nội dung cấu hình sau vào (thay `yourdomain.com` bằng tên miền thật của bạn):
```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;

    # 1. Chuyển tiếp các request gọi API sang Spring Boot
    location /api/ {
        proxy_pass http://127.0.0.1:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 2. Hỗ trợ kết nối thời gian thực WebSocket (Chatbot, Thông báo đơn hàng)
    location /ws/ {
        proxy_pass http://127.0.0.1:8081;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 3. Toàn bộ các request còn lại phục vụ giao diện React Frontend
    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Kích hoạt cấu hình và kiểm tra cú pháp Nginx:
```bash
# Tạo liên kết kích hoạt trang
sudo ln -s /etc/nginx/sites-available/foodorder /etc/nginx/sites-enabled/

# Xóa trang default cũ của Nginx nếu có
sudo rm -f /etc/nginx/sites-enabled/default

# Kiểm tra cú pháp xem có bị lỗi gõ sai không
sudo nginx -t

# Tải lại Nginx
sudo systemctl reload nginx
```

---

### 6.3. Cài đặt chứng chỉ bảo mật SSL miễn phí (HTTPS)
Chạy lệnh Certbot tự động cấu hình HTTPS:
```bash
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com
```
- Nhập email của bạn để nhận thông báo gia hạn.
- Đồng ý điều khoản (`Y`).
- Chọn tự động chuyển hướng HTTP sang HTTPS.
- Certbot sẽ tự động cấu hình toàn bộ file Nginx và thiết lập cronjob tự gia hạn định kỳ (Auto-renewal).

Bây giờ bạn có thể mở trình duyệt và truy cập `https://yourdomain.com` với biểu tượng ổ khóa bảo mật màu xanh!

---

## 7. TỰ ĐỘNG HÓA TRIỂN KHAI VỚI CI/CD (GITHUB ACTIONS)

Khi dự án bước vào giai đoạn vận hành thực tế, việc mỗi lần sửa code lại phải đăng nhập SSH vào VPS, gõ lệnh `git pull` rồi chạy lại Docker vừa tốn thời gian, vừa tiềm ẩn nguy cơ thao tác nhầm lẫn. **CI/CD** sẽ giúp tự động hóa 100% quy trình này: **Chỉ cần bạn gõ `git push origin main`, hệ thống tự động build và deploy lên VPS trong vài phút**.

---

### 7.1. Khái niệm cốt lõi về CI/CD

```
  [ Lập trình viên ]
         │
         │ git push origin main
         ▼
 ┌─────────────────────────────────────────────────────────────┐
 │                      GITHUB REPOSITORY                      │
 └──────────────────────────────┬──────────────────────────────┘
                                │ Triggers Workflow
                                ▼
 ┌─────────────────────────────────────────────────────────────┐
 │                  CI (Continuous Integration)                │
 │  - Checkout source code                                     │
 │  - Kiểm tra cú pháp (Lint)                                  │
 │  - Chạy Unit Test / Integration Test tự động                │
 └──────────────────────────────┬──────────────────────────────┘
                                │ Nếu test PASS
                                ▼
 ┌─────────────────────────────────────────────────────────────┐
 │                 CD (Continuous Deployment)                  │
 │  - Kết nối bảo mật vào VPS qua SSH Key                      │
 │  - Kéo mã nguồn mới nhất về máy chủ                         │
 │  - Build lại Docker Container Backend & Frontend            │
 │  - Khởi động lại ứng dụng mà KHÔNG làm gián đoạn Database   │
 │  - Tự động dọn dẹp Docker Image rác                         │
 └──────────────────────────────┬──────────────────────────────┘
                                │ Hoàn tất
                                ▼
                  [ WEBSITE MỚI ĐÃ ONLINE! 🚀 ]
```

- **CI (Continuous Integration - Tích hợp liên tục):** Đảm bảo mã nguồn mới luôn được kiểm thử và tích hợp thường xuyên mà không phá vỡ tính năng cũ. Nếu có lỗi compile hoặc test fail, quá trình deploy sẽ bị hủy bỏ ngay lập tức để bảo vệ server production.
- **CD (Continuous Deployment - Triển khai liên tục):** Đưa code đã vượt qua bài kiểm tra lên máy chủ production hoàn toàn tự động, không cần con người can thiệp thủ công.

---

### 7.2. Hai chiến lược triển khai CI/CD cho VPS

| Tiêu chí | Chiến lược 1: SSH Remote Deploy (Khuyên dùng khi bắt đầu) | Chiến lược 2: Docker Registry Build (Chuẩn Production lớn) |
| :--- | :--- | :--- |
| **Cách hoạt động** | GitHub Action SSH vào VPS $\rightarrow$ chạy `git pull` $\rightarrow$ ra lệnh cho VPS tự build Docker image. | GitHub Runner (máy ảo 16GB RAM của GitHub) build image $\rightarrow$ push lên Docker Hub / GHCR $\rightarrow$ VPS chỉ pull image về chạy. |
| **Ưu điểm** | - Cực kỳ dễ thiết lập, không cần đăng ký tài khoản Docker Hub.<br>- Toàn bộ source code và file `.env` giữ nguyên vị trí cũ. | - VPS yếu (1GB-2GB RAM) không bao giờ bị nghẽn CPU/RAM do không phải build.<br>- Thời gian deploy siêu nhanh (< 30 giây). |
| **Nhược điểm** | VPS phải tốn CPU và RAM để biên dịch Gradle/Vite (cần có Swap RAM). | Phải cấu hình xác thực với Docker Hub hoặc GitHub Container Registry. |

*Dưới đây là hướng dẫn chi tiết cách thiết lập **Chiến lược 1 (SSH Remote Deploy)** - giải pháp tiện lợi và phổ biến nhất.*

---

### 7.3. Thiết lập CI/CD từng bước với GitHub Actions

#### Bước 1: Tạo cặp SSH Key bảo mật riêng cho GitHub Actions trên VPS
Đăng nhập vào VPS của bạn và tạo một cặp khóa SSH mới dành riêng cho bot deploy (không dùng chung với khóa cá nhân):

```bash
# 1. Tạo cặp khóa SSH mới (loại ed25519 hiện đại và siêu bảo mật)
ssh-keygen -t ed25519 -C "github-actions-deploy" -f ~/.ssh/github_action_key

# (Khi được hỏi passphrase, bấm Enter 2 lần để bỏ trống mật khẩu)

# 2. Thêm Public Key vừa tạo vào danh sách khóa được phép truy cập VPS
cat ~/.ssh/github_action_key.pub >> ~/.ssh/authorized_keys

# 3. Phân quyền chuẩn cho thư mục SSH
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys

# 4. In nội dung Private Key ra màn hình để copy sang GitHub
cat ~/.ssh/github_action_key
```

> [!IMPORTANT]
> Copy toàn bộ đoạn text bắt đầu từ `-----BEGIN OPENSSH PRIVATE KEY-----` cho đến hết `-----END OPENSSH PRIVATE KEY-----`. Đây là chìa khóa bí mật để GitHub Actions đăng nhập vào VPS.

---

#### Bước 2: Cấu hình GitHub Secrets (Lưu trữ thông tin bảo mật)
Trên giao diện GitHub của kho lưu trữ (Repository) dự án:
1. Vào tab **Settings** $\rightarrow$ chọn mục **Secrets and variables** ở menu bên trái $\rightarrow$ chọn **Actions**.
2. Nhấn nút **New repository secret** và lần lượt tạo 4 Secret sau:

| Tên Secret | Giá trị (Value) | Giải thích |
| :--- | :--- | :--- |
| `VPS_HOST` | `103.150.xx.xx` | Địa chỉ IP Public của VPS |
| `VPS_USERNAME` | `root` (hoặc tên user quản trị của bạn) | Tên người dùng SSH |
| `VPS_SSH_KEY` | Toàn bộ nội dung khóa riêng tư bạn đã copy ở Bước 1 | Private Key đăng nhập |
| `VPS_PORT` | `22` | Cổng SSH của VPS |

---

#### Bước 3: Tạo file cấu hình Pipeline `.github/workflows/deploy.yml`
Tại thư mục gốc của dự án trên máy cá nhân, tạo thư mục `.github/workflows/` và tạo file `deploy.yml`:

```yaml
name: CI/CD Pipeline - Deploy Food Order App to VPS

# 1. Điều kiện kích hoạt: Tự động chạy mỗi khi có commit mới đẩy lên nhánh 'main'
on:
  push:
    branches: [ "main" ]
  # Cho phép kích hoạt thủ công từ giao diện GitHub (nút Run workflow)
  workflow_dispatch:

jobs:
  # -------------------------------------------------------------
  # GIAI ĐOẠN 1: CONTINUOUS INTEGRATION (Kiểm tra & Test mã nguồn)
  # -------------------------------------------------------------
  test-and-verify:
    name: Build & Verify Code
    runs-on: ubuntu-latest

    steps:
      - name: 📥 Checkout source code
        uses: actions/checkout@v4

      - name: ☕ Thiết lập Java 21 cho Backend
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '21'

      - name: 🔍 Kiểm tra quyền thực thi Gradle Wrapper
        run: chmod +x food-order-backend/gradlew

      - name: 🔨 Kiểm tra biên dịch Backend (Dry-run)
        working-directory: ./food-order-backend
        run: ./gradlew compileJava --no-daemon

      - name: ⚡ Thiết lập Node.js 20 cho Frontend
        uses: actions/setup-node@v4
        with:
          node-version: 20
          cache: 'npm'
          cache-dependency-path: FoodOrder-Frontend/package-lock.json

      - name: 📦 Cài đặt thư viện Frontend
        working-directory: ./FoodOrder-Frontend
        run: npm ci

      - name: 🔨 Kiểm tra build giao diện React Frontend
        working-directory: ./FoodOrder-Frontend
        run: npm run build

  # -------------------------------------------------------------
  # GIAI ĐOẠN 2: CONTINUOUS DEPLOYMENT (Triển khai lên VPS)
  # -------------------------------------------------------------
  deploy-to-vps:
    name: Deploy to Production VPS
    needs: test-and-verify # Chỉ chạy khi Giai đoạn 1 đã vượt qua thành công
    runs-on: ubuntu-latest

    steps:
      - name: 🚀 Kết nối SSH và chạy lệnh triển khai trên VPS
        uses: appleboy/ssh-action@v1.0.3
        with:
          host: ${{ secrets.VPS_HOST }}
          username: ${{ secrets.VPS_USERNAME }}
          key: ${{ secrets.VPS_SSH_KEY }}
          port: ${{ secrets.VPS_PORT }}
          # Thời gian tối đa cho phép chạy lệnh (15 phút)
          timeout: 15m
          script: |
            set -e # Dừng ngay nếu có bất kỳ lệnh nào bị lỗi
            
            echo "=== BẮT ĐẦU QUY TRÌNH DEPLOY ==="
            
            # 1. Di chuyển vào thư mục dự án trên VPS
            cd /var/www/my-project
            
            # 2. Đồng bộ mã nguồn mới nhất từ GitHub
            echo ">> Đang kéo code mới từ nhánh main..."
            git fetch --all
            git reset --hard origin/main
            
            # 3. Build và khởi động lại Backend & Frontend bằng Docker Compose
            # Lưu ý: Cờ --no-deps backend frontend đảm bảo MySQL KHÔNG bị reset/restart
            echo ">> Đang build lại Docker image và cập nhật container..."
            docker compose up -d --build --no-deps backend frontend
            
            # 4. Dọn dẹp các Docker image cũ không còn sử dụng để tiết kiệm dung lượng đĩa
            echo ">> Đang dọn dẹp tài nguyên rác..."
            docker image prune -f
            
            # 5. In trạng thái các container sau khi deploy
            echo ">> Trạng thái hệ thống hiện tại:"
            docker compose ps
            
            echo "=== TRIỂN KHAI HOÀN TẤT THÀNH CÔNG! ==="
```

---

### 7.4. Quy trình vận hành thực tế sau khi có CI/CD

Từ bây giờ, quy trình làm việc của bạn cực kỳ chuyên nghiệp và đơn giản:

1. Bạn lập trình tính năng mới hoặc sửa bug ở máy cá nhân.
2. Kiểm tra code chạy tốt ở localhost.
3. Commit và đẩy code lên GitHub:
   ```bash
   git add .
   git commit -m "feat: cập nhật giao diện giỏ hàng mới"
   git push origin main
   ```
4. Mở tab **Actions** trên GitHub:
   - Bạn sẽ thấy một workflow đang chạy tự động với các tích xanh $\checkmark$.
   - GitHub sẽ tự động compile code, kết nối SSH vào VPS, pull code về và chạy lại container trong vòng 2-3 phút.
   - Website của bạn tại `https://yourdomain.com` đã được cập nhật tính năng mới mà không hề gián đoạn dữ liệu database!

---

### 7.5. Kịch bản Rollback (Khôi phục khẩn cấp khi code mới bị lỗi)

Nếu bạn vừa push một commit lên `main` và sau khi deploy phát hiện ra lỗi nghiêm trọng ngoài dự kiến:
1. Tại máy cá nhân, quay lui về commit trước đó:
   ```bash
   # Quay lại commit ổn định liền trước
   git revert HEAD
   git push origin main
   ```
2. GitHub Actions sẽ tự động kích hoạt lại và deploy phiên bản ổn định lên VPS ngay lập tức.

---

### 7.6. Cách cập nhật code thủ công (Dự phòng khi CI/CD gặp sự cố)

Trong trường hợp mạng quốc tế chập chờn hoặc GitHub gặp sự cố, bạn vẫn có thể SSH trực tiếp vào VPS và cập nhật thủ công bằng 3 dòng lệnh:

```bash
cd /var/www/my-project

# 1. Kéo code mới nhất về
git pull origin main

# 2. Build lại và khởi chạy container mới mà không làm gián đoạn MySQL
docker compose up -d --build --no-deps backend frontend

# 3. Dọn dẹp các image cũ bị thừa
docker image prune -f
```

---

## 8. CÁC SỰ CỐ THƯỜNG GẶP & CÁCH XỬ LÝ (TROUBLESHOOTING)

### 1. Bị lỗi Out of Memory (OOMKilled) khi build hoặc chạy Spring Boot
- **Hiện tượng:** Chạy `docker compose ps` thấy backend báo `Exited (137)`.
- **Nguyên nhân:** Máy chủ hết RAM và tiến trình bị hệ điều hành Linux tiêu diệt.
- **Cách xử lý:** 
  1. Kiểm tra xem đã tạo Swap RAM chưa bằng lệnh `free -h`.
  2. Giới hạn RAM tối đa cho Java trong `Dockerfile`: `-Xmx384m` hoặc `-Xmx512m`.

### 2. Frontend gọi API bị lỗi CORS
- **Hiện tượng:** Trình duyệt báo `Blocked by CORS policy`.
- **Nguyên nhân:** Địa chỉ gọi API từ React khác với cấu hình cho phép trong `SecurityConfiguration.java` hoặc `WebMvcConfigurer` của Spring Boot.
- **Cách xử lý:** Đảm bảo Backend cho phép Origin là tên miền chính thức của bạn (ví dụ `https://yourdomain.com`). Nếu đi qua Nginx Reverse Proxy chung domain (cùng gọi `https://yourdomain.com/api/...`), bạn sẽ hầu như không bao giờ bị lỗi CORS vì cùng chung một Origin!

### 3. F5 / Reload trang bị lỗi 404 Not Found trên React
- **Hiện tượng:** Truy cập trang chủ thì bình thường, nhưng bấm F5 khi đang ở `/orders` hoặc `/login` thì bị 404.
- **Nguyên nhân:** React là Single Page Application (SPA), server không có file vật lý tên là `/orders`.
- **Cách xử lý:** Đảm bảo file cấu hình `nginx-frontend.conf` có dòng:
  ```nginx
  try_files $uri $uri/ /index.html;
  ```

### 4. WebSocket bị mất kết nối liên tục
- **Hiện tượng:** Tính năng thông báo realtime hoặc Chatbot bị ngắt sau vài giây.
- **Cách xử lý:** Đảm bảo Nginx có đầy đủ 2 dòng nâng cấp kết nối giao thức:
  ```nginx
  proxy_set_header Upgrade $http_upgrade;
  proxy_set_header Connection "upgrade";
  ```

### 5. Xem log trực tiếp khi xảy ra lỗi:
```bash
# Xem log backend để biết Spring Boot báo lỗi gì:
docker compose logs -f --tail 100 backend

# Xem log Nginx:
sudo tail -f /var/log/nginx/error.log
```
