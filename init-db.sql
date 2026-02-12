-- Create databases if they don't exist
SELECT 'CREATE DATABASE customerdb'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'customerdb')\gexec

SELECT 'CREATE DATABASE orderdb'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'orderdb')\gexec