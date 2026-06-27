package live.lbtrip.domain.auth.model;

public record JwtTokenSubject(
    Long userId
) {

    public static JwtTokenSubject of(Long userId) {
        return new JwtTokenSubject(userId);
    }
}
