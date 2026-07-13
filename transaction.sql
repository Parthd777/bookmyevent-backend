BEGIN;

UPDATE events
SET available_seats = available_seats - 2
WHERE id = 1
AND available_seats >= 2;

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
);

COMMIT;

-- Rollback
BEGIN;

UPDATE events
SET available_seats = available_seats - 2
WHERE id = 1;

ROLLBACK;