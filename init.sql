-- Create database if it doesn't exist
CREATE DATABASE boilerplate;

-- Grant all privileges to postgres user
GRANT ALL PRIVILEGES ON DATABASE boilerplate TO postgres;

-- Enable UUID extension for the database
\c boilerplate;  // eslint-disable-line no-eval
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
