package org.example.eurekaback.Entity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum GamificationBadge {
    FIRST_PARTICIPATION("first_participation", "First Steps", "Complete your first campaign", 1, "fas fa-baby"),
    REGULAR_PARTICIPANT("regular_participant", "Regular Participant", "Complete 5 campaigns", 5, "fas fa-user-check"),
    DEDICATED_TESTER("dedicated_tester", "Dedicated Tester", "Complete 10 campaigns", 10, "fas fa-star"),
    EXPERT_REVIEWER("expert_reviewer", "Expert Reviewer", "Complete 15 campaigns", 15, "fas fa-crown"),
    CAMPAIGN_MASTER("campaign_master", "Campaign Master", "Complete 25 campaigns", 25, "fas fa-trophy"),
    LEGEND("legend", "Legend", "Complete 50 campaigns", 50, "fas fa-dragon");

    private final String id;
    private final String name;
    private final String description;
    private final int requiredParticipations;
    private final String iconClass;

    GamificationBadge(String id, String name, String description, int requiredParticipations, String iconClass) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requiredParticipations = requiredParticipations;
        this.iconClass = iconClass;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getRequiredParticipations() {
        return requiredParticipations;
    }

    public String getIconClass() {
        return iconClass;
    }

    public static List<GamificationBadge> getUnlockedBadges(int participationCount) {
        return Arrays.stream(values())
            .filter(badge -> participationCount >= badge.getRequiredParticipations())
            .collect(Collectors.toList());
    }

    public static List<GamificationBadge> getNewlyUnlockedBadges(int oldParticipationCount, int newParticipationCount) {
        List<GamificationBadge> oldBadges = getUnlockedBadges(oldParticipationCount);
        List<GamificationBadge> newBadges = getUnlockedBadges(newParticipationCount);

        newBadges.removeAll(oldBadges);
        return newBadges;
    }
}
