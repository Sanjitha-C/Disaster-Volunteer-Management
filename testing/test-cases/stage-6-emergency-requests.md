# Stage 6 - Emergency Requests and Assignments

## Automated integration verification

Three Spring Boot MockMvc integration scenarios pass using isolated H2. The current full suite (Stages 3, 5, 6, and 7) reports 25 tests, 0 failures, 0 errors, 0 skipped.

| Test ID | Scenario | Result |
|---|---|---|
| COORD-01 | Community user submits a request with urgency and optional skills, and reads own request history | PASS |
| COORD-02 | Admin can see request queue; other roles are forbidden | PASS |
| COORD-03 | Admin assigns an available volunteer; duplicate/unavailable assignments fail; volunteer sees own assignment | PASS |

## Frontend verification

`npm run build` succeeded with Vite 6.4.5. Community request submission/history, Admin queue/assignment controls, and Volunteer assigned-task view are connected to the APIs.

## Local runtime check

Restart the backend on port 8081. Sign in as User to submit a request, Admin to assign an available Volunteer, and Volunteer to see the assigned task. Stage integration tests use H2; verify MySQL persistence by refreshing after changes.