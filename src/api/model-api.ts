//  模型服务接口
import { post, get } from "@/utils/request";
import type { InferPanelSend } from "@/Dto/SendDto/InferPanelSend";
import type { InferPanelRec } from "@/Dto/ReceiveDto/InferPanelRec";
import type { TrainPanelRec } from "@/Dto/ReceiveDto/TrainPanelRec";
import type { TrainPanelSend } from "@/Dto/SendDto/TrainPanelSend";
import type { TrainTasksRec } from "@/Dto/ReceiveDto/TrainTasksRec";
import type { TrainLineChart } from "@/Dto/ReceiveDto/TrainLineChart";
import type { TasksHotChart } from "@/Dto/ReceiveDto/TasksHotChart";
import type { InferPieChart } from "@/Dto/ReceiveDto/InferPieChart";
import type { TrainDataQuerySend } from "@/Dto/SendDto/TrainDataQuerySend";
import type { TrainDataListRec } from "../Dto/ReceiveDto/TrainDataListRec";
import type { TrainDataAddSend } from "../Dto/SendDto/TrainDataAddSend";
import type { TrainParaListRec } from "../Dto/ReceiveDto/TrainParaListRec";
import type { TrainParaQuerySend } from "@/Dto/SendDto/TrainParaQuerySend";
import type { AddTrainParaSend } from "@/Dto/SendDto/AddTrainParaSend";
import type { ModelsQuerySend } from "@/Dto/SendDto/ModelsQuerySend";
import type { ModelsListRec } from "@/Dto/ReceiveDto/ModelsListRec";
import type { ModelFilesSend } from "@/Dto/SendDto/ModelFilesSend";
import Constants from "@/utils/constants";
import type { ModelLineChart } from "@/Dto/ReceiveDto/ModelLineChart";
import type { ModelPieChart } from "@/Dto/ReceiveDto/ModelPieChart";
import type { ModelTreeChartRec } from "@/Dto/ReceiveDto/ModelTreeChartRec";
import type { InferTaskRec } from "@/Dto/ReceiveDto/InferTaskRec";
// 定义标准返回类型
// todo 把vue接收到的非标准的api返回值统一处理为ApiResponse<T>类型 特别是train.vue中的ListTrainTasksRes类型需要被替代
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

export const modelApi = {
  listInferTasks: () => {
    return get("/InferenceTasksList", {}) as Promise<ApiResponse<InferTaskRec>>;
  },
  listInferPanel: () => {
    return get("/InferencePanel", {}) as Promise<ApiResponse<InferPanelRec>>;
    //     todo 要修改这里面的的类型，不要根据message判断是否全部已经完成
  },
  checkInferData: (para: InferPanelSend) => {
    return post("/InferenceDataCheck", para);
  },
  // 训练任务
  listTrainTasks: () => {
    return get("/TrainTaskList", {}) as Promise<ApiResponse<TrainTasksRec>>;
  },
  listTrainPanel: () => {
    return get("/TrainPanel", {}) as Promise<ApiResponse<TrainPanelRec>>;
  },
  checkTrainData: (para: TrainPanelSend) => {
    return post("/TrainDataCheck", para);
  },
  // 训练折线图
  listTrainLineChart: () => {
    return get("/TrainLineChart", {}) as Promise<ApiResponse<TrainLineChart>>;
  },
  //     任务热力图
  listTasksHotChart: () => {
    return get("/TasksHotChart", {}) as Promise<ApiResponse<TasksHotChart>>;
  },
  //     推理饼图
  listInferPieChart: () => {
    return get("/InferPieChart", {}) as Promise<ApiResponse<InferPieChart>>;
  },
  // 训练数据查询
  listTrainData: (para: TrainDataQuerySend) => {
    return post("/TrainDataQuery", para) as Promise<
      ApiResponse<TrainDataListRec>
    >;
  },
  //     增加单条训练数据
  addTrainData: (para: TrainDataAddSend) => {
    return post("/TrainDataAdd", para) as Promise<ApiResponse<void>>;
  },
  // 上传训练数据csv文件
  uploadTrainData: (formData: FormData) => {
    return post("/uploadTrainDataCsv", formData) as Promise<ApiResponse<void>>;
  },
  // 批量删除训练数据
  deleteTrainData: (para: number[]) => {
    return post("/deleteTrainData", para) as Promise<ApiResponse<void>>;
  },
  //     训练参数列表查询
  listTrainParam: (para: TrainParaQuerySend) => {
    return post("/listTrainPara", para) as Promise<
      ApiResponse<TrainParaListRec>
    >;
  },
  //     删除训练参数
  deleteTrainParam: (para: number[]) => {
    return post("/deleteTrainPara", para) as Promise<ApiResponse<void>>;
  },
  //     增加训练参数
  addTrainParam: (para: AddTrainParaSend) => {
    return post("/addTrainPara", para) as Promise<ApiResponse<void>>;
  },
  //  模型列表查询
  listModels: (para: ModelsQuerySend) => {
    return post("/listModels", para) as Promise<ApiResponse<ModelsListRec>>;
  },
  //  模型删除
  deleteModels: (para: number[]) => {
    return post("/deleteModels", para) as Promise<ApiResponse<void>>;
  },

  // 下载模型文件 只需要执行这个api即可，无需其他后续对返回内容的解析。
  downLoadModel: (modelId: number) => {
    window.location.href = Constants.BASE_URL + '/download-model/' + modelId;
  },

  // 上传模型文件
  uploadModel: (formData: FormData) => {
    return post('/upload-model', formData) as Promise<ApiResponse<void>>;
  },
  // 得到模型准确率折线图
  listModelLineChart: () => {
    return get("/ModelLineChart", {}) as Promise<ApiResponse<ModelLineChart>>;
  },
  // 整体 评论状态饼图+真实准确率
  listModelPieChart: () => {
    return get("/ModelPieChart", {}) as Promise<ApiResponse<ModelPieChart>>;
  },
  // 模型树状图（按领域）
  listModelTreeChart: () => {
    return get("/ModelTreeChart", {}) as Promise<ApiResponse<ModelTreeChartRec>>;
  },
};
