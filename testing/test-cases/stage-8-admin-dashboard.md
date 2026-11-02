# Stage 8 - Dashboards and Administration

## Automated integration verification

Three Stage 8 Spring Boot MockMvc scenarios pass using isolated H2. Current complete suite: **28 tests run, 0 failures, 0 errors, 0 skipped**.

| Test ID | Scenario | Result |
|---|---|---|
| ADMIN-01 | Admin dashboard reports aggregate counts and status groupings; other roles are forbidden | PASS |
| ADMIN-02 | Admin lists accounts, changes account access, and Volunteer promotion creates a profile | PASS |
| ADMIN-03 | Current Admin cannot remove own access; only an Admin can manage accounts | PASS |

## Frontend verification

`npm run build` succeeded with Vite 6.4.5 (103 modules transformed). Admin overview charts and user-management table are connected to the Stage 8 APIs.

## Runtime check

Restart backend on port 8081. Sign in as Admin to check summary counts and change a test account's role/status. Confirm a promoted Volunteer can access the Volunteer pages after signing in again. Backend test database is H2; verify local MySQL persistence separately.