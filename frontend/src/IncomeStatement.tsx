import type { IncomeStatement as Statement, Section } from './api'
import { formatAmount } from './format'

type Props = {
  statement: Statement
}

function IncomeStatement({ statement }: Props) {
  const hasActivity =
    statement.revenue.lines.length > 0 ||
    statement.costOfGoodsSold.lines.length > 0 ||
    statement.operatingExpenses.lines.length > 0 ||
    statement.otherIncome.lines.length > 0

  return (
    <section className="statement">
      <h2>Income Statement</h2>
      <p className="period">
        For the period {statement.start} to {statement.end}
      </p>
      {!hasActivity && <p className="notice">No activity in this period.</p>}

      <table>
        <SectionRows title="Revenue" totalLabel="Total revenue" section={statement.revenue} />
        <SectionRows
          title="Cost of goods sold"
          totalLabel="Total cost of goods sold"
          section={statement.costOfGoodsSold}
        />
        <SubtotalRow label="Gross profit" amount={statement.grossProfit} />
        <SectionRows
          title="Operating expenses"
          totalLabel="Total operating expenses"
          section={statement.operatingExpenses}
        />
        <SubtotalRow label="Operating income" amount={statement.operatingIncome} />
        <SectionRows
          title="Other income"
          totalLabel="Total other income"
          section={statement.otherIncome}
        />
        <SubtotalRow label="Net income" amount={statement.netIncome} className="net-income" />
      </table>
    </section>
  )
}

type SectionRowsProps = {
  title: string
  totalLabel: string
  section: Section
}

function SectionRows({ title, totalLabel, section }: SectionRowsProps) {
  return (
    <tbody>
      <tr className="section-title">
        <th scope="rowgroup" colSpan={2}>
          {title}
        </th>
      </tr>
      {section.lines.map((line) => (
        <tr key={line.accountNumber} className="line">
          <td>
            <span className="account-number">{line.accountNumber}</span>
            {line.accountName}
          </td>
          <td className="amount">{formatAmount(line.amount)}</td>
        </tr>
      ))}
      {section.lines.length === 0 && (
        <tr className="line">
          <td className="none" colSpan={2}>
            No activity
          </td>
        </tr>
      )}
      <tr className="section-total">
        <th scope="row">{totalLabel}</th>
        <td className="amount">{formatAmount(section.total)}</td>
      </tr>
    </tbody>
  )
}

type SubtotalRowProps = {
  label: string
  amount: string
  className?: string
}

function SubtotalRow({ label, amount, className = '' }: SubtotalRowProps) {
  return (
    <tbody>
      <tr className={`subtotal ${className}`}>
        <th scope="row">{label}</th>
        <td className="amount">{formatAmount(amount)}</td>
      </tr>
    </tbody>
  )
}

export default IncomeStatement
