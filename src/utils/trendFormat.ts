export interface TrendDisplay {
  arrow: '↑' | '↓'
  formatted: string
  isUp: boolean
}

/** 综合得分差值，保留1位小数 */
export function ratingTrend(diff: number): TrendDisplay {
  const isUp = diff >= 0
  return { arrow: isUp ? '↑' : '↓', formatted: Math.abs(diff).toFixed(1), isUp }
}

/** 好评率差值，转为百分比保留2位小数（diff 原始为 0.xxxx 小数） */
export function positiveRateTrend(diff: number): TrendDisplay {
  const isUp = diff >= 0
  return { arrow: isUp ? '↑' : '↓', formatted: Math.abs(diff * 100).toFixed(2) + '%', isUp }
}

/** 评论数差值，整数本地化格式 */
export function commentCountTrend(diff: number): TrendDisplay {
  const isUp = diff >= 0
  return { arrow: isUp ? '↑' : '↓', formatted: Math.abs(diff).toLocaleString(), isUp }
}
