export interface ModelFilesSend {
  domainId: null; // Long 类型，模型领域
  description: ""; // 模型描述
  files: []; // 存储选中的 File 对象数组
}

// 前端要求，需要一个文件上传框+模型描述框+一个模型领域下拉选择框
// demo代码见下
// <template>
//   <div>
//     <input type="number" v-model="uploadForm.domainId" placeholder="请输入 Domain ID" />
//     <textarea v-model="uploadForm.description" placeholder="请输入描述"></textarea>
    
//     <!-- 允许选择多个文件 -->
//     <input type="file" multiple @change="handleFileChange" />
    
//     <button @click="submitUpload">上传模型</button>
//   </div>
// </template>

// mport axios from 'axios';

// const handleFileChange = (e) => {
//   const selectedFiles = Array.from(e.target.files);
//   // 你可以在这里做初步校验
//   ModelFilesSend.files = selectedFiles;
// };

// const submitUpload = async () => {
//   // 1. 基础校验
//   if (ModelFilesSend.files.length !== 2) {
//     alert("必须且只能选择2个文件！");
//     return;
//   }

//   // 2. 构造 FormData (这是关键！)
//   const formData = new FormData();
  
//   // 对应后端 ModelFileRec 里的字段名
//   formData.append('domainId', ModelFilesSend.domainId);
//   formData.append('description', ModelFilesSend.description);
  
//   // 将文件数组依次添加进去
//   // 注意：后端是 MultipartFile[] files，所以 key 必须都叫 'files'
//   ModelFilesSend.files.forEach(file => {
//     formData.append('files', file); 
//   });

//   try {
//     const res = await axios.post('/api/upload-model', formData, {
//       headers: {
//         // 必须指定为 multipart/form-data
//         'Content-Type': 'multipart/form-data'
//       }
//     });
//     console.log('上传成功', res.data);
//   } catch (error) {
//     console.error('上传失败', error.response?.data?.message || error.message);
//   }
// };
 
