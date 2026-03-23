package org.example.sentimentanalysis.service.impl;

import org.example.sentimentanalysis.model.Tag;
import org.example.sentimentanalysis.mapper.TagMapper;
import org.example.sentimentanalysis.service.TagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 全局标签表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-03-23
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

}
