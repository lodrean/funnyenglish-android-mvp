# FunnyEnglish — Deployment Guide (Российские сервисы)

> **Версия:** 1.0.0  
> **Дата:** 2025-01-29  
> **Статус:** Production-ready

---

## Архитектура деплоя

```
┌─────────────────────────────────────────────────────────────────┐
│                         Пользователь                             │
└──────────────────────────┬──────────────────────────────────────┘
                           │
           ┌───────────────┴───────────────┐
           │                               │
    ┌──────▼──────┐              ┌────────▼────────┐
    │   RuStore   │              │   AppGallery   │
    │   Android   │              │    Android      │
    └──────┬──────┘              └────────┬────────┘
           │                                │
           └──────────────┬───────────────┘
                          │
           ┌──────────────▼───────────────┐
           │      Telegram Bot (@Bot)    │
           │      (Python + Docker)      │
           └──────────────┬───────────────┘
                          │
           ┌──────────────▼───────────────┐
           │   Cloud Server (Docker)      │
           │                              │
           │  ┌──────────┐ ┌──────────┐  │
           │  │  Nginx   │ │ PostgreSQL│  │
           │  │  (SSL)   │ │ (DB)     │  │
           │  └────┬─────┘ └──────────┘  │
           │       │                      │
           │  ┌────▼─────┐               │
           │  │  Spring  │               │
           │  │  Backend │               │
           │  │  (Kotlin)│               │
           │  └──────────┘               │
           └───────────────────────────────┘
```

---

## 1. Android — Подготовка APK

### Подпись release APK

1. Создать keystore (один раз):
```bash
cd android
keytool -genkey -v -keystore funnyenglish-release.keystore -alias funnyenglish -keyalg RSA -keysize 2048 -validity 10000
```

2. Создать `keystore.properties`:
```properties
storeFile=funnyenglish-release.keystore
storePassword=YOUR_STORE_PASSWORD
keyAlias=funnyenglish
keyPassword=YOUR_KEY_PASSWORD
```

3. Собрать release APK:
```bash
./gradlew assembleRelease
```

### Результат
- APK: `android/app/build/outputs/apk/release/app-release.apk`
- Mapping: `android/app/build/outputs/mapping/release/mapping.txt`

### RuStore
1. Регистрация на https://console.rustore.ru/
2. Создать приложение → загрузить APK
3. В настройках RuStore добавить токены для AppTracer (crash reporting)
4. Добавить `tracer.plugin.token` и `tracer.app.token` в `local.properties`

### AppGallery (Huawei)
1. Регистрация на https://developer.huawei.com/consumer/cn/
2. Создать приложение → загрузить APK
3. Для HMS Push (опционально): добавить `agconnect-services.json` в `app/`

### Изменения в build.gradle.kts
- ✅ Убраны Google Play dependencies (Play Asset Delivery)
- ✅ Добавлен `signingConfigs.release` для подписи
- ✅ RuStore AppTracer (crash reporting) уже подключён
- ✅ `minSdk=24`, `targetSdk=34`, `compileSdk=34`

---

## 2. Backend — Spring Boot + Docker

### Dockerfile (Production)
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/funnyenglish-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### application-prod.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/funnyenglish
    username: ${DB_USER:funnyenglish}
    password: ${DB_PASSWORD:changeme}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
  h2:
    console:
      enabled: false
  security:
    user:
      name: ${ADMIN_USER:admin}
      password: ${ADMIN_PASSWORD:admin123}

server:
  port: 8080
  forward-headers-strategy: native

logging:
  level:
    root: WARN
    com.funnyenglish: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### Сборка Docker image
```bash
cd backend
./gradlew bootJar
docker build -t funnyenglish-backend:1.0.0 .
```

---

## 3. Telegram Bot — Docker

### Dockerfile
```dockerfile
FROM python:3.13-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY bot.py .
CMD ["python", "bot.py"]
```

### requirements.txt
```
python-telegram-bot>=20.0
openai>=1.0
Pillow>=10.0
```

### Переменные окружения
```env
TELEGRAM_TOKEN=your_telegram_bot_token
OPENAI_API_KEY=your_openai_api_key
BACKEND_URL=http://backend:8080
```

### Сборка Docker image
```bash
docker build -t funnyenglish-bot:1.0.0 .
```

---

## 4. Docker Compose (Production)

```yaml
version: '3.8'

services:
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/ssl:/etc/nginx/ssl:ro
      - ./nginx/www:/usr/share/nginx/html:ro
    depends_on:
      - backend
    restart: always
    networks:
      - funnyenglish

  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: funnyenglish
      POSTGRES_USER: ${DB_USER:-funnyenglish}
      POSTGRES_PASSWORD: ${DB_PASSWORD:-changeme}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: always
    networks:
      - funnyenglish

  backend:
    image: funnyenglish-backend:1.0.0
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_USER=${DB_USER:-funnyenglish}
      - DB_PASSWORD=${DB_PASSWORD:-changeme}
      - ADMIN_USER=${ADMIN_USER:-admin}
      - ADMIN_PASSWORD=${ADMIN_PASSWORD:-admin123}
    depends_on:
      - postgres
    restart: always
    networks:
      - funnyenglish

  bot:
    image: funnyenglish-bot:1.0.0
    environment:
      - TELEGRAM_TOKEN=${TELEGRAM_TOKEN}
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - BACKEND_URL=http://backend:8080
    depends_on:
      - backend
    restart: always
    networks:
      - funnyenglish

volumes:
  postgres_data:

networks:
  funnyenglish:
    driver: bridge
```

---

## 5. Nginx + SSL (Let's Encrypt)

### nginx.conf
```nginx
events {
    worker_connections 1024;
}

http {
    upstream backend {
        server backend:8080;
    }

    server {
        listen 80;
        server_name your-domain.ru;
        return 301 https://$server_name$request_uri;
    }

    server {
        listen 443 ssl http2;
        server_name your-domain.ru;

        ssl_certificate /etc/nginx/ssl/fullchain.pem;
        ssl_certificate_key /etc/nginx/ssl/privkey.pem;
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers HIGH:!aNULL:!MD5;

        location / {
            proxy_pass http://backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        location /static/ {
            alias /usr/share/nginx/html/static/;
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
}
```

### SSL Certificate (Let's Encrypt)
```bash
# Установка certbot
sudo apt-get install certbot

# Получение сертификата
sudo certbot certonly --standalone -d your-domain.ru

# Копирование в nginx/ssl
sudo cp /etc/letsencrypt/live/your-domain.ru/fullchain.pem nginx/ssl/
sudo cp /etc/letsencrypt/live/your-domain.ru/privkey.pem nginx/ssl/
```

---

## 6. CI/CD — GitHub Actions

### .github/workflows/deploy.yml
```yaml
name: Deploy to Production

on:
  push:
    branches: [main]
  workflow_dispatch:

jobs:
  build-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build Release APK
        run: |
          cd android
          echo "storeFile=funnyenglish-release.keystore" >> keystore.properties
          echo "storePassword=${{ secrets.KEYSTORE_PASSWORD }}" >> keystore.properties
          echo "keyAlias=funnyenglish" >> keystore.properties
          echo "keyPassword=${{ secrets.KEY_PASSWORD }}" >> keystore.properties
          ./gradlew assembleRelease
      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: release-apk
          path: android/app/build/outputs/apk/release/*.apk

  build-backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build JAR
        run: cd backend && ./gradlew bootJar
      - name: Build Docker Image
        run: cd backend && docker build -t funnyenglish-backend:${{ github.sha }} .
      - name: Save Docker Image
        run: docker save funnyenglish-backend:${{ github.sha }} | gzip > backend-image.tar.gz
      - name: Upload Image
        uses: actions/upload-artifact@v4
        with:
          name: backend-image
          path: backend-image.tar.gz

  build-bot:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Build Docker Image
        run: docker build -t funnyenglish-bot:${{ github.sha }} .
      - name: Save Docker Image
        run: docker save funnyenglish-bot:${{ github.sha }} | gzip > bot-image.tar.gz
      - name: Upload Image
        uses: actions/upload-artifact@v4
        with:
          name: bot-image
          path: bot-image.tar.gz

  deploy:
    needs: [build-backend, build-bot]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Download Artifacts
        uses: actions/download-artifact@v4
      - name: Deploy via SSH
        uses: appleboy/scp-action@master
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          source: "docker-compose.prod.yml,nginx/,backend-image.tar.gz,bot-image.tar.gz"
          target: "/opt/funnyenglish"
      - name: Start Services
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script: |
            cd /opt/funnyenglish
            docker load < backend-image.tar.gz
            docker load < bot-image.tar.gz
            docker-compose -f docker-compose.prod.yml up -d
```

### GitHub Secrets
| Secret | Описание |
|--------|----------|
| `KEYSTORE_PASSWORD` | Пароль keystore |
| `KEY_PASSWORD` | Пароль ключа |
| `SERVER_HOST` | IP сервера |
| `SERVER_USER` | SSH пользователь |
| `SSH_PRIVATE_KEY` | SSH ключ |
| `TELEGRAM_TOKEN` | Токен бота |
| `OPENAI_API_KEY` | OpenAI API ключ |
| `DB_PASSWORD` | Пароль PostgreSQL |
| `ADMIN_PASSWORD` | Пароль админ-панели |

---

## 7. Российские облака — Конфигурации

### Yandex Cloud (Container Registry + Compute)
```bash
# Создание registry
yc container registry create --name funnyenglish

# Сборка и пуш образов
yc cr login
DOCKER_TAG=cr.yandex/$REGISTRY_ID/funnyenglish-backend:1.0.0
docker build -t $DOCKER_TAG ./backend
docker push $DOCKER_TAG

# Создание ВМ
yc compute instance create \
  --name funnyenglish-server \
  --zone ru-central1-a \
  --cores 2 \
  --memory 4 \
  --create-boot-disk size=20,type=network-ssd,image-folder-id=standard-images,image-family=ubuntu-2204-lts \
  --network-interface subnet-name=default-ru-central1-a,nat-ip-version=ipv4 \
  --ssh-key ~/.ssh/id_rsa.pub
```

### Selectel (Облачные серверы)
```bash
# Создание сервера через API
curl -X POST https://api.selectel.ru/v1/servers \
  -H "X-Token: $SELECTEL_API_KEY" \
  -d '{
    "server": {
      "flavor": "flavor_cpu2_ram4GB_disk20GB",
      "image": "ubuntu-22.04",
      "name": "funnyenglish-server",
      "key_name": "my-ssh-key"
    }
  }'
```

### Cloud.ru (Cloud Servers)
```bash
# Создание сервера через веб-консоль или API
# Рекомендуемый тариф: 2 vCPU, 4 GB RAM, 20 GB SSD
# ОС: Ubuntu 22.04 LTS
```

### SberCloud (Cloud Servers)
```bash
# Создание ECS через консоль или API
# Рекомендуемый тариф: s6.large.2 (2 vCPU, 4 GB RAM)
# ОС: Ubuntu 22.04 LTS
# Зона: ru-moscow-1
```

---

## 8. Переменные окружения (Production)

Создать `.env` файл на сервере:

```env
# Database
DB_USER=funnyenglish
DB_PASSWORD=your_secure_password_here

# Admin
ADMIN_USER=admin
ADMIN_PASSWORD=your_secure_admin_password

# Telegram Bot
TELEGRAM_TOKEN=your_telegram_bot_token

# OpenAI
OPENAI_API_KEY=your_openai_api_key

# Domain
DOMAIN=your-domain.ru
```

---

## 9. Первый запуск (Production)

```bash
# 1. Клонировать репозиторий
git clone https://github.com/your-repo/funnyenglish.git
cd funnyenglish

# 2. Создать .env
nano .env  # заполнить все переменные

# 3. Создать директории
mkdir -p nginx/ssl nginx/www

# 4. Получить SSL сертификат
sudo certbot certonly --standalone -d your-domain.ru
sudo cp /etc/letsencrypt/live/your-domain.ru/fullchain.pem nginx/ssl/
sudo cp /etc/letsencrypt/live/your-domain.ru/privkey.pem nginx/ssl/

# 5. Собрать и запустить
docker-compose -f docker-compose.prod.yml up -d --build

# 6. Проверка
docker-compose -f docker-compose.prod.yml ps
docker-compose -f docker-compose.prod.yml logs -f
```

---

## 10. Мониторинг и обслуживание

### Обновление сертификатов (cron)
```bash
# Добавить в crontab
0 3 * * * certbot renew --quiet && docker-compose -f /opt/funnyenglish/docker-compose.prod.yml restart nginx
```

### Бэкап базы данных
```bash
# Ежедневный бэкап
docker-compose -f docker-compose.prod.yml exec postgres pg_dump -U funnyenglish funnyenglish > backup_$(date +%Y%m%d).sql
```

### Обновление приложения
```bash
# Pull новых образов
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
```

---

## 11. Чеклист деплоя

### Android
- [ ] Создан keystore
- [ ] Создан `keystore.properties`
- [ ] Собран release APK (`./gradlew assembleRelease`)
- [ ] Убраны Google Play dependencies
- [ ] RuStore AppTracer токены настроены
- [ ] APK загружен в RuStore
- [ ] APK загружен в AppGallery (опционально)

### Backend
- [ ] Создан `application-prod.yml` с PostgreSQL
- [ ] Собран Docker image (`docker build -t funnyenglish-backend:1.0.0 .`)
- [ ] Docker image запушен в registry (Yandex Cloud / Selectel / др.)

### Bot
- [ ] Создан `Dockerfile` для бота
- [ ] Собран Docker image (`docker build -t funnyenglish-bot:1.0.0 .`)
- [ ] TELEGRAM_TOKEN и OPENAI_API_KEY настроены

### Инфраструктура
- [ ] Сервер создан (Yandex Cloud / Selectel / Cloud.ru / SberCloud)
- [ ] Docker и Docker Compose установлены
- [ ] `.env` файл создан на сервере
- [ ] `docker-compose.prod.yml` развёрнут
- [ ] Nginx + SSL настроены
- [ ] PostgreSQL запущена
- [ ] Домен направлен на сервер
- [ ] SSL сертификат получен (Let's Encrypt)

### CI/CD
- [ ] GitHub Actions workflow настроен
- [ ] Secrets добавлены в GitHub
- [ ] SSH ключ добавлен на сервер
- [ ] Первый деплой выполнен успешно

---

## 12. Файлы деплоя

| Файл | Описание |
|------|----------|
| `android/keystore.properties` | Конфиг подписи (не коммитить!) |
| `android/keystore.properties.template` | Шаблон для команды |
| `backend/src/main/resources/application-prod.yml` | Production конфиг |
| `backend/Dockerfile` | Docker для backend |
| `Dockerfile` | Docker для bot |
| `docker-compose.prod.yml` | Production Docker Compose |
| `nginx/nginx.conf` | Nginx reverse proxy |
| `.github/workflows/deploy.yml` | CI/CD pipeline |
| `.env` | Переменные окружения (не коммитить!) |
| `DEPLOY.md` | Эта документация |

---

## Контакты и поддержка

- **RuStore:** https://console.rustore.ru/
- **AppGallery:** https://developer.huawei.com/
- **Yandex Cloud:** https://cloud.yandex.ru/
- **Selectel:** https://selectel.ru/
- **Cloud.ru:** https://cloud.ru/
- **SberCloud:** https://sbercloud.ru/
