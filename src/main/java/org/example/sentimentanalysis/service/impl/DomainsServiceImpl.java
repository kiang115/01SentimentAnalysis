package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.sentimentanalysis.assembler.InferencePanelAssembler;
import org.example.sentimentanalysis.dto.responseDto.InferencePanelDto;
import org.example.sentimentanalysis.enums.CommentStatusEnum;
import org.example.sentimentanalysis.model.Comments;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.mapper.DomainsMapper;
import org.example.sentimentanalysis.model.Models;
import org.example.sentimentanalysis.service.CommentsService;
import org.example.sentimentanalysis.service.DomainsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.sentimentanalysis.service.ModelsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 领域信息表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-02-11
 */
@Service
public class DomainsServiceImpl extends ServiceImpl<DomainsMapper, Domains> implements DomainsService {
    @Autowired
    private ModelsService modelsService;
    @Autowired
    private CommentsService commentsService;
    @Autowired
    private InferencePanelAssembler inferencePanelAssembler;

    @Override
    public InferencePanelDto ListInferencePanelDto() {
//        列举领域map
        List<Domains> domains = list();
//        列举所有modelmap
        List<Models> models = modelsService.list();
//     计算comment表中未推理的评论数
        QueryWrapper<Comments> wrapper = new QueryWrapper<>();

        wrapper.select("domain_id", "count(*) as count_num") // 指定查询列：ID 和 数量
                .lambda() // 切换为 Lambda 模式，下面可以使用方法引用，防手误
                .eq(Comments::getStatus, CommentStatusEnum.PENDING.getCode()) // status = 0
                .groupBy(Comments::getDomainId); // GROUP BY domain_id
        // 2. 执行查询，返回 List<Map<String, Object>>
        // listMaps 是 MP 提供的专门用于返回 Map 的方法
        List<Map<String, Object>> resultList = commentsService.listMaps(wrapper);
        //每一个map是一行数据 的所有字段名->字段值映射
        // 3. 转换结果为 Map<Long, Long>
        Map<Long, Long> resultMap = resultList.stream()
                .collect(Collectors.toMap(
                        // Key: domain_id
                        map -> ((Number) map.get("domain_id")).longValue(),
                        // Value: count_num
                        map -> ((Number) map.get("count_num")).longValue()
                ));

        return inferencePanelAssembler.toDto(domains, resultMap, models);

    }
}
