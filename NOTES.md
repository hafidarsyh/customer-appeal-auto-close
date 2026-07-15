# Notes

## Ambiguities and decisions

- **Linking the two DBs.** Spec says "linked to the customer appeal by
  id" but doesn't say how. I used the *same primary key value* in both
  tables rather than a generated intranet id + a separate `sourceAppealId`
  foreign-key-style column. Simpler, and the id genuinely is a natural key
  shared across both systems in this scenario.
- **What happens if the customer *does* reply within 7 days?** Not
  specified. There's no "customer reply" inbound channel in this build
  (the customer only ever has the one write path — `POST /appeals` — and
  that's for *new* appeals, not replies to an existing one). I left
  `AWAITING_CUSTOMER` as a dead end with no path back to `OPEN`, since
  building a full reply/reopen flow wasn't asked for. Flagged this in
  TECHNICAL.md rather than silently guessing at a reopen endpoint.
- **7 days vs demo speed.** Rather than hardcoding "7 days" and a separate
  "2 minutes for demo," I made the threshold and both job intervals
  externalized properties, and made the seed data's timestamps computed
  *relative to the configured threshold* rather than a fixed "8 days ago."
  That way `--spring.profiles.active=demo` produces a correct demo
  (one closes, one doesn't) instead of both or neither closing.
- **Auth.** Spec doesn't mention it; left the staff API open (CORS `*`)
  since this is a demo, called out as a simplification in TECHNICAL.md.

## How I'd test the cron jobs (not closing the wrong ones)

- **Unit level:** test `AutoCloseJob` with a fake/in-memory
  `IntranetAppealRepository` seeded with four rows — one `AWAITING_CUSTOMER`
  just over the threshold, one just under, one `OPEN`, one already
  `CLOSED` — and assert only the first one flips to `CLOSED` after
  `closeStaleAppeals()` runs. That's the sharpest test: it directly checks
  the boundary and that status filtering is respected.
- **Repository level:** an `@DataJpaTest` against the real H2
  `intranet_db` asserting `findByStatusAndRespondedAtBefore` returns
  exactly the expected ids for a seeded set of rows with `respondedAt`
  timestamps straddling the cutoff.
- **Idempotency for the sync job:** call `syncNewAppeals()` twice in a row
  in a test and assert `intranetRepo.count()` doesn't change on the second
  call, and no duplicate ids exist.
- **End-to-end/manual:** what this repo's `demo` profile is for — seed
  data timed relative to a short threshold, watch the console log the
  exact ids it closes each run, cross-check against `GET
  /api/staff/appeals`.

## Where AI helped / where it got in the way

**Helped:** scaffolding the dual-datasource Spring Boot config (easy to get
subtly wrong — bean naming, `@Primary`, `@EnableJpaRepositories`
`entityManagerFactoryRef`/`transactionManagerRef` wiring), and generating
the first pass of the React list/detail components so I could focus on the
business logic (idempotent sync, threshold-relative seed data) instead of
boilerplate.

**Got in the way:** the first draft had the auto-close job hardcode "7
days" directly in both the job and the seed data, which meant the demo
profile's shortened interval didn't actually demonstrate anything (nothing
would go stale fast enough, or everything would). Had to explicitly redirect
it to derive seed timestamps from the configured threshold instead — a good
example of AI producing something that *looks* right and runs without
errors but doesn't actually prove the behavior it's meant to demonstrate.

## AI conversation snippet

> **Me:** the auto-close job works but my demo profile doesn't actually
> show one appeal closing and one staying open — they either both close or
> neither does.
>
> **AI:** That's because your seed data hardcodes `respondedAt` as
> `now.minusDays(8)` and `now.minusDays(2)`, but the demo profile's
> threshold is 2 minutes — both are "8 days" and "2 days" which are both
> way more than 2 minutes, so both look stale. Compute the seed timestamps
> from the injected `threshold-minutes` property instead, e.g.
> `now.minusMinutes(thresholdMinutes * 2)` for the one that should close
> and `now.minusMinutes(thresholdMinutes / 4)` for the one that shouldn't —
> then it's correct regardless of which profile is active.
>
> **Me:** do that, and inject threshold-minutes into DataSeeder via
> @Value the same way AutoCloseJob does.
