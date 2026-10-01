// Shape of GET /income-statement. Amounts are decimal strings such as "1234.50".

export type Line = {
  accountNumber: string
  accountName: string
  amount: string
}

export type Section = {
  lines: Line[]
  total: string
}

export type IncomeStatement = {
  start: string
  end: string
  revenue: Section
  costOfGoodsSold: Section
  grossProfit: string
  operatingExpenses: Section
  operatingIncome: string
  otherIncome: Section
  netIncome: string
}
