# Test Reports

Stage 9 results were generated on 2026-10-30.

- `backend/target/surefire-reports/` contains the Maven test reports.
- `backend/target/site/jacoco/index.html` is the HTML JaCoCo report. Recreate it with `mvn verify` from `backend`; it covers the 30 JUnit/MockMvc/Mockito tests. Selenium is opt-in and skipped when `DVC_SELENIUM_BASE_URL` is unset.
- The 1 Selenium end-to-end scenario passed separately against the isolated H2 server. See [`testing/selenium/README.md`](../selenium/README.md).
- Cyclomatic complexity results and the analyzer command are recorded in [`testing/complexity/stage-9-cyclomatic-complexity.md`](../complexity/stage-9-cyclomatic-complexity.md).

Latest backend suite: 31 tests discovered, 30 passed and the opt-in Selenium test skipped; 0 failures and 0 errors. A separate browser run passed the Selenium scenario.
