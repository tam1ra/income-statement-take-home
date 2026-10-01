# Notes

## Net income for Q1 2026

**-44,480.14 USD** for 2026-01-01 to 2026-03-31, a net loss.

| | |
| --- | ---: |
| Revenue | 37,850.50 |
| Cost of goods sold | 14,272.75 |
| Gross profit | 23,577.75 |
| Operating expenses | 68,100.07 |
| Operating income | (44,522.32) |
| Other income | 42.18 |
| Net income | (44,480.14) |

## Decisions and assumptions

- **Only `posted` entries count.** `void` (JE-009, JE-025) and `draft` (JE-019) are left out.
  A draft is not in the books until it is approved, so the 5,000.00 bonus is not an expense yet.
- **Both dates are inclusive.** Entries dated on `start` or on `end` are in the period.
- **The section comes from `subtype`, not `type`.** Interest Income (7000) has type `revenue`
  but subtype `other_income`, so it is under Other income and not in Revenue or Operating income.
- **Contra revenue is a negative line inside Revenue.** Returns and discounts (4900) reduce
  total revenue instead of appearing as an expense.
- **The sign comes from the account type.** Revenue is credits minus debits; expenses are
  debits minus credits. A credit to an expense (JE-020, vendor credit) reduces that expense,
  and can make the line negative if the period holds only the credit.
- **Inactive accounts are still reported.** `is_active` only says whether new entries can be
  posted. Marketing (legacy, 6300) has a posted entry in January, so it appears.
- **Entries are reported as recorded.** The 9,000.00 rent (JE-007) covers January to March
  but is expensed on 2026-01-01, so all of it is in January.
- **Accounts with no activity in the period have no line.** A section with no lines shows
  "No activity" and a 0.00 total. A period with nothing in it returns `200` with zeros.
- **Amounts are exact.** The ledger's strings are read straight into `BigDecimal`, and the
  API returns strings with 2 decimals. The frontend formats the text and does no arithmetic.
- **An entry that names an unknown account stops the request with an error**, instead of
  being skipped. The ledger has none.
- **`ledger.json` is the README's data with the journal entries sorted by date**, to make it
  easier to read. Nothing else was changed, and the code does not rely on the order.
- **Not checked at load:** that each entry's debits equal its credits. All 25 do today.

## How the numbers were checked

- The Q1 figures were added up by hand from the journal entries before being written into
  the test, not copied from the app's output.
- January, February and March were worked out separately, and add up to the quarter.
- Each rule above has a test on a date range that isolates the entry it concerns.
- The running API was compared with a separate recomputation from `ledger.json` over 15
  date ranges. The same ranges are in `backend/src/test/http/request.http`.

## Where AI helped, and where it was wrong

TODO

## With more time

TODO
