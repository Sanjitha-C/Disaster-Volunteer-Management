# DVC architecture

## Runtime components

```mermaid
flowchart LR
  Browser[React and Vite browser client]
  API[Spring Boot REST API]
  Auth[Session security and CSRF]
  Services[Auth, volunteer, coordination, operations, admin services]
  Repos[Spring Data JPA repositories]
  DB[(MySQL database)]
  Browser -->|Credentialed JSON requests| API
  API --> Auth
  Auth --> Services
  Services --> Repos
  Repos --> DB
```

The frontend uses Axios with `withCredentials`, a server session cookie, and the `XSRF-TOKEN` cookie/header pair for state-changing requests. API role checks are enforced by Spring Security and service-level ownership checks; client-side route guards only control navigation and are not the authorization boundary. Credentialed CORS allows the configured frontend origin (`dvc.cors.allowed-origin`, default `http://localhost:5173`).

## Main workflow

```mermaid
sequenceDiagram
  actor User
  actor Admin
  actor Volunteer
  participant UI as React UI
  participant API as Spring Boot API
  participant DB as MySQL
  User->>UI: Submit emergency request
  UI->>API: POST request (session + CSRF)
  API->>DB: Save request and required skills
  Admin->>UI: Assign available volunteer
  UI->>API: Assign request
  API->>DB: Save assignment and notifications
  Volunteer->>UI: Accept, start, complete
  UI->>API: Update allowed task status
  API->>DB: Update assignment and notify requester
  Volunteer->>UI: Submit feedback after completion
  API->>DB: Save feedback
  Admin->>UI: Review feedback and reports
```

## Data and API references

- Relational entities and constraints are summarized in the [ER diagram](../database/ERD.md).
- Authentication, CSRF, and role routes are described in the [authentication API guide](../api/authentication.md).
- Local schema creation and MySQL setup are in [`database/README.md`](../../database/README.md).
- Feature-to-API-to-table mapping is in [`FEATURE_TRACEABILITY.md`](../../FEATURE_TRACEABILITY.md).

Integration tests use isolated in-memory H2 with MySQL compatibility mode. Local development uses MySQL. Production database migrations and deployment hosting are outside this development scaffold.
