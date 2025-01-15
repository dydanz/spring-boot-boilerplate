CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    token_auth VARCHAR(255),
    token_expiry_date TIMESTAMP,
    enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    full_name VARCHAR(255),
    photo_url VARCHAR(512),
    birth_date DATE,
    phone_number VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE user_event_journal (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_name VARCHAR(255) NOT NULL,
    event_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    event_data JSONB,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_token ON users(token_auth);
CREATE INDEX idx_event_user ON user_event_journal(user_id);
CREATE INDEX idx_event_date ON user_event_journal(event_date); 