package org.example.sentimentanalysis.service.impl;

import org.example.sentimentanalysis.model.Products;
import org.example.sentimentanalysis.mapper.ProductsMapper;
import org.example.sentimentanalysis.service.ProductsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品信息表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-03-17
 */
@Service
public class ProductsServiceImpl extends ServiceImpl<ProductsMapper, Products> implements ProductsService {

}
