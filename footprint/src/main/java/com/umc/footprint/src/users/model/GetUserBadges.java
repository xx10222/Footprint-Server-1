package com.umc.footprint.src.users.model;

import com.umc.footprint.src.badge.model.BadgeInfo;
import com.umc.footprint.src.badge.model.BadgeOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@Builder
@RequiredArgsConstructor
public class GetUserBadges {
    private final BadgeInfo repBadgeInfo;
    private final List<BadgeOrder> badgeList;
}
