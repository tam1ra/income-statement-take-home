# Income statement

A small full-stack app that shows the income statement of Northwind Coffee Roasters for any
date range. The backend reads the ledger and does all the arithmetic; the frontend shows
the report.

- The take-home prompt and the accounting primer are in [requirements/](requirements/).
- Decisions, assumptions and the Q1 2026 net income are in [NOTES.md](NOTES.md).

## What you need

| Tool | Version used |
| --- | --- |
| Java (JDK) | 21.0.12 |
| Node.js | 24.21.0 |
| npm | 11.19.0 |

Maven does not need to be installed. The wrapper (`./mvnw`) downloads Maven 3.9.16 on first use.

Main libraries: Spring Boot 4.1.1, React 19.2, Vite 8.3, TypeScript 6.0.

## Run the backend

```sh
cd backend
./mvnw spring-boot:run
```

It listens on http://localhost:8080. Try it:

```sh
curl 'http://localhost:8080/income-statement?start=2026-01-01&end=2026-03-31'
```

Both dates are required, in `YYYY-MM-DD` format, and both are inclusive. A missing or
invalid date, or a start after the end, returns `400` with a short message.

## Run the frontend

```sh
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. The backend must be running: the dev server forwards
`/income-statement` to http://localhost:8080.

## Run the tests

```sh
cd backend
./mvnw test
```

```sh
cd frontend
npm run build
npm run lint
```

The frontend has no test suite: it does no arithmetic and displays the backend's amounts as
they are.

## Try requests by hand

[backend/src/test/http/request.http](backend/src/test/http/request.http) has a request for
each interesting date range and each invalid input, with the expected result in a comment.
It works with the VS Code REST Client extension (`humao.rest-client`).

## Where things are

```text
requirements/   The take-home prompt and the accounting primer
NOTES.md        Decisions, Q1 2026 net income, how the numbers were checked
backend/        Spring Boot. controller → service → ledger loader
  src/main/resources/ledger.json   The ledger data
frontend/       React + Vite + TypeScript
```

The path from the dates to the numbers is in
[IncomeStatementService.java](backend/src/main/java/com/example/incomestatement/IncomeStatementService.java).
