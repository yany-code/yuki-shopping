// 创建订单、发起支付必须携带 Idempotency-Key；同一次"下单意图"的重试必须复用同一个 key
let current: string | null = null

export function takeIdempotencyKey(): string {
  if (!current) current = crypto.randomUUID()
  return current
}

export function clearIdempotencyKey() {
  current = null
}
