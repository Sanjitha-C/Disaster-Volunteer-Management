# Disaster Volunteer Coordination System (DVC)

DVC is a college project for coordinating emergency requests, volunteers, tasks, resources, and notifications. All ten development stages in `PROJECT_PROGRESS.md` are complete. Admins can review response summaries and manage account roles/statuses alongside the response workflows.

## Current project layout

- `frontend/` — React, Vite, React Router, Axios, Bootstrap, React Icons, and Chart.js starter.
- `backend/` — Spring Boot 3 / Maven application, JPA model, repositories, BCrypt authentication, session security, and REST APIs.
- `database/` contains MySQL setup and schema scripts.
- `testing/` contains test cases, reports, Selenium instructions, and quality analysis.
- `documentation/` contains API, architecture, and data model references.
- `PROJECT_PROGRESS.md`, `DEVELOPMENT_LOG.md`, `FEATURE_TRACEABILITY.md` — progress and review records.

## Prerequisites found during Stage 1

| Tool | Detected | Notes |
|---|---|---|
| Java | 26 | Present. Project targets Java 21; Java 21 is not installed/detected. Install a Java 21 JDK for the requested stack and set `JAVA_HOME`. |
| Node.js | 24.14.1 | Present. |
| npm | 11.11.0 | Present. |
| Maven | 3.9.14 | Present. Maven Wrapper is not yet configured. |
| MySQL | MySQL80 service running; `mysql` command not found | MySQL server is installed and listening locally. The command-line client is not on PATH; use MySQL Workbench or add the client `bin` directory to PATH. |
| Git | Present | Workspace had no repository; Stage 1 initializes one. |

The build targets Java release 21. The detected runtime is Java 26. The local MySQL service and backend connection were verified in Stage 2.

## Start the frontend

```cmd
cd frontend
npm install
npm run dev
```

Open the local URL Vite prints (normally `http://localhost:5173`).

## Start the backend (Windows CMD)

In a second terminal, from the project root:

```cmd
cd /d "C:\Users\Sanjitha\Downloads\New folder (13)\backend"
set "DB_USERNAME=root"
set "DB_PASSWORD=YOUR_LOCAL_MYSQL_PASSWORD"
mvn -Dmaven.repo.local="%CD%\target\m2-repository" spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

Replace the password with your local MySQL password. The health endpoint is `http://localhost:8081/api/health`; it reports application availability. See [database setup](database/README.md) for schema and local database notes.

## Database (Stage 2)

The MySQL datasource reads `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` from the process environment. See [database setup](database/README.md) and the [ER diagram](documentation/database/ERD.md). Never put a real password in tracked files. Local development uses Hibernate to create or update tables from the JPA entities.

## Authentication (Stage 3)

The backend uses BCrypt password hashes and server-side HTTP sessions. Volunteer registration is open; it cannot set the account role. Login, logout, and the current-user endpoint are documented in [the authentication API guide](documentation/api/authentication.md). State-changing requests require the `XSRF-TOKEN` cookie value in the `X-XSRF-TOKEN` header. Development defaults seed the demo accounts once and never reset an existing account's password. Use `spring.profiles.active=prod` in deployed environments to disable the demo seeder.

Demo logins for the local demo profile:

| Role | Email | Password |
|---|---|---|
| Admin | `admin@gmail.com` | `Admin@12345` |
| Volunteer | `volunteer@gmail.com` | `Volunteer@12345` |
| User | `user@gmail.com` | `User@12345` |

## Current feature status

All 15 Jira stories are tracked in `FEATURE_TRACEABILITY.md`. DVC-20, DVC-2, and DVC-3 through DVC-15 are implemented and verified at their documented stage scope. Stage 9 results are in [`testing/test-cases/stage-9-testing-quality.md`](testing/test-cases/stage-9-testing-quality.md); see [`testing/reports/README.md`](testing/reports/README.md) and [`testing/selenium/README.md`](testing/selenium/README.md) for report and browser test instructions. Stage 10 review notes are in [`documentation/final-integration-review.md`](documentation/final-integration-review.md).
