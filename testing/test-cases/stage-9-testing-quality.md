# Stage 9 testing and quality cases

Executed 2026-10-30. Backend tests use the isolated H2 test profile.

| Test group | Cases | Result |
|---|---|---|
| Authentication (MockMvc) | Login/session, registration, validation, access rules | 13 passed |
| Volunteer management (MockMvc) | Profile, skills, availability | 5 passed |
| Emergency coordination (MockMvc) | Request creation, assignment, ownership | 3 passed |
| Task operations (MockMvc) | Status lifecycle, notifications, feedback, admin resources | 4 passed |
| Admin management (MockMvc) | Reports, role/access changes, guardrails | 3 passed |
| Operations service (Mockito) | Reject invalid transition; reject another volunteer’s assignment | 2 passed |
| Selenium | User request → Admin assignment → Volunteer completion and feedback → Admin review | 1 passed in focused run; opt-in in default Maven suite |

The successful default `mvn verify` run discovered 31 test methods: 30 passed and the opt-in Selenium method was skipped; zero failures/errors. The focused Selenium execution passed all of its assertions. JaCoCo covers the default backend suite; its report currently shows 640/674 lines (94.96%) and 107/170 branches (62.94%).
