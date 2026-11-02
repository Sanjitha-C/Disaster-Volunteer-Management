-- Run this in MySQL Workbench while connected to your local MySQL Server.
CREATE DATABASE IF NOT EXISTS dvc
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

-- Tables are created from the JPA entities when the Spring Boot app starts.
-- The DB user needs permission to create and alter tables in the dvc database.
