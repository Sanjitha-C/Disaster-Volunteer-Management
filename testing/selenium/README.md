# Selenium browser test

`backend/src/test/java/edu/dvc/selenium/EmergencyWorkflowSeleniumTest.java` automates the user request → admin assignment → volunteer accept/start/complete → feedback submission → admin feedback review path in headless Chrome.

The test is opt-in: set `DVC_SELENIUM_BASE_URL` to the frontend base URL. It expects an isolated backend at port 8082 and frontend at port 5175. The tested backend used the `test,demo` profiles, H2 in-memory storage, and CORS origin `http://localhost:5175`; it did not use or modify the MySQL database. Selenium Manager downloads a compatible ChromeDriver when needed.

Example Windows CMD setup, in separate terminals:

1. Start the backend with the `test,demo` profiles and H2 settings from [`backend/src/test/resources/application-test.properties`](../../backend/src/test/resources/application-test.properties). Set `SPRING_PROFILES_ACTIVE=test,demo`, then run from `backend`:

   ```cmd
   mvn -Dmaven.repo.local="%CD%\target\m2-repository" spring-boot:run "-Dspring-boot.run.arguments=--server.port=8082 --dvc.cors.allowed-origin=http://localhost:5175"
   ```

2. Start the frontend:

   ```cmd
   cd frontend
   set VITE_API_BASE_URL=http://localhost:8082/api
   npm run dev -- --host localhost --port 5175
   ```

3. In another CMD terminal, run:

   ```cmd
   cd backend
   set DVC_SELENIUM_BASE_URL=http://localhost:5175
   mvn -Dmaven.repo.local="%CD%\target\m2-repository" -Dtest=EmergencyWorkflowSeleniumTest test
   ```

The Selenium scenario passed on 2026-10-30. Chrome 156 emitted a CDP version warning (Selenium selected its nearest supported CDP version); it did not prevent the successful standalone scenario. Chrome may exit unexpectedly under a combined full-suite run, so run this focused scenario separately when validating it. Without the environment variable, Maven reports this one scenario skipped and continues the normal backend suite.
