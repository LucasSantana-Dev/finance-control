-- Create SonarQube database
CREATE DATABASE sonar;

-- Grant privileges to the application user
GRANT ALL PRIVILEGES ON DATABASE sonar TO ${DB_USERNAME};

-- Connect to sonar database and create extensions
\c sonar;

-- Create required extensions for SonarQube
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm"; 