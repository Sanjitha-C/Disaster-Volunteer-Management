# Development Log

## Stage 1 — Environment and Project Setup

**Initial inspection:** Workspace was empty and contained no existing Git repository or `AGENTS.md` instructions.

**Detected tools:** Java 26, Node.js 24.14.1, npm 11.11.0, Maven 3.9.14, and Git. The `mysql` command was unavailable. The requested Java 21 runtime is not detected; install a JDK 21 to match the project target. Install/configure MySQL 8.x before Stage 2 database work.

**Created:** Vite/React frontend starter, Spring Boot 3.5.6 Maven starter, API health endpoint, `.gitignore`, `.env.example`, README, and the project directory skeleton.

**Verification:** `git init` succeeded. `git status` succeeded when Git received a per-command safe-directory override for the workspace. An initial `npm install` attempt in the sandbox was blocked (`EACCES`). The user's later CMD log shows Maven Central downloads succeeding with a project-local cache; Spring Boot compiled the backend and attempted to start, but Tomcat could not bind to port 8080 because another process already owns it. This confirms compilation, but not a successful backend start or health response.

**Exact CMD commands for the backend:**

```cmd
cd /d "C:\Users\Sanjitha\Downloads\New folder (13)\backend"
mvn -Dmaven.repo.local="%CD%\target\m2-repository" -DskipTests package
mvn -Dmaven.repo.local="%CD%\target\m2-repository" spring-boot:run
```

Check `http://localhost:8080/api/health`; if it does not return DVC health JSON, inspect the current 8080 listener or use port 8081. Frontend install/build/start is still pending.

**Stage boundary:** No database schema, authentication, demo accounts, dashboards, or backlog workflows are implemented in Stage 1.

## Stage 2 — MySQL and Backend Foundation

**Inspection:** Windows service `MySQL80` is installed and reported `Running`. The MySQL command-line client is not on PATH. The application build currently targets Java 21 while the detected runtime is Java 26.

**Implemented:** Added Spring Data JPA, Bean Validation, and MySQL Connector/J dependencies. Added entities for accounts, volunteer profiles, skills, availability slots, emergency requests, task assignments, resources, notifications, and feedback; added repositories, enum value types, foreign-key mappings, indexes and uniqueness constraints. Added consistent JSON handling for request validation and data-integrity conflicts. Added environment-based MySQL datasource settings, a create-database script, and an ER diagram.

**Verification:** `mvn -DskipTests package` succeeded with an in-project Maven cache, compiling 33 Java source files for Java release 21 and packaging the backend. This required allowing Maven Central access after an earlier restricted attempt failed. `MySQL80` is Running and `netstat` shows port 3306 listening.

**User follow-up and live verification:** The backend responds at `http://localhost:8081/api/health` with `{"service":"dvc-backend","status":"UP"}`. A process/port check identified the same PID listening on 8081 with established TCP sessions to the MySQL listener on 3306. Together with successful application startup and `spring.jpa.hibernate.ddl-auto=update`, this confirms the backend connected to MySQL and Hibernate applied the development entity schema. The health route itself reports application availability and does not execute a database query.

**Stage result:** Stage 2 database implementation and connectivity are verified.

## Stage 3 — Authentication and Demo Accounts

**Implemented:** Added Spring Security with BCrypt, server-side sessions, CSRF cookies for the Vite SPA, role gates for `/api/admin/**`, `/api/volunteer/**`, and `/api/user/**`, volunteer-only public registration, login/current-user/logout routes, and inactive-account rejection. The `demo` profile seeds missing Admin, Volunteer, and User accounts plus the volunteer profile and required skills. Existing accounts and passwords are not overwritten. Demo credentials are configurable through `DVC_DEMO_*` environment variables; activate a non-demo profile when deployed.

**Verification:** `mvn test` succeeded on 2026-10-30. JUnit/Spring Boot/MockMvc ran 13 tests on isolated H2: 13 passed, 0 failures, 0 errors, 0 skipped. Tests cover all three demo login roles and session persistence, access control, CSRF, registration validation, role-escalation rejection, BCrypt storage, duplicate email, invalid credentials, logout, and demo-seeder idempotence. No test used MySQL.

The Vite host was aligned to `localhost` to match the backend's credentialed CORS and same-site cookie configuration. The first sandboxed build attempt was blocked because esbuild could not read an ancestor directory; the later Stage 4 build succeeded after workspace filesystem access was allowed.

**Stage boundary:** The backend auth API is complete. Frontend login/registration forms, protected client-side routing, and role dashboard shells are complete in Stage 4; workflow APIs and dashboard data are later stages.

## Stage 4 — Frontend and Role-Based Dashboards

**Implemented:** Added home-page sign-in and volunteer-registration entry points, sign-in and volunteer-registration screens, local demo-account autofill, a session-aware authentication context, CSRF/credential-enabled Axios API calls, redirect after login, a protected dashboard route, and distinct Admin, Volunteer, and User navigation/overview shells. Feature routes without backend support show a clear roadmap state. Changed Vite's host to `localhost` to match the API CORS allowlist and same-site session cookie behavior. Full dashboard data and task actions remain in later feature stages.

**Verification:** `npm run build` succeeded with Vite 6.4.5 (97 modules transformed). Vite served the app entry and transformed `App.jsx` and `AuthContext.jsx` modules with HTTP 200. A CORS preflight from `http://localhost:5173` succeeded with credentials and the CSRF header allowed. Live session checks against the restarted backend succeeded for the Admin, Volunteer, and User demo accounts: each login role matched `/api/auth/me`, then logout invalidated the session. The backend process has established MySQL connections. Initial sandboxed build/start attempts were denied filesystem access; rerunning with the required workspace access passed.

## Stage 5 - Volunteer Profile, Skills, and Availability

**Implemented:** Added authenticated APIs for volunteer profile read/update, skill add/edit/remove, availability status, and future availability slot CRUD. Profile and skill routes resolve the volunteer from the authenticated account. Slot ranges are validated and overlaps rejected. Admins can read active volunteers' availability and saved slots. Connected volunteer overview/profile/skills/availability screens and the Admin volunteer availability list to dashboard navigation.

**Verification:** Backend suite: 18 tests passed (13 authentication and 5 volunteer-management integration tests), with 0 failures or errors on isolated H2. Frontend production build passed with Vite 6.4.5, transforming 99 modules. A sandboxed build was initially denied ancestor-directory access; rerunning with workspace access succeeded.

**Runtime follow-up:** Restart the backend on port 8081 so its running process loads the new Stage 5 routes. Then verify profile edits, skill changes, availability slots, and the Admin view against configured MySQL. Integration tests use isolated H2 and do not claim live Stage 5 MySQL verification.
## Stage 6 - Emergency Requests and Assignments

**Implemented:** Community users can submit requests with urgency, location, details, and optional skills, then see their own request history/status. Admins can review all requests and assign an active, available volunteer. Duplicate assignment and closed-request assignment are rejected. Volunteers can view their assigned requests. Assignment now notifies both volunteer and requester.

**Verification:** Three Stage 6 MockMvc integration scenarios pass on isolated H2 (request submission/ownership, Admin assignment and volunteer view, availability and anonymous-access rules). Frontend Vite 6.4.5 production build succeeds. The full suite passed at 21 tests before Stage 7 additions; current suite has 25 tests.

## Stage 7 - Task Operations and Supporting Features

**Implemented:** Volunteers can accept, start, complete, or cancel tasks along valid transitions; requesters receive notifications on task status changes. All signed-in roles can view and mark their own notifications read. Admins can manage resource inventory. Volunteers can submit a 1-5 rating and comments after completing a task; Admins can review feedback.

**Verification:** Four Stage 7 integration scenarios plus all Stage 3, 5, and 6 tests pass: 25 total, 0 failures, 0 errors, 0 skipped using isolated H2. Frontend Vite 6.4.5 production build succeeds with 103 modules.

**Runtime follow-up for Stages 6-7:** Restart the backend on port 8081 to load new APIs. Test request submission/assignment, task updates, notifications, resources, and feedback in the browser; H2 integration tests do not claim live MySQL persistence verification.
## Stage 8 - Dashboards and Administration

**Implemented:** Admin dashboard summary endpoints and responsive report cards/status charts for account, volunteer, request, assignment, resource, and feedback data. Admin user management lists accounts and updates role/status without touching passwords. Promoting an account to Volunteer creates an unavailable profile. Admin self-lockout and loss of the last active Admin are blocked.

**Verification:** Three Stage 8 MockMvc integration tests and previous integration tests pass: 28 total, 0 failures, 0 errors, 0 skipped on isolated H2. Frontend production build succeeds with Vite 6.4.5 and 103 modules.

**Runtime follow-up:** Restart the backend on port 8081 to load Stage 8 routes and verify the dashboard and role changes through the browser. Integration tests use H2 and do not claim live MySQL verification.

## Post-stage UI follow-up - Volunteer history and stale Admin API

**User report:** Volunteer Task History and Feedback still showed roadmap placeholders. Admin reports and user management showed request errors and an empty account list.

**Diagnosis:** The frontend on port 5173 served current source, but the backend currently answering on port 8081 returned HTTP 404 for `/api/admin/reports/summary` and `/api/admin/users`. The current source includes both endpoints. This confirms the running backend is an older build. A temporary current-source backend using isolated H2 returned HTTP 200 for both Admin routes, listed three demo accounts, and returned HTTP 200 for volunteer assignments and feedback.

**Implemented:** Connected the Volunteer Task History and Feedback navigation routes to data-backed screens; added a volunteer-owned feedback history endpoint; made assignment selection use the signed-in volunteer rather than assuming the first assignment belongs to them; replaced the outdated overview roadmap message. Admin user management no longer displays “No accounts found” when the account request failed.

**Verification:** `npm run build` passed with 104 modules. `mvn -DskipTests package` passed and compiled all 79 backend Java files. The updated backend's Admin and volunteer endpoints returned HTTP 200 on isolated H2. No test suite was run for this follow-up.

**Runtime action needed:** Restart the user's existing backend on port 8081 from the current `backend` source. Its port 8081 process was left running and untouched because its database environment belongs to the user's running session. The frontend already serves the updated source on port 5173.
