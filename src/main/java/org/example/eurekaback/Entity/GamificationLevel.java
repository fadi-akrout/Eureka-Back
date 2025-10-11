package org.example.eurekaback.Entity;

public enum GamificationLevel {
    BRONZE(0, "Bronze", "#CD7F32", "fas fa-award"),
    ARGENT(3, "Silver", "#C0C0C0", "fas fa-medal"),
    OR(10, "Gold", "#FFD700", "fas fa-trophy"),
    DIAMANT(25, "Diamond", "#B9F2FF", "fas fa-gem");

    private final int requiredParticipations;
    private final String displayName;
    private final String color;
    private final String iconClass;

    GamificationLevel(int requiredParticipations, String displayName, String color, String iconClass) {
        this.requiredParticipations = requiredParticipations;
        this.displayName = displayName;
        this.color = color;
        this.iconClass = iconClass;
    }

    public int getRequiredParticipations() {
        return requiredParticipations;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public String getIconClass() {
        return iconClass;
    }

    public static GamificationLevel calculateLevel(int participationCount) {
        if (participationCount >= 25) return DIAMANT;
        if (participationCount >= 10) return OR;
        if (participationCount >= 3) return ARGENT;
        return BRONZE;
    }

    public int getParticipationsToNextLevel(int currentParticipations) {
        switch (this) {
            case BRONZE: return Math.max(0, 3 - currentParticipations);
            case ARGENT: return Math.max(0, 10 - currentParticipations);
            case OR: return Math.max(0, 25 - currentParticipations);
            case DIAMANT: return 0; // Max level reached
            default: return 0;
        }
    }

    public double calculateLevelProgress(int participationCount) {
        switch (this) {
            case BRONZE:
                return Math.min(100.0, (participationCount / 3.0) * 100);
            case ARGENT:
                return Math.min(100.0, ((participationCount - 3) / 7.0) * 100);
            case OR:
                return Math.min(100.0, ((participationCount - 10) / 15.0) * 100);
            case DIAMANT:
                return 100.0;
            default:
                return 0.0;
        }
    }
}
