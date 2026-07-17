# CLAUDE.md — rules for working on this repo

This file steers Claude Code / Cursor / any AI assistant working on this
project. Read it before making changes.

## Non-negotiables

1. **`internet_db` and `intranet_db` are separate datasources.** Never
   merge them into one schema, one `EntityManagerFactory`, or one
   repository package. Customer-submitted data (`internet.*` package) and
   staff workflow data (`intranet.*` package) must stay physically
   separate, connected only by sharing the same `id` value. If a change
   seems to require joining them in a single JPA query, that's a sign the
   change is wrong for this architecture — do the join in Java after two
   separate repository calls instead.

2. **The sync job must stay idempotent.** Any change to `SyncJob` must
   preserve the property that running it twice in a row (or with
   overlapping executions) never creates duplicate rows in `intranet_db`.
   Guard new logic with the existing `existsById` check or an equivalent.

3. **Never hardcode the 7-day threshold as a literal duration in more than
   one place.** It lives in `app.autoclose.threshold-minutes`. If you add
   new code that needs to know "how stale is too stale," inject that
   property — don't retype `7` or `10080` elsewhere. This includes seed
   data: if you add or change seeded appeals meant to demonstrate
   closing/not-closing, derive their timestamps from the injected
   threshold so the `demo` profile's shortened threshold still produces a
   correct demonstration (one appeal closes, one doesn't) instead of
   coincidentally closing everything or nothing.

4. **Status transitions are one-directional in this build:**
   `OPEN → AWAITING_CUSTOMER → CLOSED`. Don't add a path back to `OPEN`
   or a "reopen" endpoint without discussing it first — the spec this was
   built against didn't define what a customer reply looks like, and
   inventing that flow is a scope decision, not a bug fix.

5. **Log one line per row touched, plus one summary line, in both
   scheduled jobs.** This is how a reviewer verifies the jobs did the
   right thing without attaching a debugger — don't remove it "to reduce
   noise."

## Style

- No Lombok — plain getters/setters on entities, so the code reads the
  same whether or not the reader's IDE has annotation processing set up.
- Keep the React side dependency-free beyond `react` + `react-dom` unless
  there's a concrete reason (this app is two panels and three fetch
  calls — it doesn't need a router or a state library).
- Prefer adding a new `@Scheduled` component under `service/` over adding
  scheduling logic inline in a controller or repository.

## Before calling something "done"

- If you touched `AutoCloseJob` or `SyncJob`, re-read `DataSeeder` and
  confirm the seed data still exercises both jobs meaningfully under
  *both* the default profile and the `demo` profile.
- If you touched the JPA config (`InternetDbConfig` / `IntranetDbConfig`),
  double check `@EnableJpaRepositories` still points `basePackages` at the
  right repository package and nowhere else — a copy-paste error here
  silently makes one repository invisible to Spring rather than throwing
  an obvious error.
