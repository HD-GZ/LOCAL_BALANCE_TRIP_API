package live.lbtrip.domain.recommendation.client.dto;

public record OdiiTheme(
    String tid,
    String tlid,
    String title,
    Double longitude,
    Double latitude
) {
}
