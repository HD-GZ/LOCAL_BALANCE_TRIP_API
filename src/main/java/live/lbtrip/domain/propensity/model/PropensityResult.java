package live.lbtrip.domain.propensity.model;

public record PropensityResult(
    String type,
    String description
) {

    public static PropensityResult of(String type, String description) {
        return new PropensityResult(type, description);
    }
}
