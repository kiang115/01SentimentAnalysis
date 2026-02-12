package org.example.sentimentanalysis.assembler;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.example.sentimentanalysis.dto.requestDto.InferenceParaDto;
import org.example.sentimentanalysis.dto.responseDto.InferenceDataDto;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.Comments;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InferenceDataAssembler {

    public InferenceDataDto toDto(List<InferenceParaDto.InferenceDomainPara> inferenceDomainParaList,
                                 Map<Long, List<Comments>> domainsToCommentsList) {
        // 1. 基础校验
        if (CollectionUtils.isEmpty(inferenceDomainParaList)) {
            throw new CustomBusinessException("推理参数为空");
        }
        if (CollectionUtils.isEmpty(domainsToCommentsList)) {
            throw new CustomBusinessException("待推理评论列表为空");
        }

        List<InferenceDataDto.InferenceDomainData> domainDataList = inferenceDomainParaList.stream()
                .map(p -> assembleDomainData(p, domainsToCommentsList))
                .toList();

        return InferenceDataDto.builder()
                .inferenceDomainDataList(domainDataList)
                .build();
    }

    private InferenceDataDto.InferenceDomainData assembleDomainData(
            InferenceParaDto.InferenceDomainPara para,
            Map<Long, List<Comments>> domainsToCommentsList) {

        Long domainId = para.getDomainId();
        // 确保推理数量不为 null
        long requiredNums = Math.toIntExact(para.getInferenceReviewNums() == null ? 0 : para.getInferenceReviewNums());

        List<Comments> commentsList = domainsToCommentsList.getOrDefault(domainId, Collections.emptyList());

        // 2. 业务逻辑校验
        if (commentsList.size() < requiredNums) {
            throw new CustomBusinessException(String.format(
                "领域 [%s](ID:%d) 未处理数据数 %d 少于选定评论数 %d,请刷新后再试",
                para.getDomainName(), domainId, commentsList.size(), requiredNums));
        }

        // 3. 转换评论列表
        List<InferenceDataDto.InferenceDomainComment> commentDtos = commentsList.stream()
                .limit(requiredNums)
                .map(this::toCommentDto)
                .toList();

        // 4. 构建领域数据对象
        return InferenceDataDto.InferenceDomainData.builder()
                .domainId(domainId)
                .domainName(para.getDomainName())
                .modelId(para.getModelId())
                .modelVersion(para.getModelVersion())
                .inferenceCommentNums(requiredNums)
                .inferenceDomainCommentList(commentDtos)
                .build();
    }

    private InferenceDataDto.InferenceDomainComment toCommentDto(Comments c) {
        return InferenceDataDto.InferenceDomainComment.builder()
                .commentId(c.getCommentId())
                .content(c.getContent())
                .build();
    }
}
