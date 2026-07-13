-- Event + Venue
SELECT
    e.id,
    e.name,
    v.name AS venue_name,
    v.city
FROM events e
INNER JOIN venues v
ON e.venue_id=v.id;

-- Booking + User
SELECT
    b.id,
    u.name,
    u.email,
    b.seats_booked
FROM bookings b
INNER JOIN users u
ON b.user_id=u.id;

-- Booking + Event
SELECT
    b.id,
    e.name,
    b.seats_booked
FROM bookings b
INNER JOIN events e
ON b.event_id=e.id;

-- Booking + Event + User
SELECT
    b.id,
    e.name AS event_name,
    u.name AS user_name,
    b.seats_booked
FROM bookings b
INNER JOIN events e
ON b.event_id=e.id
INNER JOIN users u
ON b.user_id=u.id;