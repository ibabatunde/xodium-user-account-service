package com.xodium.useraccountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStatisticsDTO {
    private long totalUsers;
    private long activeUsers;
    private long inActiveUsers;
    private long totalAccounts;
    private long averageAccountPerUser;
    private long customerCount;
    private long adminsCount;
}
