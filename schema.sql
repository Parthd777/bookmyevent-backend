CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE venues (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    capacity INT NOT NULL CHECK (capacity > 0)
);

CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    event_date TIMESTAMP NOT NULL,

    venue_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,

    total_seats INT NOT NULL CHECK (total_seats > 0),

    available_seats INT NOT NULL
        CHECK (available_seats >= 0),

    CONSTRAINT fk_event_venue
        FOREIGN KEY (venue_id)
        REFERENCES venues(id),

    CONSTRAINT fk_event_creator
        FOREIGN KEY (created_by)
        REFERENCES users(id)
);

CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,

    event_id BIGINT NOT NULL,

    seats_booked INT NOT NULL
        CHECK (seats_booked > 0),

    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_booking_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_booking_event
        FOREIGN KEY (event_id)
        REFERENCES events(id)
);

CREATE INDEX idx_users_email
ON users(email);

CREATE INDEX idx_events_date
ON events(event_date);

CREATE INDEX idx_booking_user
ON bookings(user_id);