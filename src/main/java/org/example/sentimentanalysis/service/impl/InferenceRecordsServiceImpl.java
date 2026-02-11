package org.example.sentimentanalysis.service.impl;

import org.example.sentimentanalysis.model.InferenceRecords;
import org.example.sentimentanalysis.mapper.InferenceRecordsMapper;
import org.example.sentimentanalysis.service.InferenceRecordsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 推理记录表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-02-11
 */
@Service
public class InferenceRecordsServiceImpl extends ServiceImpl<InferenceRecordsMapper, InferenceRecords> implements InferenceRecordsService {

}
