# Hyperproof Risk Register

A small full-stack Risk Register: a Spring Boot API + a React/TypeScript dashboard for
creating risks, scoring them, attaching mitigations, and seeing everything prioritized
by severity.

> **Note on this build**: this was produced in a sandboxed environment with no access
> to Maven Central, so the backend could be written carefully but not compiled here.
> The frontend *was* built and typechecked successfully (`npm run build` passes clean).
> Please run `mvn test` as your first step — see below.

---

## Setup and run (should take < 5 minutes)

### Prerequisites
- Java 17+
- Maven 3.9+ (or use the included wrapper if you add one)
- Node 18+
- npm 9+

### 1. Backend

```bash
cd backend
mvn spring-boot:run
```

This starts the API on `http://localhost:8080`, backed by a file-based H2 database
(`backend/data/riskregister.mv.db`, auto-created). No external DB setup needed.

To run the tests:

```bash
mvn test
```

To use real Postgres instead of H2 (see rationale below):

```bash
docker run --name risk-register-db -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=riskregister -p 5432:5432 -d postgres:16

mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

### 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. It talks to the API at `http://localhost:8080/api` by
default (see `.env.example` — copy to `.env` to override via `VITE_API_BASE_URL`).

---

## The residual-risk formula

**Inherent score** = `likelihood × impact` (1–25), as specified.

**Residual score**: I modeled each mitigation as removing a *fraction* of the
remaining risk, and combined mitigations multiplicatively rather than by summing
raw point reductions:

```
remainingFraction = PRODUCT over each mitigation m of (1 - (effectiveness(m) / 5) * 0.8)
residual = max(1, round(inherent * remainingFraction))
```

Why this shape, specifically:

- **Zero mitigations → residual = inherent.** An empty product is `1`, so
  `remainingFraction = 1` and residual falls out equal to inherent automatically —
  no special-casing needed.
- **One highly effective mitigation is meaningfully lower.** An effectiveness-5
  control removes 80% of the score (`0.8` is the max fraction any single control
  can remove), so a Critical 25 drops to a Low 5. Effectiveness scales linearly
  within that ceiling, so an effectiveness-1 control barely moves the needle
  (~16% reduction) — a weak control should behave like a weak control.
- **Stacking controls has diminishing returns, not linear stacking.** Multiplying
  survival fractions (`0.4 × 0.4`, not `1 - (0.6 + 0.6)`) means the second mitigation
  helps less than the first in absolute terms, which matches how compliance teams
  actually think about layered controls — the first firewall rule catches most of
  the obvious cases; the fifth overlapping one catches less.
- **Residual can never exceed inherent or reach 0.** The fraction is always in
  `(0, 1]` by construction (each per-control factor is in `(0.2, 1]`, well-formed
  products of numbers in that range stay in that range), and the explicit
  `max(1, ...)` floor guarantees the "never below 1" requirement even after rounding.

I considered a simpler additive model (`residual = inherent - sum(reduction_i)`), but
rejected it because it can produce negative or zero-clamped-everywhere-looking scores
with just two or three moderate mitigations, and it doesn't capture diminishing
returns at all — a fourth mediocre control would remove exactly as much as the first,
which isn't how defense-in-depth works.

## Severity bands
Applied identically to inherent and residual scores, as specified: Low 1–5,
Medium 6–12, High 13–19, Critical 20–25. This lives in `Severity.fromScore()` and is
unit-tested at every band boundary (5→6, 12→13, 19→20) since off-by-one errors here
are the easiest way to mis-classify a risk.

## Business rule: closing a risk with zero mitigations

**Decision: reject it.** Attempting to `PUT` a risk to `status: CLOSED` while it has
no mitigations returns `409 Conflict` with a message explaining why, and suggesting
`MITIGATING` as the status to use while controls are still being put in place.

Reasoning: in a compliance product, "Closed" is a claim you're making to an auditor —
it says *this risk has been addressed*. A risk with zero recorded mitigations and a
Closed status is indistinguishable from a risk someone dismissed without doing
anything, which is exactly the failure mode compliance tooling exists to prevent.
Silently allowing it would make the tool complicit in that failure. I did *not*
require the mitigations to be "effective enough" (e.g. residual below some
threshold) — that's a much fuzzier judgment call that varies by organization and risk
appetite, and it's easy to route around by adding a token low-effectiveness
mitigation regardless of the threshold. Requiring *at least one documented
mitigation* is a floor, not a guarantee of quality, but it's an honest, enforceable
one: it forces the mitigating action to be visible and auditable rather than
silently rubber-stamped.

## Key assumptions and trade-offs

- **Database: H2 (file-based) instead of Postgres by default.** The assignment
  explicitly allows this as a local-setup simplification. A `postgres` Spring
  profile is included and wired up (`application-postgres.properties`) — switching
  is a one-flag change, not a rewrite, because everything goes through Spring Data
  JPA rather than anything H2-specific.
- **Sorting by a computed field.** Residual score isn't a persisted column — it's
  derived from likelihood, impact, and each mitigation's effectiveness at read time.
  Filtering (category, status) happens in the database via a JPA `Specification`;
  sorting by residual score happens in-memory in `RiskService` after mapping to
  DTOs, since the database can't sort by a value it doesn't store. At the size a
  single organization's risk register will ever reach (hundreds to low thousands of
  rows, not millions), this is fine. At real scale, I'd either persist
  `residualScore` as a column recomputed on every write (denormalize for
  read-performance) or push the scoring formula into a SQL/materialized view.
- **No authentication, single implicit org** — per the "out of scope" list.
- **IDs are UUID strings, not auto-increment longs** — makes the API safer to expose
  publicly later (no enumerable sequential IDs) at negligible cost here.
- **Mitigation endpoints return the parent `RiskResponse`, not a bare
  `MitigationResponse`.** Every mitigation write changes the risk's residual score,
  so the frontend needs the recomputed risk anyway — returning it directly saves a
  second round trip and avoids a window where the UI shows a stale residual score.
- **Validation is enforced at the DTO layer** (`@Min(1) @Max(5)` on likelihood,
  impact, effectiveness) with a global exception handler that turns
  `MethodArgumentNotValidException` into a `400` with a field-level message list,
  so "reject invalid input with a clear message" is satisfied uniformly rather than
  per-endpoint.
- **CORS** is scoped to `http://localhost:5173` (the Vite dev server) only — fine for
  local dev, would need widening/config for any real deployment.
- **Edge cases intentionally not handled**: concurrent edits (no optimistic
  locking/versioning on `Risk`), pagination on the risk list (fine at expected
  scale, would matter past a few thousand risks), and partial updates (the `PUT`
  endpoint expects the full risk payload, not a `PATCH`-style partial merge).

## What I'd do with more time

- Add optimistic locking (`@Version` on `Risk`) so two people editing the same risk
  don't silently clobber each other.
- Persist `residualScore` as a denormalized column with a recompute-on-write trigger,
  so sorting/filtering by it can happen in the database and the API can paginate.
- The two stretch goals I skipped in favor of a solid core: a hardcoded NIST
  CSF/SOC 2 category mapping surfaced in the UI, and a "next review date" field with
  an overdue indicator.
- Loading/error states are present but minimal (no retry, no optimistic UI updates on
  mutation) — I'd add those next along with a toast/notification system instead of
  inline banners.
- More integration test coverage: mitigation update/delete endpoints, the
  category+status combined filter, and a test asserting the 400 response shape for
  every invalid field (currently only `likelihood` is explicitly tested end-to-end;
  the exhaustive range is covered at the unit level instead).
- A confirm-dialog component instead of the browser's native `confirm()` for
  deleting a risk.

## Automated tests

```bash
cd backend
mvn test
```

Covers:
- `ScoringServiceTest` — inherent score arithmetic, every sanity check called out in
  the brief (zero mitigations = inherent, one strong mitigation meaningfully lower,
  never below 1, never above inherent, diminishing returns when stacking), and every
  severity band boundary.
- `RiskFlowIntegrationTest` — the full create risk → add mitigation → fetch → verify
  residual score flow end-to-end via `MockMvc`, plus validation-error shape, the
  close-with-no-mitigations business rule, and sort-by-residual-score ordering.
