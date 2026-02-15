//  模型服务接口
import { post, get } from '@/utils/request'
import type { CheckInferDataPara } from '@/requestDto/check-infer-data'

export const modelApi = {
  listInferTasks: () => {
    return get('/InferenceTasksList', {})
  },
  listInferPanel: () => {
    return get('/InferencePanel', {})
  },
  checkInferData: (para: CheckInferDataPara) => {
    return post('/InferenceDataCheck', para)
  },
}