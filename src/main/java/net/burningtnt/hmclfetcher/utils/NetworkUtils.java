package net.burningtnt.hmclfetcher.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class NetworkUtils {
    private NetworkUtils() {
    }

    public static String withQuery(String baseURL, Map<String, String> queryArgs) {
        if (queryArgs.isEmpty()) {
            return baseURL;
        }

        StringBuilder stringBuilder = new StringBuilder(baseURL);
        stringBuilder.append('?');
        for (Map.Entry<String, String> arg : queryArgs.entrySet()) {
            stringBuilder.append(encodeURL(arg.getKey()));
            stringBuilder.append('=');
            stringBuilder.append(encodeURL(arg.getValue()));
            stringBuilder.append('&');
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    public static String encodeURL(String raw) {
        return URLEncoder.encode(raw, StandardCharsets.UTF_8);
    }
}
