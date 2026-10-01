// Formats a decimal string from the backend for display: "-1234.50" becomes "(1,234.50)".
// Works on the text only, so the amount is never converted to a floating-point number.
export function formatAmount(amount: string): string {
  const negative = amount.startsWith('-')
  const [whole, cents = '00'] = amount.replace('-', '').split('.')
  const grouped = whole.replace(/\B(?=(\d{3})+$)/g, ',')
  const text = `${grouped}.${cents}`
  return negative ? `(${text})` : text
}
