# Stage 7 - Task Operations and Supporting Features

## Automated integration verification

Four Stage 7 MockMvc integration scenarios pass using isolated H2. Full current suite: **25 tests run, 0 failures, 0 errors, 0 skipped**.

| Test ID | Scenario | Result |
|---|---|---|
| OPS-01 | Volunteer advances an owned task through allowed statuses; requester receives updates and can read notifications | PASS |
| OPS-02 | Other volunteers cannot update or leave feedback on a task; invalid status transition is rejected | PASS |
| OPS-03 | Admin can create/list resources and a community user is forbidden | PASS |
| OPS-04 | Completed-task feedback is saved once and is visible to Admin | PASS |
| OPS-05 | Admin can cancel an active assignment; arbitrary status changes are rejected | PASS |

## Frontend verification

`npm run build` succeeded with Vite 6.4.5 (103 modules transformed). Volunteer task progress/feedback, notifications, Admin resource inventory, and Admin feedback review pages are connected.

## Local runtime check

Restart the backend on port 8081. Sign in as Volunteer and progress a task to completion, review notifications as the requester, and submit feedback. Sign in as Admin to manage resources and review feedback. Tests use H2; verify persistence against configured MySQL after restart.
