package com.talentpulse.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnreadCountResponse {

    private long unreadCount;
}
