export interface ReputationHistoryQuerySend {
  targetId: number  // 商铺ID 或 商品ID
  type: 0 | 1      // 0=商铺, 1=商品
}
