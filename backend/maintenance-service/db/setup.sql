-- Run this in psql (as a superuser) before starting the service.
-- If the database/user already exists, do NOT recreate them —
-- use ALTER USER ... WITH PASSWORD '...' if you need to reset the password.

CREATE USER maintenance_user WITH PASSWORD 'maintenance_password';
CREATE DATABASE maintenance_db OWNER maintenance_user;

-- Tables are created/updated automatically by Hibernate
-- (spring.jpa.hibernate.ddl-auto=update), so no CREATE TABLE
-- statements are needed here.
