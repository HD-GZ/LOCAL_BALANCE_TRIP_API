package live.lbtrip.global.util;

import com.fasterxml.jackson.databind.JsonNode;

public final class JsonNodes {

    private JsonNodes() {
    }

    public static String textOrNull(JsonNode node, String field) {
        String value = node.path(field).asText("");
        return value.isBlank() ? null : value;
    }

    public static Double doubleOrNull(JsonNode node, String field) {
        String value = node.path(field).asText("");
        if (value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
