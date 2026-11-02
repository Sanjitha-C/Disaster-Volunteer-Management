# Stage 10: Final integration and review

Review date: 2026-10-30.

## Integrated feature check

All 15 feature stories are marked verified in [`FEATURE_TRACEABILITY.md`](../FEATURE_TRACEABILITY.md). The cross-role emergency workflow was exercised by the Stage 9 Selenium scenario against the isolated H2 backend and local frontend: user request → admin assignment → volunteer accepts, starts, and completes → volunteer submits feedback → admin reviews it.

## Final checks

| Check | Result |
|---|---|
| Backend automated suite (`mvn verify`) | 30 JUnit/MockMvc/Mockito tests pass; Selenium is opt-in and skipped by default |
| Selenium browser workflow | Passed in a focused run; see [`testing/selenium/README.md`](../testing/selenium/README.md) |
| Frontend production build (`npm run build`) | Passed with Vite 6.4.5; 103 modules transformed |
| JaCoCo | 94.96% line coverage; 62.94% branch coverage for the default backend test suite |
| Complexity analysis | 78 functions; average CCN 1.9; two functions exceed CCN 10; see [report](../testing/complexity/stage-9-cyclomatic-complexity.md) |
| Local MySQL | Backend connection was verified in Stage 2; recent integration tests ran on isolated H2 |

## Setup and operations review

- The root [`README.md`](../README.md) provides Windows CMD startup instructions for the frontend and backend.
- Database variables are documented in `.env.example` and [`database/README.md`](../database/README.md). Spring Boot does not automatically read `.env.example`; supply variables in the process environment.
- Demo accounts are for local development. The demo seeder should be disabled with the `prod` profile outside local development.
- Backend targets Java 21; the machine used for review has Java 26 and Maven 3.9.14. Maven Wrapper is not configured.

## Known limits

- A combined Maven run with Selenium enabled had a Chrome disconnect once. The dedicated Selenium workflow passed separately, and the default backend suite remains green. Run the focused browser scenario separately if Chrome behaves inconsistently.
- No versioned production database migration tool or hosted deployment configuration is included. Hibernate schema update is for local development only.
- Current checks do not establish production readiness, external disaster-response integrations, or real-world operational suitability.

**Stage 10 status:** Final documentation, integration review, and local build verification are complete. All ten project stages are verified and ready for user review.

## Follow-up after user reported stale screens

The current source now routes Volunteer Task History and Feedback to live data and exposes the volunteer's submitted feedback list. The running backend on port 8081 was verified stale: its Admin reports and users routes return 404. Current source endpoints returned 200 on a temporary H2 server. Restart the backend from the current `backend` directory to load these fixes; the existing 8081 process was not stopped so its configured database session remains undisturbed. Frontend build passed; backend package compilation passed with tests skipped.
