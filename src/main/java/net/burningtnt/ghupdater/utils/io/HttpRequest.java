/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2021  huangyuhui <huanghongxun2008@126.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.burningtnt.ghupdater.utils.io;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import net.burningtnt.ghupdater.utils.NetworkUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class HttpRequest {
    protected final Gson GSON = new Gson();

    protected final String url;
    protected final String method;
    protected final Map<String, String> headers = new HashMap<>();

    private HttpRequest(String url, String method) {
        this.url = url;
        this.method = method;
    }

    public HttpRequest accept(String contentType) {
        return header("Accept", contentType);
    }

    public HttpRequest authorization(String token) {
        return header("Authorization", token);
    }

    public HttpRequest header(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public abstract byte[] getRawData() throws IOException;

    public <T> T getJson(Class<T> typeOfT) throws IOException, JsonParseException {
        return GSON.fromJson(new InputStreamReader(new ByteArrayInputStream(getRawData())), typeOfT);
    }

    public <T> T getJson(Type type) throws IOException, JsonParseException {
        return GSON.fromJson(new InputStreamReader(new ByteArrayInputStream(getRawData())), type);
    }

    public HttpURLConnection createConnection() throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod(method);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }
        return con;
    }

    public static class HttpGetRequest extends HttpRequest {
        public HttpGetRequest(String url) {
            super(url, "GET");
        }

        @Override
        public byte[] getRawData() throws IOException {
            return createConnection().getInputStream().readAllBytes();
        }
    }

    public static final class HttpPostRequest extends HttpRequest {
        public HttpPostRequest(String url) {
            super(url, "POST");
        }

        public HttpPostRequest contentType(String contentType) {
            headers.put("Content-Type", contentType);
            return this;
        }

        public HttpPostRequest json(Object payload) throws JsonParseException {
            return string(payload instanceof String ? (String) payload : GSON.toJson(payload), "application/json");
        }

        public HttpPostRequest string(String payload, String contentType) {
            byte[] bytes = payload.getBytes(UTF_8);
            header("Content-Length", "" + bytes.length);
            contentType(contentType + "; charset=utf-8");
            return this;
        }

        @Override
        public byte[] getRawData() throws IOException {
            return createConnection().getInputStream().readAllBytes();
        }
    }

    public static HttpGetRequest GET(String url) {
        return new HttpGetRequest(url);
    }

    public static HttpGetRequest GET(String url, Map<String, String> query) {
        return GET(NetworkUtils.withQuery(url, query));
    }

    public static HttpPostRequest POST(String url) {
        return new HttpPostRequest(url);
    }

    @FunctionalInterface
    public interface ExceptionalSupplier<T, E extends Throwable> {
        T get() throws E;
    }
}
