# Stage 5 - Volunteer Management Test Cases

## Automated integration verification

Executed `mvn test` in `backend/` with the isolated H2 test profile. Result: **18 tests run, 0 failures, 0 errors, 0 skipped**. This includes 13 Stage 3 authentication tests and the five integration scenarios below. Stage 5's backend integration tests use H2, not the local MySQL instance.

| Test ID | Scenario | Result |
|---|---|---|
| VOL-01 | Volunteer reads own profile and updates contact details; completion percentage is recalculated | PASS |
| VOL-02 | Profile routes reject anonymous users and non-volunteer roles | PASS |
| VOL-03 | Volunteer adds, edits, and removes a skill; duplicate skill membership is rejected | PASS |
| VOL-04 | A volunteer cannot edit or remove another volunteer's skill | PASS |
| VOL-05 | Volunteer reads/updates availability status and creates, edits, and removes time slots | PASS |
| VOL-06 | Invalid/reversed or overlapping slots are rejected; Admin can view availability and Volunteer access is forbidden | PASS |

## Frontend verification

`npm run build` succeeded using Vite 6.4.5; 99 modules transformed. Dashboard routing connects the Volunteer overview, profile, skills, availability, and Admin volunteer availability views.

## Remaining runtime check

Restart the backend on port 8081 to load the new Stage 5 controllers, sign in as the demo Volunteer, and exercise profile/skill/availability changes. Sign in as Admin to check the read-only volunteer availability list. Confirm saved changes remain after a page reload to verify persistence against the configured MySQL database.