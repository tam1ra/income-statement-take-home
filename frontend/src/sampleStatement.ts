import type { IncomeStatement } from './api'

// SAMPLE DATA: made-up numbers for laying out the report page.
// Deleted when the page is connected to the backend.
export function sampleStatement(start: string, end: string): IncomeStatement {
  return {
    start,
    end,
    revenue: {
      lines: [
        { accountNumber: '4000', accountName: 'Product Revenue', amount: '12500.00' },
        { accountNumber: '4100', accountName: 'Subscription Revenue', amount: '1000.00' },
        { accountNumber: '4900', accountName: 'Sales Returns & Discounts', amount: '-250.50' },
      ],
      total: '13249.50',
    },
    costOfGoodsSold: {
      lines: [{ accountNumber: '5000', accountName: 'Cost of Goods Sold', amount: '5000.25' }],
      total: '5000.25',
    },
    grossProfit: '8249.25',
    operatingExpenses: {
      lines: [
        { accountNumber: '6000', accountName: 'Salaries', amount: '18500.00' },
        { accountNumber: '6100', accountName: 'Rent', amount: '3000.00' },
      ],
      total: '21500.00',
    },
    operatingIncome: '-13250.75',
    otherIncome: {
      lines: [],
      total: '0.00',
    },
    netIncome: '-13250.75',
  }
}
