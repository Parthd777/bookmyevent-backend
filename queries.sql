SELECT *
FROM events;

SELECT *
FROM users;

SELECT *
FROM events
WHERE event_date > CURRENT_TIMESTAMP;

SELECT *
FROM events
ORDER BY event_date;

SELECT *
FROM users
WHERE email='parth@gmail.com';