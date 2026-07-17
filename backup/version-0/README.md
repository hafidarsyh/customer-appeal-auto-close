# Customer Appeal Auto-Close

A council intranet demo: customers submit appeals via API, staff respond
through a React page, and appeals that go unanswered for 7 days after an
officer response auto-close. Built on two separate in-memory H2 databases
(`internet_db` and `intranet_db`) to mirror a real public/internal split.

See also: [FUNCTIONAL.md](FUNCTIONAL.md) (non-technical walkthrough),
[TECHNICAL.md](TECHNICAL.md) (architecture, endpoints, schema, jobs),
[NOTES.md](NOTES.md) (assumptions, testing approach, AI usage).

## Stack

- **Backend:** Java 17, Spring Boot 3.2 (Web, Data JPA, Scheduling), two H2
  in-memory datasources
- **Frontend:** React 18 + Vite, plain fetch (no extra state library)

## Prerequisites

- Java 17+ and Maven (or use `./mvnw` if you add the wrapper — not bundled
  here since this sandbox had no Maven Central access to verify a wrapper
  download; `mvn` on your machine works fine)
- Node.js 18+ and npm

## Running the backend

```bash
cd backend
mvn spring-boot:run
```

This starts on `http://localhost:8080` and seeds both H2 databases with 6
sample appeals covering every stage (see `DataSeeder.java` and NOTES.md for
what each one demonstrates).

Both H2 consoles are enabled for inspection:

- `http://localhost:8080/h2-console` — JDBC URL `jdbc:h2:mem:internet_db` or
  `jdbc:h2:mem:intranet_db`, user `sa`, empty password. **Note:** because
  each datasource is a separate in-process H2 instance, you can only
  connect to one at a time per running app (whichever URL you type in).

### Running with fast cron intervals (for demo/review)

By default the sync job runs every 5 minutes and the auto-close job runs
once a day with a 7-day (10080 minute) staleness threshold — matching the
spec. To see both jobs fire quickly instead:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

This applies `application-demo.yml`, which shortens the sync job to every
20 seconds, the auto-close job to every 30 seconds, and the staleness
threshold to 2 minutes. The seed data's "responded X ago" timestamps for the
two AWAITING_CUSTOMER sample appeals are computed **relative to whatever
threshold is configured**, so the demo still correctly closes the stale one
and leaves the recent one alone (see `DataSeeder.java`).

Watch the console log — both jobs log one line per appeal they touch.

## Running the frontend

```bash
cd frontend
npm install
npm run dev
```

Opens on `http://localhost:5173` and talks to the backend at
`http://localhost:8080` (hardcoded base URL in `src/api.js` — fine for a
demo, would move to an env var for real deployment).

## Trying the public submission endpoint

There's no public UI (not required by the spec). Submit a new appeal with:

```bash
curl -X POST http://localhost:8080/appeals \
  -H "Content-Type: application/json" \
  -d '{"customerName":"Grace Kim","subject":"Speeding fine","message":"I was not speeding, the camera misread my plate."}'
```

It will only appear on the staff page after the next sync job run (or
almost immediately if you're running the `demo` profile).

## Project layout

```
backend/    Spring Boot API, jobs, two datasources
frontend/   React staff intranet page
FUNCTIONAL.md
TECHNICAL.md
NOTES.md
CLAUDE.md   Rules file used to steer the AI while building this
```
