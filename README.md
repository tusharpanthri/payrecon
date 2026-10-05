# PayRecon

A payment reconciliation service in **Java 17** and **Spring Boot 3**. It compares payments in an internal ledger
against transactions reported by Stripe, PayPal and banks, scores every possible pair, and reports matches, fees,
amount mismatches, duplicates and missing records through a REST API.

All provider data is simulated. This is a learning and portfolio project, not a production financial system.

## Stack

Java 17, Spring Boot 3.3, Spring Web, Spring Data JPA (Hibernate), Bean Validation, Flyway, PostgreSQL 16,
JUnit 5, MockMvc, H2 (tests only), Swagger UI (springdoc), Docker, Docker Compose, Maven.

## Run it

**With Docker (easiest):**

```bash
docker compose up --build
```

**Locally (needs Java 17+ and Maven 3.9+):**

```bash
docker compose up -d postgres
mvn spring-boot:run
```

Then open:

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

**Run the tests** (no database needed, they use in-memory H2):

```bash
mvn test
```

## Demo in three calls

```bash
# 1. Generate 40 payments plus their provider records (resets all data)
curl -X POST "http://localhost:8080/api/demo/seed?count=40"

# 2. Reconcile everything
curl -X POST http://localhost:8080/api/reconciliations/run

# 3. Look at the problems
curl "http://localhost:8080/api/reconciliations?status=AMOUNT_MISMATCH"
```

## API

| Method | Path | What it does |
|---|---|---|
| POST | `/api/payments` | Create an internal payment (validated, 409 on duplicate reference) |
| GET | `/api/payments?page=0&size=20` | List payments, paginated |
| GET | `/api/payments/{id}` | One payment, 404 if missing |
| POST | `/api/provider-transactions` | Record a Stripe / PayPal / bank transaction |
| GET | `/api/provider-transactions` | List provider transactions, paginated |
| POST | `/api/reconciliations/run` | Run the matching engine, returns a summary |
| GET | `/api/reconciliations?status=...` | List results, optional status filter |
| GET | `/api/reconciliations/{id}` | One result with both sides |
| GET | `/api/reconciliations/summary` | Counts and money at stake per status, match rate |
| POST | `/api/demo/seed?count=40` | Reset and generate demo data (1 to 1000) |

Errors use the standard `ProblemDetail` JSON format (RFC 7807).

## How matching works

Money is stored as `long` minor units (cents), never `double`.

Every payment and provider transaction pair gets a score out of 100:

| Field | Points | Notes |
|---|---|---|
| Reference | 40 | Only counted if both sides have one (banks often do not) |
| Amount | 30 | Payment amount must equal provider net + fee |
| Currency | 10 | |
| Card last 4 | 10 | Only counted if both sides have one |
| Date | 10 | Same day = 10, within 3 days = 5 |

Score = points earned / points possible. Pairs at 65% or above are candidates.

1. Sort candidates by score, best first. Assign each payment and transaction at most once (greedy).
2. A leftover transaction that scored well against an already-taken payment is a **DUPLICATE**.
3. A leftover transaction with no candidate is **MISSING_INTERNAL** (provider has money we cannot explain).
4. A leftover payment is **MISSING_EXTERNAL** (we recorded it, the provider never did).
5. A matched pair is **MATCHED**, **MATCHED_WITH_FEE**, or **AMOUNT_MISMATCH** with the difference in cents.

The engine (`engine/MatchingEngine.java`) has no Spring or database code, so it is tested with plain JUnit.

## Project structure

```
src/main/java/com/tushar/payrecon/
  controller/   HTTP layer only: parse request, call service, return DTO
  service/      Business logic and @Transactional boundaries
  engine/       Pure matching logic
  repository/   Spring Data JPA interfaces
  entity/       JPA entities and enums
  dto/          Request and response records
  exception/    Custom exceptions and the global handler
src/main/resources/db/migration/   Flyway SQL (the schema lives here, not in Hibernate)
```

## Design decisions worth knowing

- **Flyway owns the schema** (`ddl-auto: none`). Hibernate never changes tables.
- **`open-in-view: false`**. Entities are mapped to DTOs inside `@Transactional` service methods.
- **`@EntityGraph`** on reconciliation queries loads both sides in one join, avoiding the N+1 query problem.
- **Summary is a `GROUP BY` in SQL**, not a loop in Java.
- **Reconciliation run is one transaction**: if it fails halfway, the previous results are kept.
- **Uniqueness is checked twice**: a friendly `existsBy...` check, plus the database unique constraint as the
  real guarantee when two requests race.

## Known limitations and next steps

- Scoring compares every payment with every transaction: O(n x m). To scale, first group candidates by
  amount or reference (a hash map, or a SQL join) and only score within each group.
- A provider record with no reference is not penalized for that, so at large volumes an orphan record can
  occasionally win a payment whose amount and date it happens to share (seen at ~1 in 1000 in demo data).
  A fix is to lower confidence when one side has a reference and the other does not.
- Each run reprocesses everything. Real systems reconcile incrementally by date window.
- No authentication yet (planned: Spring Security with JWT).
- No frontend; use Swagger UI.
