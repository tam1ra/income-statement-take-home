import { useState } from 'react'
import type { SubmitEvent } from 'react'
import IncomeStatement from './IncomeStatement'
import { sampleStatement } from './sampleStatement'

function App() {
  const [start, setStart] = useState('2026-01-01')
  const [end, setEnd] = useState('2026-03-31')
  const [statement, setStatement] = useState(() => sampleStatement(start, end))

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    setStatement(sampleStatement(start, end))
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
        <button type="submit">Run report</button>
      </form>

      <p className="notice">Sample data. Not connected to the backend yet.</p>

      <IncomeStatement statement={statement} />
    </main>
  )
}

export default App
