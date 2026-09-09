<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { payApi } from '@/api/pay'
import { showApiError } from '@/utils/feedback'
import { clearIdempotencyKey } from '@/utils/idempotency'

// 模拟支付弹窗：发起支付 → 模拟渠道回调，全流程走真实接口路径
const props = defineProps<{ orderNo: string; payAmount: string }>()
const emit = defineEmits<{ paid: []; later: [] }>()
const visible = defineModel<boolean>({ required: true })

const paying = ref(false)

async function doPay() {
  paying.value = true
  try {
    const pay = await payApi.pay(props.orderNo, 1)
    await payApi.callback({
      paymentNo: pay.paymentNo,
      amount: props.payAmount,
      status: 1,
      tradeNo: `SIM${Date.now()}`,
    })
    // 支付完成 = 这次下单意图闭环，下一次下单用新幂等键
    clearIdempotencyKey()
    ElMessage.success('支付成功')
    visible.value = false
    emit('paid')
  } catch (e) {
    showApiError(e)
  } finally {
    paying.value = false
  }
}

function later() {
  visible.value = false
  emit('later')
}
</script>

<template>
  <el-dialog v-model="visible" title="收银台" width="400px" :close-on-click-modal="false">
    <div class="pay-body">
      <p class="label">订单号</p>
      <p class="value">{{ orderNo }}</p>
      <p class="label">应付金额</p>
      <p class="amount">¥{{ payAmount }}</p>
      <p class="tip">模拟支付：点击「确认支付」即模拟渠道支付成功并回调</p>
    </div>
    <template #footer>
      <el-button :disabled="paying" @click="later">暂不支付</el-button>
      <el-button type="primary" :loading="paying" @click="doPay">确认支付 ¥{{ payAmount }}</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.pay-body {
  text-align: center;
}

.label {
  color: #909399;
  font-size: 13px;
  margin: 8px 0 2px;
}

.value {
  margin: 0;
  font-family: monospace;
}

.amount {
  font-size: 28px;
  font-weight: 700;
  color: #f56c6c;
  margin: 0 0 12px;
}

.tip {
  color: #909399;
  font-size: 12px;
}
</style>
