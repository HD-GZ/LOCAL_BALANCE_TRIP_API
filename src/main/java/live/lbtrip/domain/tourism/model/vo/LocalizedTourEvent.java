package live.lbtrip.domain.tourism.model.vo;

import java.time.LocalDate;

import live.lbtrip.domain.tourism.model.entity.TourEvent;

public record LocalizedTourEvent(
    String title,
    String imageUrl,
    LocalDate startDate,
    LocalDate endDate,
    String address
) {

    public static LocalizedTourEvent from(TourEvent event) {
        return new LocalizedTourEvent(
            event.getTitle(),
            event.getImageUrl(),
            event.getEventStart(),
            event.getEventEnd(),
            event.getAddress());
    }
}
