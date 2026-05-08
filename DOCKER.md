# Docker Guide

Run everything locally:

```bash
docker compose up --build
```

Frontend:

```text
http://localhost:5173
```

Keycloak admin:

```text
http://localhost:8181
username: admin
password: admin
```

Useful commands:

```bash
docker compose ps
docker compose logs -f gateway
docker compose logs -f userservice
docker compose down
docker compose down -v
```

The `-v` flag deletes local persisted container data.

For AI recommendations, pass a Gemini key:

```bash
GEMINI_API_KEY=your-key docker compose up --build
```

PowerShell:

```powershell
$env:GEMINI_API_KEY="your-key"
docker compose up --build
```

The local Keycloak realm is imported from:

```text
deployment/keycloak/realm-fitness.json
```

For production, do not use the local `admin/admin` or `local-dev-secret` values. Use Render environment variables and a persistent Keycloak PostgreSQL database.
