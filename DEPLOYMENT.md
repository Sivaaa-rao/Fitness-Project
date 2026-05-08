# Production Deployment Guide

This project should be deployed with persistent managed services for auth, data, and messaging. Do not rely on local Docker container state for production.

## Recommended Services

- Frontend: Render Static Site
- Gateway and Spring services: Render Web Services or Private Services
- App databases: MongoDB Atlas
- Keycloak database: managed PostgreSQL
- Keycloak: hosted container service with an external PostgreSQL database
- Kafka: managed Kafka provider, or replace Kafka with direct service calls for a smaller demo deployment

## Local Docker Run

The repo includes Dockerfiles for every app service and a local `docker-compose.yml` for:

- MongoDB
- Kafka
- Keycloak
- Keycloak PostgreSQL
- Eureka
- Config Server
- User Service
- Activity Service
- AI Service
- Gateway
- Frontend

Start the full local stack:

```bash
docker compose up --build
```

Optional, pass a Gemini key for AI recommendations:

```bash
GEMINI_API_KEY=your-key docker compose up --build
```

On Windows PowerShell:

```powershell
$env:GEMINI_API_KEY="your-key"
docker compose up --build
```

Local URLs:

```text
Frontend: http://localhost:5173
Gateway:  http://localhost:8080
Eureka:   http://localhost:8761
Keycloak: http://localhost:8181
MongoDB:  mongodb://localhost:27017
Kafka:    localhost:9092
```

Local Keycloak admin login:

```text
Username: admin
Password: admin
```

The local compose stack imports `deployment/keycloak/realm-fitness.json`, which creates:

- Realm: `fitness`
- Frontend client: `oauth2-pkce-client`
- Backend client: `fitlife-backend`
- Local backend client secret: `local-dev-secret`

Stop containers:

```bash
docker compose down
```

Stop and remove local persisted volumes:

```bash
docker compose down -v
```

Use `docker compose down -v` only when you want to erase local MongoDB, Kafka, and Keycloak database state.

## Why Keycloak Needs Its Own Database

Keycloak stores realms, users, clients, roles, secrets, and signing keys in its database. If you run Keycloak with development storage inside a disposable Docker container, that data can disappear when the container is removed or recreated.

Production Keycloak should use an external relational database, usually PostgreSQL.

## Keycloak Setup

Create a PostgreSQL database for Keycloak, then deploy Keycloak with environment variables like:

```env
KC_DB=postgres
KC_DB_URL=jdbc:postgresql://YOUR_KEYCLOAK_DB_HOST:5432/keycloak
KC_DB_USERNAME=keycloak
KC_DB_PASSWORD=replace-me
KC_HOSTNAME=https://your-keycloak-domain.example.com
KC_HTTP_ENABLED=true
KC_PROXY_HEADERS=xforwarded
KC_HEALTH_ENABLED=true
KC_METRICS_ENABLED=true
KC_BOOTSTRAP_ADMIN_USERNAME=admin
KC_BOOTSTRAP_ADMIN_PASSWORD=replace-me
```

After Keycloak starts, create or import:

- Realm: `fitness`
- Public frontend client: `oauth2-pkce-client`
- Confidential backend/admin client: `fitlife-backend`
- Valid frontend redirect URI, for example `https://your-frontend.onrender.com`
- Valid web origin, for example `https://your-frontend.onrender.com`

Save the backend client secret in Render as `KEYCLOAK_ADMIN_CLIENT_SECRET`.

## Backend Environment Variables

Set these on the relevant Render services.

Shared:

```env
CONFIG_SERVER_URL=https://your-config-service.onrender.com
EUREKA_DEFAULT_ZONE=https://your-eureka-service.onrender.com/eureka/
KAFKA_BOOTSTRAP_SERVERS=your-kafka-bootstrap:9092
KAFKA_ACTIVITY_TOPIC=activity-events
KAFKA_ACTIVITY_DELETE_TOPIC=activity-delete-events
```

Gateway:

```env
KEYCLOAK_SERVER_URL=https://your-keycloak-domain.example.com
KEYCLOAK_REALM=fitness
KEYCLOAK_ADMIN_CLIENT_ID=fitlife-backend
KEYCLOAK_ADMIN_CLIENT_SECRET=replace-me
CORS_ALLOWED_ORIGINS=https://your-frontend.onrender.com
GATEWAY_SERVICE_PORT=8080
```

User service:

```env
USER_MONGODB_URI=mongodb+srv://USER:PASSWORD@CLUSTER.mongodb.net/fitness-micro-user?retryWrites=true&w=majority
USER_MONGODB_DATABASE=fitness-micro-user
USER_SERVICE_PORT=8081
```

Activity service:

```env
ACTIVITY_MONGODB_URI=mongodb+srv://USER:PASSWORD@CLUSTER.mongodb.net/aiactivityfitness?retryWrites=true&w=majority
ACTIVITY_MONGODB_DATABASE=aiactivityfitness
ACTIVITY_SERVICE_PORT=8082
```

AI service:

```env
AI_MONGODB_URI=mongodb+srv://USER:PASSWORD@CLUSTER.mongodb.net/airecommendationfitness?retryWrites=true&w=majority
AI_MONGODB_DATABASE=airecommendationfitness
GEMINI_API_KEY=replace-me
AI_SERVICE_PORT=8083
```

Config server:

```env
CONFIG_SERVER_PORT=8888
```

Eureka:

```env
EUREKA_SERVER_PORT=8761
```

## Frontend Environment Variables

Set these in the Render Static Site environment before building:

```env
VITE_API_BASE_URL=https://your-gateway.onrender.com/api
VITE_KEYCLOAK_URL=https://your-keycloak-domain.example.com
VITE_KEYCLOAK_REALM=fitness
VITE_KEYCLOAK_CLIENT_ID=oauth2-pkce-client
VITE_AUTH_REDIRECT_URI=https://your-frontend.onrender.com
VITE_KEYCLOAK_SCOPE=openid profile email offline_access
```

## Deployment Order

1. Create MongoDB Atlas databases for user, activity, and AI recommendation data.
2. Create PostgreSQL for Keycloak.
3. Deploy Keycloak connected to PostgreSQL.
4. Create/import the `fitness` realm and clients in Keycloak.
5. Deploy Eureka.
6. Deploy Config Server.
7. Deploy user, activity, and AI services with their MongoDB/Kafka/Gemini variables.
8. Deploy Gateway with Keycloak and CORS variables.
9. Deploy frontend with Vite variables pointing to Gateway and Keycloak.
10. In Keycloak, confirm redirect URIs and web origins match the deployed frontend URL.

## Maintenance Rules

- Do not commit real secrets.
- Rotate Keycloak client secrets and API keys if they were ever committed.
- Keep Keycloak realm settings exported in a sanitized file for disaster recovery.
- Back up MongoDB Atlas and Keycloak PostgreSQL.
- Use separate Keycloak realms or projects for local, staging, and production.
