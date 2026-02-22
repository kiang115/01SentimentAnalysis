//  模型服务接口
import {post, get} from '@/utils/request'
import type {InferPanelSend} from '@/Dto/SendDto/InferPanelSend'
import type {InferPanelRec} from '@/Dto/ReceiveDto/InferPanelRec'
import type {TrainPanelRec} from '@/Dto/ReceiveDto/TrainPanelRec'
import type {TrainPanelSend} from '@/Dto/SendDto/TrainPanelSend'
import type {TrainTasksRec} from '@/Dto/ReceiveDto/TrainTasksRec'
import type {TrainLineChart} from '@/Dto/ReceiveDto/TrainLineChart'
import type {TasksHotChart} from '@/Dto/ReceiveDto/TasksHotChart'
import type {InferPieChart} from '@/Dto/ReceiveDto/InferPieChart'
// 定义标准返回类型
// todo 把vue接收到的非标准的api返回值统一处理为ApiResponse<T>类型 特别是train.vue中的ListTrainTasksRes类型需要被替代
export interface ApiResponse<T = any> {
    code: number;
    message: string;
    data: T;
}

export const modelApi = {
    listInferTasks: () => {
        return get('/InferenceTasksList', {})
    },
    listInferPanel: () => {
        return get('/InferencePanel', {}) as Promise<ApiResponse<InferPanelRec>>
        //     todo 要修改这里面的的类型，不要根据message判断是否全部已经完成
    },
    checkInferData: (para: InferPanelSend) => {
        return post('/InferenceDataCheck', para)
    },
    // 训练任务
    listTrainTasks: () => {
        return get('/TrainTaskList', {}) as Promise<ApiResponse<TrainTasksRec>>
    },
    listTrainPanel: () => {
        return get('/TrainPanel', {}) as Promise<ApiResponse<TrainPanelRec>>
    },
    checkTrainData: (para: TrainPanelSend) => {
        return post('/TrainDataCheck', para)
    },
    // 训练折线图
    listTrainLineChart:()=>{
        return get('/TrainLineChart',{}) as Promise<ApiResponse<TrainLineChart>>
    },
//     任务热力图
    listTasksHotChart:()=>{
        return get('/TasksHotChart',{}) as Promise<ApiResponse<TasksHotChart>>
    },
//     推理饼图
    listInferPieChart:()=>{
        return get('/InferPieChart',{}) as Promise<ApiResponse<InferPieChart>>
    },
}