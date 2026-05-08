# AI-Powered Fitness Platform

AI-Powered Fitness Platform is a full-stack microservices application for tracking fitness activities and generating AI-based workout recommendations. Users can register, log in, add activities, view activity history, and receive recommendation feedback based on their activity data.

The project runs locally with Docker Compose and includes the frontend, backend microservices, service discovery, centralized configuration, authentication, database, Kafka messaging, and AI recommendation service.

## Features

- User registration and login
- Keycloak-based authentication
- JWT-protected backend APIs
- User profile synchronization
- Fitness activity creation, update, delete, and listing
- Activity statistics and detail views
- Kafka-based activity event processing
- Gemini-powered recommendation generation
- Fallback recommendations if the external AI API is unavailable or rate-limited
- Full local Docker Compose setup

## Tech Stack

Frontend:

- React
- Vite
- Material UI
- Axios

Backend:

- Java
- Spring Boot
- Spring Cloud Gateway
- Spring Security
- Spring Cloud Config
- Netflix Eureka

Infrastructure:

- Docker
- Docker Compose
- MongoDB
- Apache Kafka
- Keycloak
- PostgreSQL for Keycloak

AI:

- Google Gemini API

## Repository Structure

```text
.
|-- activityservice/       # Activity tracking microservice
|-- aiservice/             # AI recommendation microservice
|-- configserver/          # Spring Cloud Config Server
|-- deployment/            # Keycloak realm and deployment support files
|-- eureka/                # Eureka service discovery server
|-- fitness-frontend/      # React frontend
|-- gateway/               # Spring Cloud Gateway and auth routing
|-- userservice/           # User profile microservice
|-- docker-compose.yml     # Full local stack
|-- DOCKER.md              # Short Docker reference
`-- DEPLOYMENT.md          # Production deployment notes
```

## Prerequisites

Install these before running the project:

- Git
- Docker Desktop
- Docker Compose
- A Gemini API key from Google AI Studio

You do not need to install Java, Maven, Node.js, MongoDB, Kafka, or Keycloak locally if you run the project with Docker Compose. Docker builds and runs everything.

## Clone the Repository

```bash
git clone https://github.com/Sivaaa-rao/Fitness-Project.git
cd Fitness-Project
```

## Environment Setup

Create a root `.env` file in the project folder.

On Windows PowerShell:

```powershell
New-Item -ItemType File -Path .env
```

On macOS/Linux:

```bash
touch .env
```

Add your Gemini API key:

```env
GEMINI_API_KEY=your_gemini_api_key_here
```

Optional Cloudinary values can be added if you want image upload support:

```env
VITE_CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
VITE_CLOUDINARY_UPLOAD_PRESET=your_unsigned_upload_preset
```

Do not commit real API keys or secrets.

## Run the Full Application Locally

From the repository root, run:

```bash
docker compose up --build -d
```

This starts:

- MongoDB
- Kafka
- Keycloak PostgreSQL
- Keycloak
- Eureka Server
- Config Server
- User Service
- Activity Service
- AI Service
- Gateway
- React frontend

The first build can take several minutes because Docker downloads base images and Maven/Node dependencies.

## Local URLs

```text
Frontend: http://localhost:5173
Gateway:  http://localhost:8080
User Service: http://localhost:8081
Activity Service: http://localhost:8082
AI Service: http://localhost:8083
Eureka:   http://localhost:8761
Keycloak: http://localhost:8181
MongoDB:  mongodb://localhost:27017
Kafka:    localhost:9092
```

## Keycloak Admin Login

```text
URL:      http://localhost:8181
Username: admin
Password: admin
```

The local Keycloak realm is imported automatically from:

```text
deployment/keycloak/realm-fitness.json
```

The imported realm includes:

- Realm: `fitness`
- Frontend client: `oauth2-pkce-client`
- Backend/admin client: `fitlife-backend`

These local credentials are for development only. Do not use them in production.

## How to Use the Application

1. Start the stack:

```bash
docker compose up --build -d
```

2. Open the frontend:

```text
http://localhost:5173
```

3. Sign up with a new user account.

4. Log in with the same user.

5. Add a fitness activity, such as running, cycling, walking, or strength training.

6. Open the activity details or recommendations section.

7. The Activity Service publishes the activity event to Kafka.

8. The AI Service consumes the event and generates a recommendation using Gemini.

9. If Gemini is unavailable or rate-limited, the service saves a fallback recommendation so the user experience does not break.

## Demo User

If you want a clean demo user, create one through the signup screen:

```text
Email: testuser@lewisu.edu
Password: Test@12345
```

If the local database was reset, this user must be recreated.

## Useful Docker Commands

Check running services:

```bash
docker compose ps
```

View all logs:

```bash
docker compose logs -f
```

View logs for one service:

```bash
docker compose logs -f gateway
docker compose logs -f userservice
docker compose logs -f activityservice
docker compose logs -f aiservice
```

Stop all services:

```bash
docker compose down
```

Stop all services and delete local persisted data:

```bash
docker compose down -v
```

Use `docker compose down -v` when you want to clear all local users, activities, recommendations, Kafka state, MongoDB data, and Keycloak data.

## Rebuild After Code Changes

Rebuild the full stack:

```bash
docker compose up --build -d
```

Rebuild only one service:

```bash
docker compose up --build -d aiservice
docker compose up --build -d activityservice
docker compose up --build -d gateway
```

## Gemini Model Configuration

The project uses this default Gemini model:

```text
gemini-2.5-flash-lite
```

The local Docker Compose configuration points the AI service to:

```env
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent
```

This model was selected because some free-tier projects may have zero available quota for other models such as `gemini-2.0-flash`.

If you want to use another Gemini model, update:

```text
docker-compose.yml
aiservice/src/main/resources/application.yml
configserver/src/main/resources/config/ai-service.yml
```

Then rebuild:

```bash
docker compose up --build -d configserver aiservice
```

## Troubleshooting

### Frontend does not load

Check whether the frontend container is running:

```bash
docker compose ps
```

Then open:

```text
http://localhost:5173
```

### Login does not work

Make sure Keycloak is running:

```bash
docker compose logs -f keycloak
```

Then open:

```text
http://localhost:8181
```

If Keycloak data is broken or old, reset the local stack:

```bash
docker compose down -v
docker compose up --build -d
```

### Services are not registered in Eureka

Open:

```text
http://localhost:8761
```

If services are missing immediately after startup, wait 30 to 60 seconds and refresh. On a cold start, services can take time to register.

### Recommendation is not generated

Check AI service logs:

```bash
docker compose logs -f aiservice
```

Common causes:

- Missing `GEMINI_API_KEY`
- Gemini API quota exceeded
- Gemini model not available for your project
- Kafka or AI service not ready when the activity was created

The application includes fallback recommendation handling, so recommendation records should still be saved even if Gemini is temporarily unavailable.

### Gemini returns 429 Too Many Requests

This can happen even with one user action because Gemini quotas are enforced by Google Cloud project and model. Limits can include requests per minute, tokens per minute, and requests per day.

Check your quota here:

```text
https://ai.dev/rate-limit
```

Try again later, use `gemini-2.5-flash-lite`, or create a new API key/project if your current project has no free quota for the selected model.

### Ports are already in use

Stop any existing containers:

```bash
docker compose down
```

If another local application is using one of these ports, stop that application or change the port mapping in `docker-compose.yml`.

## Testing

The repository includes backend tests for major service logic, including:

- Activity service behavior
- AI recommendation service behavior
- Kafka activity listeners
- Registration request validation
- Gateway user synchronization
- User service registration and lookup

Each service can be tested from its own folder with Maven if Java and Maven are installed locally:

```bash
mvn test
```

When using Docker, the service Dockerfiles currently build with tests skipped for faster local startup.

## Production Notes

The current setup is intended for local development and demonstration.

For production, use:

- Managed MongoDB, such as MongoDB Atlas
- Managed PostgreSQL for Keycloak
- A managed Kafka provider
- Strong Keycloak admin credentials
- Secure environment variables
- HTTPS
- Separate production secrets
- Cloud hosting such as Render, AWS, Azure, or GCP

Do not use local demo credentials such as `admin/admin` or local client secrets in production.

## License

This project is for academic and demonstration purposes.
