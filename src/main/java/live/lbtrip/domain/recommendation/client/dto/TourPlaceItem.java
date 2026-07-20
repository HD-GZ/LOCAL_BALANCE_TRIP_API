package live.lbtrip.domain.recommendation.client.dto;

public record TourPlaceItem(
    String contentId,
    String title,
    int contentTypeId,
    String imageUrl,
    Double longitude,
    Double latitude
) {
}
