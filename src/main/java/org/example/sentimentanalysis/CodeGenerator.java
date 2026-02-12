package org.example.sentimentanalysis;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;
//注意FileOverride会覆盖自定义的servicde函数，需要注释

public class CodeGenerator {
    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://127.0.0.1:3306/sentiment_analysis?characterEncoding=UTF-8&&serverTimezone=GMT", "root", "121144")
                .globalConfig(builder -> {
                    builder.outputDir((System.getProperty("user.dir") + "/src/main/java"))
                            .disableOpenDir()
                            .author("kiang");
                })
                .packageConfig(builder -> {
                    builder.parent("org.example.sentimentanalysis")
                            .entity("model")
                            .mapper("mapper")
                            .service("service")
                            .serviceImpl("service.impl")
                            .pathInfo(Collections.singletonMap(OutputFile.xml,
                                    System.getProperty("user.dir") + "/src/main/resources/mapper"));
                })
                .strategyConfig(builder -> {
                    builder.enableSkipView()
                            // 生成所有表（不指定表名）
                            .entityBuilder()
                            .enableFileOverride() // 覆盖已生成文件
                            .enableLombok()
                            .enableTableFieldAnnotation()
                            .enableChainModel()
                            .naming(NamingStrategy.underline_to_camel)
                            .columnNaming(NamingStrategy.underline_to_camel) // 下划线转驼峰
                            .formatFileName("%s")

                            .mapperBuilder()
                            .enableFileOverride()// 覆盖已生成文件
                            .formatMapperFileName("%sMapper")
                            .formatXmlFileName("%sMapper")

                            .serviceBuilder()
//                            .enableFileOverride() // 覆盖已生成文件
                            .formatServiceFileName("%sService")
                            .formatServiceImplFileName("%sServiceImpl")

                            .controllerBuilder()
                            .disable();
                })
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
