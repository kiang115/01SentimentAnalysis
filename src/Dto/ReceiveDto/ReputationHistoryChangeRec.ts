export interface ReputationHistoryChangeRec {
  ratingDiff: number        // 综合得分差值
  positiveRateDiff: number  // 好评率差值（原始小数，如 0.0123）
  commentCountDiff: number  // 评论数差值
  rankingDiff: number       // 排名差值
}
