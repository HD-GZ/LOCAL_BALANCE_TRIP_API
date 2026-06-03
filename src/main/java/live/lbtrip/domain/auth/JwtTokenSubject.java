package live.lbtrip.domain.auth;

public record JwtTokenSubject(
	Long userId,
	String email
) {
}
