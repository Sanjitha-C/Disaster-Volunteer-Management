# MySQL Database Setup (Stage 2)

## Data model

The backend JPA entities define the initial DVC schema. The main tables are `user_accounts`, `volunteer_profiles`, `skills`, `volunteer_skills`, `availability_slots`, `emergency_requests`, `emergency_request_skills`, `task_assignments`, `resources`, `notifications`, and `volunteer_feedback`. The entity relationships and constraints are shown in [the ER diagram](../documentation/database/ERD.md).

## Local setup

The Windows `MySQL80` service is running on this machine. The MySQL command-line client was not found on PATH. You can create the database in MySQL Workbench by opening and running `create-database.sql`; alternatively the JDBC URL requests database creation on first connection, if the configured database user has permission.

**Stage 2 runtime verification:** The backend responds on port 8081, and the process listening on that port has established connections to MySQL port 3306. This confirms the running backend connected to MySQL. Hibernate is configured to apply the entity schema in development mode.

Set your own local MySQL username and password in the terminal before starting the backend. Do not commit or paste the password into project files or chat. The `.env.example` file lists the variable names, but Spring Boot does not automatically load that file.

From **Command Prompt (CMD)**:

```cmd
cd /d "C:\Users\Sanjitha\Downloads\New folder (13)\backend"
set "DB_URL=jdbc:mysql://localhost:3306/dvc?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
set "DB_USERNAME=root"
set "DB_PASSWORD=YOUR_LOCAL_MYSQL_PASSWORD"
mvn -Dmaven.repo.local="%CD%\target\m2-repository" spring-boot:run
```

Replace the example password in your local terminal command with the password configured for your MySQL account. If port 8080 is occupied, stop the intended previous DVC process or start the backend with `-Dspring-boot.run.arguments=--server.port=8081` and use that port for the health check.

When it starts successfully, Hibernate's development `update` mode creates or updates the tables. Verify the service at `http://localhost:8080/api/health`, then inspect the `dvc` schema in MySQL Workbench. The health endpoint currently confirms application availability; direct database connection is additionally confirmed by Hibernate startup logs showing successful persistence initialization.

## Development schema behavior

`spring.jpa.hibernate.ddl-auto=update` is for local development. Hibernate can evolve the schema from the entity mappings; it does not drop existing data. For production, use reviewed versioned migrations rather than automatic schema updates. Database credentials are provided by environment variables and are not stored in source control.
