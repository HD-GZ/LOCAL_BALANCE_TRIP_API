package live.lbtrip.domain.auth.model;

public record JwtTokenSubject(
	Long userId,
	String email
) {
}
