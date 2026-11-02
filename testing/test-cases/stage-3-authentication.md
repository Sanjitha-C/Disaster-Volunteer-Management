# Stage 3 Authentication Test Results

Executed using JUnit 5, Spring Boot Test, MockMvc, and an isolated H2 in-memory database. No test connects to MySQL.

**Command:** from `backend/` run `mvn test` (the project's Maven cache can be set with `-Dmaven.repo.local=target/m2-repository`).

| Test ID | Scenario | Expected result | Actual result | Status |
|---|---|---|---|---|
| AUTH-01 | Public health endpoint | HTTP 200 | HTTP 200 | PASS |
| AUTH-02 | Anonymous request to current-user route | HTTP 401 | HTTP 401 | PASS |
| AUTH-03 | Volunteer attempts admin route | HTTP 403 | HTTP 403 | PASS |
| AUTH-04 | Registration without CSRF token | HTTP 403 | HTTP 403 | PASS |
| AUTH-05 | Valid volunteer registration with submitted `ADMIN` role | HTTP 201, forced VOLUNTEER role, BCrypt password, profile created, no hash in response | All asserted | PASS |
| AUTH-06 | Duplicate volunteer email | HTTP 409 | HTTP 409 | PASS |
| AUTH-07 | Invalid registration fields | HTTP 400 with field errors | HTTP 400 with field errors | PASS |
| AUTH-08 | Admin demo login and session | HTTP 200 and authenticated session | Passed | PASS |
| AUTH-09 | Volunteer demo login and session | HTTP 200 and authenticated session | Passed | PASS |
| AUTH-10 | User demo login and session | HTTP 200 and authenticated session | Passed | PASS |
| AUTH-11 | Incorrect demo password | HTTP 401 | HTTP 401 | PASS |
| AUTH-12 | Logout then request current-user route | Session invalidated; HTTP 401 | Session invalidated; HTTP 401 | PASS |
| AUTH-13 | Run demo seeder a second time | No duplicate accounts; existing password hash unchanged | Both asserted | PASS |

**Execution summary:** 13 tests run, 0 failures, 0 errors, 0 skipped. Maven reported `BUILD SUCCESS` on 2026-10-30.
