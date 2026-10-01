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

export async function fetchIncomeStatement(start: string, end: string): Promise<IncomeStatement> {
  const params = new URLSearchParams({ start, end })

  let response: Response
  try {
    response = await fetch(`/income-statement?${params}`)
  } catch {
    throw new Error('Could not reach the server.')
  }

  if (!response.ok) {
    throw new Error(await errorMessage(response))
  }
  return response.json()
}

// The backend answers invalid input with 400 and {"message": "..."}.
async function errorMessage(response: Response): Promise<string> {
  try {
    const body = await response.json()
    if (typeof body.message === 'string') {
      return body.message
    }
  } catch {
    // The body was not JSON, for example when the backend is not running.
  }
  return `The server returned an error (${response.status}). Is the backend running?`
}
