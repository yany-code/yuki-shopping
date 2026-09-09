/** 价格区间展示：min=max 时只显示一个价 */
export function priceRangeText(min: string, max: string): string {
  return min === max ? `¥${min}` : `¥${min} ~ ¥${max}`
}

/** SKU 规格 JSON {"颜色":"黑","存储":"256G"} → "颜色:黑 / 存储:256G" */
export function specsText(specsJson: string): string {
  try {
    return Object.entries(JSON.parse(specsJson) as Record<string, string>)
      .map(([k, v]) => `${k}:${v}`)
      .join(' / ')
  } catch {
    return specsJson
  }
}

// 订单状态：10待支付 20待发货 30已发货 40已完成 50已取消 60已退款
const ORDER_STATUS_META: Record<number, { text: string; tag: 'warning' | 'primary' | 'success' | 'info' | 'danger' }> = {
  10: { text: '待支付', tag: 'warning' },
  20: { text: '待发货', tag: 'primary' },
  30: { text: '已发货', tag: 'success' },
  40: { text: '已完成', tag: 'info' },
  50: { text: '已取消', tag: 'info' },
  60: { text: '已退款', tag: 'danger' },
}

export function orderStatusText(status?: number): string {
  return (status !== undefined && ORDER_STATUS_META[status]?.text) || '未知状态'
}

export function orderStatusTag(status?: number): 'warning' | 'primary' | 'success' | 'info' | 'danger' {
  return (status !== undefined && ORDER_STATUS_META[status]?.tag) || 'info'
}
