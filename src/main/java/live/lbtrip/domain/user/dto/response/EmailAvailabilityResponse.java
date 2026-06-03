package live.lbtrip.domain.user.dto.response;

public record EmailAvailabilityResponse(
	boolean available
) {
	public static EmailAvailabilityResponse of(boolean available) {
		return new EmailAvailabilityResponse(available);
	}
}
