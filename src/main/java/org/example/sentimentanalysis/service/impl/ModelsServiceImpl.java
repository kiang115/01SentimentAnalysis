package org.example.sentimentanalysis.service.impl;

import org.example.sentimentanalysis.model.Models;
import org.example.sentimentanalysis.mapper.ModelsMapper;
import org.example.sentimentanalysis.service.ModelsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 模型表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-02-11
 */
@Service
public class ModelsServiceImpl extends ServiceImpl<ModelsMapper, Models> implements ModelsService {

}
