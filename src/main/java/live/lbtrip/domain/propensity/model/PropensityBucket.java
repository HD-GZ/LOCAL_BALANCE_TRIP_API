package live.lbtrip.domain.propensity.model;

public enum PropensityBucket {
    LOW,
    NEUTRAL,
    HIGH;

    public static PropensityBucket fromScore(int score) {
        if (score <= 2) {
            return LOW;
        }
        if (score == 3) {
            return NEUTRAL;
        }
        return HIGH;
    }
}
