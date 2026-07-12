package live.lbtrip.domain.admin.auth.model;

public record AdminJwtTokenSubject(
    Long adminId
) {

    public static AdminJwtTokenSubject of(Long adminId) {
        return new AdminJwtTokenSubject(adminId);
    }
}
