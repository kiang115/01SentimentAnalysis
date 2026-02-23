package org.example.sentimentanalysis.dto.responseDto;

import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.sentimentanalysis.model.TrainData;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainDataListSend {
    private PageInfo<TrainDataInfo> pageInfo;//用分页器得到的数据
    private List<domainInfo> domains;//从domains数据库中得到的信息

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class domainInfo {
        private Long domainId;
        private String domainName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainDataInfo{
        private Long id;
        private String content;
        private Long domainId;
        private String domainName;//相比于TrainData唯一多的属性
        private Integer label;
        private String source;
        private Integer trainCount;
        private LocalDateTime createdAt;
    }
}
