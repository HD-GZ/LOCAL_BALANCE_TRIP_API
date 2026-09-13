package live.lbtrip.global.storage.service;

import java.util.Locale;

public interface AudioStorage {

    String storeTts(byte[] audio, Locale locale);

    String publicUrl(String key);
}
