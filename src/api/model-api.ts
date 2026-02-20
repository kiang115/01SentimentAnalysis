//  模型服务接口
import {post, get} from '@/utils/request'
import type {InferPanelSend} from '@/Dto/SendDto/InferPanelSend'
import type {InferPanelRec} from '@/Dto/ReceiveDto/InferPanelRec'
import type {TrainPanelRec} from '@/Dto/ReceiveDto/TrainPanelRec'
import type {TrainPanelSend} from '@/Dto/SendDto/TrainPanelSend'

export const modelApi = {
    listInferTasks: () => {
        return get('/InferenceTasksList', {})
    },
    listInferPanel: () => {
        return get('/InferencePanel', {}) as Promise<{ data: InferPanelRec }>
    },
    checkInferData: (para: InferPanelSend) => {
        return post('/InferenceDataCheck', para)
    },
    listTrainPanel: () => {
        return get('/TrainPanel', {}) as Promise<{ data: TrainPanelRec }>
    },
    checkTrainData: (para: TrainPanelSend) => {
        return post('/TrainDataCheck', para)
    },
    listTrainTasks: () => {
        return get('/TrainTaskList', {})
    },
}