package live.lbtrip.global.error;

public record FieldErrorDetail(
    String field,
    String message
) {
    public static FieldErrorDetail of(String field, String message) {
        return new FieldErrorDetail(field, message);
    }
}
