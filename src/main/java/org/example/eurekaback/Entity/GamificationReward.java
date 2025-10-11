package org.example.eurekaback.Entity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum GamificationReward {
    WELCOME_BONUS("welcome_bonus", "Welcome Bonus", "5€ credit for first participation", 1, 5.0, "EUR"),
    LOYALTY_REWARD("loyalty_reward", "Loyalty Reward", "10€ credit for being regular", 5, 10.0, "EUR"),
    DEDICATION_PRIZE("dedication_prize", "Dedication Prize", "20€ credit for dedication", 10, 20.0, "EUR"),
    EXPERT_BONUS("expert_bonus", "Expert Bonus", "30€ credit for expertise", 15, 30.0, "EUR"),
    MASTER_REWARD("master_reward", "Master Reward", "50€ credit for mastery", 25, 50.0, "EUR"),
    LEGENDARY_PRIZE("legendary_prize", "Legendary Prize", "100€ credit for legend status", 50, 100.0, "EUR");

    private final String id;
    private final String name;
    private final String description;
    private final int requiredParticipations;
    private final double value;
    private final String currency;

    GamificationReward(String id, String name, String description, int requiredParticipations, double value, String currency) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requiredParticipations = requiredParticipations;
        this.value = value;
        this.currency = currency;
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

    public double getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }

    public static List<GamificationReward> getUnlockedRewards(int participationCount) {
        return Arrays.stream(values())
            .filter(reward -> participationCount >= reward.getRequiredParticipations())
            .collect(Collectors.toList());
    }

    public static List<GamificationReward> getNewlyUnlockedRewards(int oldParticipationCount, int newParticipationCount) {
        List<GamificationReward> oldRewards = getUnlockedRewards(oldParticipationCount);
        List<GamificationReward> newRewards = getUnlockedRewards(newParticipationCount);

        newRewards.removeAll(oldRewards);
        return newRewards;
    }
}
