package org.example.sentimentanalysis.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductTagAnalyzeSend {
    private Long productId;
    private Integer processedCommentCount;
    private Integer touchedTagCount;
    private Integer newTagCount;
}
