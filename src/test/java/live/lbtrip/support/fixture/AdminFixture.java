package live.lbtrip.support.fixture;

import live.lbtrip.domain.admin.admin.model.Admin;

public final class AdminFixture {

    public static final long ADMIN_ID = 1L;
    public static final String NAME = "관리자";
    public static final String EMAIL = "admin@example.com";
    public static final String PASSWORD = "adminpass1";
    public static final String ENCODED_PASSWORD = "encoded-admin-password";

    private AdminFixture() {
    }

    public static Admin admin() {
        return Admin.create(NAME, EMAIL, ENCODED_PASSWORD);
    }
}
