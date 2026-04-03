<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'
import {modelApi} from '@/api/model-api'
import { userStore } from '@/stores/user'
import type {CommentsQuerySend} from '@/Dto/SendDto/CommentsQuerySend'
import type {CommentAddSend} from '@/Dto/SendDto/CommentAddSend'
import type {CommentInfo} from '@/Dto/ReceiveDto/CommentDataListRec'
import type {PageInfo} from '@/Dto/ReceiveDto/PageInfo'

const props = defineProps<{
  productId: number
  merchantId: number
  domainId: number
  canSendComment: boolean
  canViewInferenceTags: boolean
}>()

const store = userStore()
const commentContent = ref('')
const listLoading = ref(false)
const submitLoading = ref(false)
const reviewLoadingCommentId = ref<number | null>(null)
const pageInfo = ref<PageInfo<CommentInfo> | null>(null)

const queryParams = reactive<CommentsQuerySend>({
  productId: props.productId,
  pageNum: 1,
  pageSize: 8,
})

const userInfo = computed(() => store.userInfo)
const isAdminUser = computed(() => userInfo.value.userType === 'admin')
const isMerchantOwner = computed(() => (
  userInfo.value.userType === 'merchant' && userInfo.value.merchantId === props.merchantId
))

function formatPercent(value: number) {
  return `${(value * 100).toFixed(1)}%`
}

function canReviewComment(item: CommentInfo) {
  if (item.statusId !== 2) {
    return false
  }

  return isAdminUser.value || isMerchantOwner.value
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
  if (!props.canSendComment) {
    return
  }
  const content = commentContent.value.trim()
  if (!content) {
    ElMessage.warning('请输入评论内容')
    return
  }

  const payload: CommentAddSend = {
    content,
    domainId: props.domainId,
    productId: props.productId,
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

async function handleReviewComment(commentId: number) {
  if (reviewLoadingCommentId.value !== null) {
    return
  }

  reviewLoadingCommentId.value = commentId
  try {
    await modelApi.reviewComment(commentId)
    ElMessage.success('申请复核成功')
    await fetchComments()
  } finally {
    reviewLoadingCommentId.value = null
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

    <section v-if="props.canSendComment" class="comment-send-card">
      <div class="send-input-row">
        <el-input
            v-model="commentContent"
            type="textarea"
            :rows="2"
            resize="none"
            placeholder="请输入评论内容"
        />
        <el-button
          type="primary"
          :loading="submitLoading"
          :icon="Promotion"
          class="send-btn"
          @click="submitComment"
        />
      </div>
    </section>

    <section class="comment-list-card" v-loading="listLoading">
      <el-empty v-if="!listLoading && (!pageInfo || pageInfo.list.length === 0)" description="暂无评论"/>

      <div v-else class="comment-list">
        <article v-for="item in pageInfo?.list" :key="item.commentId" class="comment-item">
          <div class="comment-main">
            <div class="comment-meta-row">
              <el-avatar :size="40" :src="item.userAvatarUrl" class="comment-avatar">
                {{ item.userName?.slice(0, 1) }}
              </el-avatar>
              <div class="comment-meta-text">
                <span class="comment-user">{{ item.userName }}</span>
                <span class="comment-time">{{ item.publishTime }}</span>
              </div>
            </div>
            <p class="comment-content">{{ item.content }}</p>
          </div>

          <div v-if="props.canViewInferenceTags" class="comment-tags">
            <div class="comment-tag-row">
              <el-tag type="info" effect="plain">状态：{{ item.statusName }}</el-tag>
              <el-tag :type="item.isInspected ? '' : 'warning'" effect="plain">
                标签分析：{{ item.isInspected ? '已分析' : '待分析' }}
              </el-tag>
              <el-tag v-if="item.finalSentimentName != null" type="warning" effect="plain">情感：{{
                  item.finalSentimentName
                }}
              </el-tag>
            </div>
            <div
              v-if="item.confidence != null || item.positiveProb != null || item.negativeProb != null"
              class="comment-tag-row comment-prob-row"
            >
              <el-tag v-if="item.confidence != null" effect="light">置信度：{{ formatPercent(item.confidence) }}</el-tag>
              <el-tag v-if="item.positiveProb != null" type="success" effect="light">
                正向概率：{{ formatPercent(item.positiveProb) }}
              </el-tag>
              <el-tag v-if="item.negativeProb != null" type="danger" effect="light">
                负向概率：{{ formatPercent(item.negativeProb) }}
              </el-tag>
            </div>
            <div v-if="canReviewComment(item)" class="comment-review-row">
              <el-button
                type="primary"
                plain
                size="small"
                :loading="reviewLoadingCommentId === item.commentId"
                @click="handleReviewComment(item.commentId)"
              >
                申请复核
              </el-button>
            </div>
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

.send-input-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;

  .el-textarea {
    flex: 1;
  }
}

.send-btn {
  flex-shrink: 0;
  height: 56px;
  width: 44px;
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

.comment-avatar {
  flex-shrink: 0;
}

.comment-meta-text {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
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
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  max-width: 360px;
}

.comment-tag-row {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
  width: 100%;
}

.comment-prob-row {
  flex-wrap: nowrap;
}

.comment-review-row {
  width: 100%;
  display: flex;
  justify-content: flex-end;
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
    align-items: flex-start;
    max-width: none;
  }

  .comment-tag-row {
    justify-content: flex-start;
  }

  .comment-review-row {
    justify-content: flex-start;
  }

  .comment-prob-row {
    flex-wrap: wrap;
  }
}
</style>
