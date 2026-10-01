# Income Statement

## Requirements

`README.md` is the take-home prompt and the source of truth for requirements. Accounting
terms are defined in `ACCOUNTING_PRIMER.md`. Read both before changing behavior.

A simple full-stack app: the backend reads a ledger (chart of accounts and journal entries)
from a JSON file and returns an income statement for a date range; the frontend shows that
report.

What shapes every decision:

- The budget is 2 hours. Correct numbers on a plain page beat an elaborate page with wrong numbers.
- The code is extended by hand, without AI, in a pairing session. Write plain, explicit code
  that the author can explain line by line and change quickly.
- The stack is chosen for familiarity, not trend. Do not suggest switching technologies.

## Layout

This is the target shape. Create files when a step needs them, not before.

```text
NOTES.md        Decisions, assumptions, Q1 2026 net income, how the numbers were checked
backend/        Spring Boot, Java 21, Maven wrapper
                controller → service → ledger loader, records for the ledger and the response
  src/main/resources/ledger.json
                The data from the README. Same content, journal entries sorted by date.
                Code must not rely on that order.
frontend/       React + Vite + TypeScript, npm
  src/
  ├── main.tsx              # Entry point
  ├── App.tsx               # Date inputs, fetch state (loading, error, data)
  ├── api.ts                # The only place that calls the backend; response types
  ├── IncomeStatement.tsx   # Renders the report
  ├── format.ts             # Amount formatting
  └── styles.css
```

## Commands

Backend (from `backend/`):

```sh
./mvnw spring-boot:run     # http://localhost:8080
./mvnw test
curl 'localhost:8080/income-statement?start=2026-01-01&end=2026-03-31'
```

Hand-test requests for the VS Code REST Client extension live in
`backend/src/test/http/request.http`. Keep it in step with the endpoint: add a request for
each new case worth checking by hand.

Frontend (from `frontend/`):

```sh
npm run dev                # http://localhost:5173, proxies /income-statement to :8080
npm run build
npm run lint
```

## Accounting rules

These come from the README's data dictionary. The ledger contains entries that exist to
test each one, so do not simplify them away.

- **Status.** Only `posted` entries count. `draft` and `void` are excluded (JE-009, JE-019, JE-025).
- **Dates.** Filter by the entry `date`. `start` and `end` are both inclusive. There is no
  time or time zone.
- **Which accounts.** Only accounts whose `subtype` is not `balance_sheet`. Balance sheet
  lines in the same entry are ignored.
- **Sections come from `subtype`**, not from `type` or the account number:

  | `subtype` | Section |
  | --- | --- |
  | `operating_revenue`, `contra_revenue` | Revenue |
  | `cogs` | Cost of goods sold |
  | `operating_expense` | Operating expenses |
  | `other_income` | Other income (7000 has type `revenue` but is not in Revenue) |

- **Sign.** For `type` `revenue`, amount = credits − debits. For `type` `expense`,
  amount = debits − credits.
- **Contra revenue** (4900) is a line inside Revenue that reduces it, shown as a negative amount.
- **Credits to an expense account reduce the expense** (JE-020). Lines and totals can be negative.
- **Subtotals.**
  - Gross profit = Revenue − Cost of goods sold
  - Operating income = Gross profit − Operating expenses
  - Net income = Operating income + Other income
- **Inactive accounts.** `is_active: false` only blocks new postings. An inactive account
  with activity in the range is still reported (6300).
- **As recorded.** Do not re-accrue, spread or reclassify anything (JE-007 rent stays in January).
- **Ambiguity.** Make a decision, write it in `NOTES.md`, and move on. Never resolve it silently.

## Rules

### Simplicity

- Simple is enough. Do not over-engineer.
- No new layer, library or abstraction unless a current requirement needs it.
- No database, auth, caching, Docker or CI unless asked.
- No features beyond the README's list.

### Backend

- Java 21. Use records for data and DTOs.
- Structure is controller → service → ledger loader. Nothing more.
- Constructor injection only, no field injection.
- The endpoint is `GET /income-statement?start=YYYY-MM-DD&end=YYYY-MM-DD`, as the README specifies.
- Money is `BigDecimal`, never `double` or `float`. Parse the ledger's `debit` and `credit`
  strings straight into `BigDecimal`. Amounts go out as strings with 2 decimals.
- Dates are `LocalDate` in ISO format (`YYYY-MM-DD`).
- Invalid input returns `400` with a short, clear message: a missing parameter, a malformed
  date, or `start` after `end`.
- A range with no activity returns `200` with zero totals, not an error.
- The response has each section with its lines (account number, name, amount) and subtotal,
  plus gross profit, operating income and net income. The backend does all the arithmetic.

### Frontend

- Function components and hooks. State is `useState`; no Redux, router or UI library.
- All backend calls live in `src/api.ts`.
- Plain CSS.
- Show the backend's amounts as they are. Do not recompute totals or parse amounts into
  `number` for arithmetic.
- The report must be readable by an accountant: clear sections, a line per account,
  right-aligned amounts, totals stand out, negatives shown the same way everywhere.
- Always handle loading, error and empty states.

### Testing

- Tests must catch a real accounting mistake, not only a typo. Service tests come first:
  - `draft` and `void` entries excluded
  - date boundaries inclusive (2025-12-15 out, 2026-03-31 in, 2026-04-01 out for Q1 2026)
  - contra revenue reduces revenue; a credit to an expense reduces the expense
  - balance sheet accounts excluded; inactive account with activity included
  - exact decimals, no rounding drift
  - a range with no activity
- One test asserts the full Q1 2026 statement against figures worked out by hand from the
  ledger, never copied from the app's own output.
- Every public service and loader method and the HTTP endpoint has tests. Records and the
  Spring main class do not need their own.
- Use JUnit 5 with AssertJ assertions (`assertThat`), not JUnit's `assertEquals`.
- Test the controller with MockMvc, including each `400` case.

### Definition of done

- `./mvnw test` passes; `npm run build` and `npm run lint` pass.
- The change was actually run and checked (curl for the API, browser for the page), not only compiled.
- Report what was verified and what was not. Never claim something works without running it.
- `NOTES.md` is updated if the step involved a decision or an assumption about the data.
- The run instructions and tool versions for the submission are still correct.

### Workflow

- Work one step at a time. Finish and verify a step, then stop for review before the next one.
- Plan before large changes; do not execute until the plan is approved.
- Commit after each verified step, with a message that describes that step. Reviewers want
  real history, not one squashed commit.
