package org.example.sentimentanalysis.service.impl;

import org.example.sentimentanalysis.model.Comments;
import org.example.sentimentanalysis.mapper.CommentsMapper;
import org.example.sentimentanalysis.service.CommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商家情感分析-评论主表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-02-11
 */
@Service
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, Comments> implements CommentsService {

}
