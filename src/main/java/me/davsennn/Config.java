package me.davsennn;

public class Config {

    private static double PREFERENCE_BONUS;
    private static double NON_PREFERENCE_PENALTY;
    private static double UNFULFILLED_PREFERENCE_PENALTY;
    private static double MUTUAL_PREFERENCE_BONUS;
    private static double AGE_DIFFERENCE_PENALTY;
    private static double LARGE_AGE_DIFFERENCE_PENALTY;
    private static double SAME_LOCATION_BONUS;
    private static double SAME_GENDER_BONUS;

    private static double LARGE_GROUP_BONUS;
    private static double CRITICAL_OCCUPANCY_PENALTY;
    private static double UNDER_OCCUPANCY_PENALTY;

    private static double AGE_DIFFERENCE_THRESHOLD;
    private static double LARGE_AGE_DIFFERENCE_THRESHOLD;

    public static void setDefaults() {
        setPreferenceBonus(2.0);
        setNonPreferencePenalty(0.5);
        setUnfulfilledPreferencePenalty(4.0);
        setMutualPreferenceBonus(10.0);
        setAgeDifferencePenalty(1.0);
        setLargeAgeDifferencePenalty(2.0);
        setSameLocationBonus(1.0);
        setSameGenderBonus(2.0);

        setLargeGroupBonus(0.0);
        setUnderOccupancyPenalty(1.0);
        setCriticalOccupancyPenalty(10.0);

        setAgeDifferenceThreshold(2.0);
        setLargeAgeDifferenceThreshold(3.0);
    }

    public static double getPreferenceBonus() {
        return PREFERENCE_BONUS;
    }

    public static void setPreferenceBonus(double preferenceBonus) {
        PREFERENCE_BONUS = preferenceBonus;
    }

    public static double getNonPreferencePenalty() {
        return NON_PREFERENCE_PENALTY;
    }

    public static void setNonPreferencePenalty(double nonPreferencePenalty) {
        NON_PREFERENCE_PENALTY = nonPreferencePenalty;
    }

    public static double getUnfulfilledPreferencePenalty() {
        return UNFULFILLED_PREFERENCE_PENALTY;
    }

    public static void setUnfulfilledPreferencePenalty(double unfulfilledPreferencePenalty) {
        UNFULFILLED_PREFERENCE_PENALTY = unfulfilledPreferencePenalty;
    }

    public static double getMutualPreferenceBonus() {
        return MUTUAL_PREFERENCE_BONUS;
    }

    public static void setMutualPreferenceBonus(double mutualPreferenceBonus) {
        MUTUAL_PREFERENCE_BONUS = mutualPreferenceBonus;
    }

    public static double getAgeDifferencePenalty() {
        return AGE_DIFFERENCE_PENALTY;
    }

    public static void setAgeDifferencePenalty(double ageDifferencePenalty) {
        AGE_DIFFERENCE_PENALTY = ageDifferencePenalty;
    }

    public static double getLargeAgeDifferencePenalty() {
        return LARGE_AGE_DIFFERENCE_PENALTY;
    }

    public static void setLargeAgeDifferencePenalty(double largeAgeDifferencePenalty) {
        LARGE_AGE_DIFFERENCE_PENALTY = largeAgeDifferencePenalty;
    }

    public static double getSameLocationBonus() {
        return SAME_LOCATION_BONUS;
    }

    public static void setSameLocationBonus(double sameLocationBonus) {
        SAME_LOCATION_BONUS = sameLocationBonus;
    }

    public static double getSameGenderBonus() {
        return SAME_GENDER_BONUS;
    }

    public static void setSameGenderBonus(double sameGenderBonus) {
        SAME_GENDER_BONUS = sameGenderBonus;
    }

    public static double getLargeGroupBonus() {
        return LARGE_GROUP_BONUS;
    }

    public static void setLargeGroupBonus(double largeGroupBonus) {
        LARGE_GROUP_BONUS = largeGroupBonus;
    }

    public static double getCriticalOccupancyPenalty() {
        return CRITICAL_OCCUPANCY_PENALTY;
    }

    public static void setCriticalOccupancyPenalty(double criticalOccupancyPenalty) {
        CRITICAL_OCCUPANCY_PENALTY = criticalOccupancyPenalty;
    }

    public static double getUnderOccupancyPenalty() {
        return UNDER_OCCUPANCY_PENALTY;
    }

    public static void setUnderOccupancyPenalty(double underOccupancyPenalty) {
        UNDER_OCCUPANCY_PENALTY = underOccupancyPenalty;
    }

    public static double getAgeDifferenceThreshold() {
        return AGE_DIFFERENCE_THRESHOLD;
    }

    public static void setAgeDifferenceThreshold(double ageDifferenceThreshold) {
        if (ageDifferenceThreshold < 0) return;
        AGE_DIFFERENCE_THRESHOLD = ageDifferenceThreshold;
    }

    public static double getLargeAgeDifferenceThreshold() {
        return LARGE_AGE_DIFFERENCE_THRESHOLD;
    }

    public static void setLargeAgeDifferenceThreshold(double largeAgeDifferenceThreshold) {
        if (largeAgeDifferenceThreshold < 0) return;
        LARGE_AGE_DIFFERENCE_THRESHOLD = largeAgeDifferenceThreshold;
    }

}
