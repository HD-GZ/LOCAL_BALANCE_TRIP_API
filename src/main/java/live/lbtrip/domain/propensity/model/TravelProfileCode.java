package live.lbtrip.domain.propensity.model;

public record TravelProfileCode(String value) {

    private static final int HIGH_POLE_MIN_SCORE = 4;

    public static TravelProfileCode from(Preference preference) {
        return new TravelProfileCode(
            pole(preference.getLocality(), "H", "L")
                + pole(preference.getFrugality(), "P", "V")
                + pole(preference.getExperientiality(), "S", "E")
                + pole(preference.getVitality(), "R", "A")
                + pole(preference.getSociality(), "I", "G")
        );
    }

    private static String pole(int score, String low, String high) {
        return score >= HIGH_POLE_MIN_SCORE ? high : low;
    }
}
