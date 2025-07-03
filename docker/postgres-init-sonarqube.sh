#!/bin/bash
set -e

# Default values if environment variables are not set
SONAR_DB_USER=${SONAR_DB_USER:-sonarqube}
SONAR_DB_PASSWORD=${SONAR_DB_PASSWORD:-sonarqube}
SONAR_DB_NAME=${SONAR_DB_NAME:-sonarqube}

echo "Creating SonarQube database and user..."
echo "Database: $SONAR_DB_NAME"
echo "User: $SONAR_DB_USER"

# Create user if not exists
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" -c "
DO \$\$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = '$SONAR_DB_USER') THEN
        CREATE USER $SONAR_DB_USER WITH ENCRYPTED PASSWORD '$SONAR_DB_PASSWORD';
        RAISE NOTICE 'User $SONAR_DB_USER created successfully';
    ELSE
        RAISE NOTICE 'User $SONAR_DB_USER already exists';
    END IF;
END
\$\$;"

# Create database if not exists (pure SQL, no \gexec)
db_exists=$(psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" -tAc "SELECT 1 FROM pg_database WHERE datname = '$SONAR_DB_NAME'")
if [ "$db_exists" != "1" ]; then
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" -c "CREATE DATABASE $SONAR_DB_NAME OWNER $SONAR_DB_USER;"
    echo "Database $SONAR_DB_NAME created."
else
    echo "Database $SONAR_DB_NAME already exists."
fi

# Grant privileges
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" -c "GRANT ALL PRIVILEGES ON DATABASE $SONAR_DB_NAME TO $SONAR_DB_USER;"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" -c "ALTER DATABASE $SONAR_DB_NAME OWNER TO $SONAR_DB_USER;"

echo "SonarQube database setup completed!" 