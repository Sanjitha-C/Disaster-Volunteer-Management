# Test Cases

Stage-specific cases are recorded in the files in this folder. Stage 9 adds:

- JUnit/MockMvc integration coverage across authentication, volunteer management, requests, operations, and administration.
- Mockito unit coverage for task status transition guards and assignment ownership checks.
- A Selenium browser scenario covering request creation, admin assignment, volunteer task lifecycle, feedback submission, and admin feedback review; see [`testing/selenium/README.md`](../selenium/README.md).
- JaCoCo backend line and branch reporting and Java cyclomatic complexity analysis; see [`testing/reports/README.md`](../reports/README.md).
- Stage 9 case inventory and results: [`stage-9-testing-quality.md`](stage-9-testing-quality.md).
