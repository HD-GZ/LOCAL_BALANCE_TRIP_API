package live.lbtrip.global.storage;

public enum ImageDirectory {

    RECEIPT("receipts");

    private final String path;

    ImageDirectory(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}
