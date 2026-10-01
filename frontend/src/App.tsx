import { useEffect, useState } from 'react'
import type { SubmitEvent } from 'react'
import { fetchIncomeStatement } from './api'
import type { IncomeStatement as Statement } from './api'
import IncomeStatement from './IncomeStatement'

const DEFAULT_START = '2026-01-01'
const DEFAULT_END = '2026-03-31'

function App() {
  const [start, setStart] = useState(DEFAULT_START)
  const [end, setEnd] = useState(DEFAULT_END)
  const [statement, setStatement] = useState<Statement | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  function showStatement(statement: Statement) {
    setStatement(statement)
    setError(null)
    setLoading(false)
  }

  function showError(e: unknown) {
    setStatement(null)
    setError(e instanceof Error ? e.message : 'Something went wrong.')
    setLoading(false)
  }

  // Show the default period when the page opens. loading starts as true.
  useEffect(() => {
    fetchIncomeStatement(DEFAULT_START, DEFAULT_END).then(showStatement, showError)
  }, [])

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    setLoading(true)
    setError(null)
    fetchIncomeStatement(start, end).then(showStatement, showError)
  }

  return (
    <main>
      <h1>Northwind Coffee Roasters</h1>

      <form className="filter" onSubmit={handleSubmit}>
        <label>
          Start date
          <input type="date" value={start} onChange={(e) => setStart(e.target.value)} required />
        </label>
        <label>
          End date
          <input type="date" value={end} onChange={(e) => setEnd(e.target.value)} required />
        </label>
        <button type="submit" disabled={loading}>
          Run report
        </button>
      </form>

      {loading && <p className="notice">Loading…</p>}
      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}
      {!loading && statement && <IncomeStatement statement={statement} />}
    </main>
  )
}

export default App
