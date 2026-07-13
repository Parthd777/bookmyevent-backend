INSERT INTO users
(name,email,password,role)
VALUES
('Admin User',
 'admin@bookmyevent.com',
 'admin123',
 'ADMIN'),

('Parth',
 'parth@gmail.com',
 'password123',
 'CUSTOMER'),

('Rahul',
 'rahul@gmail.com',
 'password123',
 'CUSTOMER');



INSERT INTO venues
(name,city,capacity)
VALUES
('Hyderabad Convention Center',
 'Hyderabad',
 500),

('Bangalore Arena',
 'Bangalore',
 1000);



INSERT INTO events
(
 name,
 event_date,
 venue_id,
 total_seats,
 available_seats,
 created_by
)
VALUES
(
 'Java Conference 2026',
 '2026-08-15 10:00:00',
 1,
 500,
 500,
 1
),

(
 'Spring Boot Summit',
 '2026-09-10 09:00:00',
 2,
 1000,
 1000,
 1
);



INSERT INTO bookings
(
 user_id,
 event_id,
 seats_booked,
 status
)
VALUES
(
 2,
 1,
 2,
 'CONFIRMED'
),

(
 3,
 2,
 4,
 'CONFIRMED'
);