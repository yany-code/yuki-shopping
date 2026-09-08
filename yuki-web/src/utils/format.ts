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
