<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { modelApi } from '@/api/model-api'
import type { CommentsQuerySend } from '@/Dto/SendDto/CommentsQuerySend'
import type { CommentAddSend } from '@/Dto/SendDto/CommentAddSend'
import type { CommentInfo } from '@/Dto/ReceiveDto/CommentDataListRec'
import type { PageInfo } from '@/Dto/ReceiveDto/PageInfo'

const props = defineProps<{
  productId: number
  merchantId: number
  domainId: number
}>()

const commentContent = ref('')
const listLoading = ref(false)
const submitLoading = ref(false)
const pageInfo = ref<PageInfo<CommentInfo> | null>(null)

const queryParams = reactive<CommentsQuerySend>({
  productId: props.productId,
  pageNum: 1,
  pageSize: 8,
})

function formatPercent(value: number) {
  return `${(value * 100).toFixed(1)}%`
}

async function fetchComments() {
  listLoading.value = true
  try {
    const res = await modelApi.listCommentDataList(queryParams)
    pageInfo.value = res.data.pageInfo
  } finally {
    listLoading.value = false
  }
}

function handlePageChange(page: number) {
  queryParams.pageNum = page
  fetchComments()
}

async function submitComment() {
  const content = commentContent.value.trim()
  if (!content) {
    ElMessage.warning('请输入评论内容')
    return
  }

  const payload: CommentAddSend = {
    customerId: null,
    content,
    domainId: props.domainId,
    productId: props.productId,
    merchantId: props.merchantId,
  }

  submitLoading.value = true
  try {
    await modelApi.addCommmentData(payload)
    commentContent.value = ''
    queryParams.pageNum = 1
    await fetchComments()
  } finally {
    submitLoading.value = false
  }
}

watch(
  () => props.productId,
  async (productId) => {
    queryParams.productId = productId
    queryParams.pageNum = 1
    await fetchComments()
  },
)

onMounted(() => {
  fetchComments()
})
</script>

<template>
  <section class="comments-section">
    <div class="comments-header">
      <h2 class="comments-title">评论区</h2>
    </div>

    <section class="comment-send-card">
      <el-input
        v-model="commentContent"
        type="textarea"
        :rows="4"
        resize="none"
        placeholder="请输入评论内容"
      />
      <div class="send-action-row">
        <el-button type="primary" :loading="submitLoading" @click="submitComment">发送评论</el-button>
      </div>
    </section>

    <section class="comment-list-card" v-loading="listLoading">
      <el-empty v-if="!listLoading && (!pageInfo || pageInfo.list.length === 0)" description="暂无评论" />

      <div v-else class="comment-list">
        <article v-for="item in pageInfo?.list" :key="item.commentId" class="comment-item">
          <div class="comment-main">
            <div class="comment-meta-row">
              <span class="comment-user">{{ item.userName }}</span>
              <span class="comment-time">{{ item.publishTime }}</span>
            </div>
            <p class="comment-content">{{ item.content }}</p>
          </div>

          <div class="comment-tags">
            <el-tag type="info" effect="plain">状态：{{ item.statusName }}</el-tag>
            <el-tag v-if="item.finalSentimentName != null" type="warning" effect="plain">情感：{{ item.finalSentimentName }}</el-tag>
            <el-tag v-if="item.confidence != null" effect="light">置信度：{{ formatPercent(item.confidence) }}</el-tag>
            <el-tag v-if="item.positiveProb != null" type="success" effect="light">正向概率：{{ formatPercent(item.positiveProb) }}</el-tag>
            <el-tag v-if="item.negativeProb != null" type="danger" effect="light">负向概率：{{ formatPercent(item.negativeProb) }}</el-tag>
          </div>
        </article>
      </div>

      <div v-if="pageInfo && pageInfo.total > 0" class="pagination-wrapper">
        <el-pagination
          :current-page="pageInfo.pageNum"
          :page-size="pageInfo.pageSize"
          :total="pageInfo.total"
          layout="total, prev, pager, next"
          background
          @current-change="handlePageChange"
        />
      </div>
    </section>
  </section>
</template>

<style scoped lang="less">
.comments-section {
  margin-top: 20px;
}

.comments-header {
  margin-bottom: 12px;
}

.comments-title {
  margin: 0;
  font-size: 22px;
  color: #1f2937;
}

.comment-send-card,
.comment-list-card {
  border-radius: 12px;
  background: #fff;
  padding: 16px;
}

.comment-send-card {
  margin-bottom: 14px;
}

.send-action-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 14px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: start;
}

.comment-main {
  min-width: 0;
}

.comment-meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-user {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.comment-time {
  font-size: 13px;
  color: #64748b;
}

.comment-content {
  margin: 0;
  color: #334155;
  line-height: 1.7;
  word-break: break-word;
  white-space: pre-wrap;
}

.comment-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  align-content: flex-start;
  gap: 8px;
  max-width: 320px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

@media (max-width: 992px) {
  .comment-item {
    grid-template-columns: 1fr;
  }

  .comment-tags {
    justify-content: flex-start;
    max-width: none;
  }
}
</style>

