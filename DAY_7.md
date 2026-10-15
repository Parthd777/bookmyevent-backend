# Day 7: Production-Ready Booking Flow

## What was added and why

| Addition | Why it is here | How it behaves |
| --- | --- | --- |
| Pessimistic event-row lock | Prevent concurrent requests from selling the last seat twice. | `EventRepository.findByIdForUpdate` acquires a database write lock. Every reservation, hold confirmation, and hold expiry locks the event first, then checks or mutates capacity. |
| Explicit booking transactions | Keep capacity changes and booking records consistent. | Booking operations use `READ_COMMITTED`; the event capacity update and booking status/insert commit or roll back together. Lock contention maps to HTTP 409. |
| Temporary seat holds | Represent a checkout that is not yet paid or confirmed. | `POST /bookings/holds` creates `PENDING`, reduces available capacity, and returns `expiresAt`. `POST /bookings/{id}/confirm` confirms a live hold. The customer must own the hold. |
| Scheduled hold cleanup | Return seats when a checkout is abandoned. | A scheduled job finds due `PENDING` records in batches. Each hold is expired in its own transaction; its reserved seats are restored once. |
| Async confirmation notification simulation | Keep notification work off the request thread. | Confirmed bookings publish an application event. An `@Async` listener logs a simulated email only after the database transaction commits. A bounded executor prevents unbounded queue growth. |
| Event-list cache | Reduce repeat reads for the event listing endpoint. | Caffeine caches DTOs for up to 30 seconds. Booking/hold capacity changes and event creation evict the listing cache. |
| Audit timestamps | Make record lifecycle timestamps consistent and automatic. | `BaseEntity` assigns `createdAt` and `updatedAt` in UTC on insert and refreshes `updatedAt` on update. Booking responses expose these fields. |
| Structured operational signals | Make important booking outcomes visible without SQL noise. | Booking/expiry/notification logs use stable `action=...` key-value fields. Micrometer counters track simulated notifications, expired holds, and cleanup failures. Actuator exposes health and metrics. |

## Booking API

All booking endpoints require a customer JWT. The `userId` in the request must match the authenticated customer's email; a customer cannot place or confirm another customer's booking.

Create an immediately confirmed booking (existing behavior):

```http
POST /bookings
Authorization: Bearer <customer-jwt>
Content-Type: application/json

{"userId": 12, "eventId": 34, "seatsBooked": 1}
```

Create a temporary hold (default lifetime: five minutes):

```http
POST /bookings/holds
Authorization: Bearer <customer-jwt>
Content-Type: application/json

{"userId": 12, "eventId": 34, "seatsBooked": 1}
```

The hold response has `status: "PENDING"` and an `expiresAt` timestamp. Confirm it before expiry:

```http
POST /bookings/56/confirm
Authorization: Bearer <customer-jwt>
```

A full event, expired hold, or lock conflict returns HTTP 409. A customer attempting to use another customer's identity or hold receives HTTP 403. A missing event or booking returns HTTP 404.

## Configuration and monitoring

- `BOOKING_HOLD_DURATION`: ISO-8601 duration, defaults to `PT5M`.
- `BOOKING_CLEANUP_DELAY_MS`: delay between cleanup runs, defaults to `30000` milliseconds. Each run processes up to 100 due holds.
- `GET /actuator/health`: application health. Details are intentionally hidden.
- `GET /actuator/metrics`: available Micrometer measurements.
- Logs include examples such as `action=booking_reserved`, `action=booking_rejected`, `action=booking_expired`, and `action=booking_email_simulated`.

Actuator endpoints remain subject to the application's existing authentication rules. Use a valid JWT when calling them.

## Validate it

Run the focused integration tests, which start the full Spring application against an isolated in-memory H2 database:

```powershell
.\mvnw.cmd -Dtest=BookingConcurrencyIntegrationTest test
```

The tests verify:

1. Two simultaneous service calls competing for one seat result in one confirmed booking and one capacity conflict. Available capacity never becomes negative.
2. An expired hold restores its seats exactly once.
3. Only the hold owner can confirm; repeating confirmation does not reserve or release more capacity.

Run the complete project test suite:

```powershell
.\mvnw.cmd test
```

Manual API check:

1. Start the application with PostgreSQL available and log in as a customer to get a JWT.
2. Create a hold with `POST /bookings/holds`; verify `PENDING`, `expiresAt`, and reduced `availableSeats` in `GET /events/{eventId}`.
3. Confirm it with `POST /bookings/{bookingId}/confirm`; verify `CONFIRMED`, a cleared `expiresAt`, and unchanged reserved capacity.
4. To observe expiry, temporarily set `BOOKING_HOLD_DURATION=PT10S`, create another hold, wait for expiry plus one cleanup interval, and verify its status is `EXPIRED` and capacity is restored.
5. Repeat `GET /events` and inspect the cache through the service/Actuator metrics. Create or book an event and repeat the listing; its changed response should reflect cache eviction.
6. Inspect logs for a `notification-` thread and `action=booking_email_simulated` after a successful confirmation. A rolled-back booking must not emit that notification.

The concurrency integration test uses H2 to check the application transaction and locking behavior without requiring a local PostgreSQL server. Before production use, also run the race scenario against PostgreSQL: database lock behavior and isolation are provided by PostgreSQL in deployment, not by an in-process JVM lock.
