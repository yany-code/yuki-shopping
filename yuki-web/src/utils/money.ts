// 后端金额是字符串 "99.90"，前端计算一律转"分"（整数），展示再格式化回来，严禁 Number 相加
export function toCents(amount: string): number {
  const negative = amount.startsWith('-')
  const [yuan, fen = ''] = (negative ? amount.slice(1) : amount).split('.')
  const cents = Number(yuan) * 100 + Number(fen.padEnd(2, '0').slice(0, 2))
  return negative ? -cents : cents
}

export function formatCents(cents: number): string {
  const sign = cents < 0 ? '-' : ''
  const abs = Math.abs(cents)
  return `${sign}${Math.floor(abs / 100)}.${String(abs % 100).padStart(2, '0')}`
}

// 购物车合计：list.reduce((sum, i) => sum + toCents(i.price) * i.quantity, 0) → formatCents()
