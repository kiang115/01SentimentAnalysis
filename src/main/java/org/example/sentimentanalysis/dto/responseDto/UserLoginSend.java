package org.example.sentimentanalysis.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginSend {
    private Long userId;
    private String userName;
    private String userType;
    private Long merchantId;
    private String token;
    private String avatarUrl;
}