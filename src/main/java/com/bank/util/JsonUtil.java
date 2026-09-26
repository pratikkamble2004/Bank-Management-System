package com.bank.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class JsonUtil {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    private JsonUtil() {}

    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    public static <T> T fromRequest(HttpServletRequest request, Class<T> classOfT) throws IOException {
        int length = request.getContentLength();
        if (length <= 0) {
            return null;
        }
        byte[] bytes = request.getInputStream().readNBytes(length);
        String json = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        if (json.isBlank()) {
            return null;
        }
        return fromJson(json, classOfT);
    }

    public static void sendResponse(HttpServletResponse response, int status, Object obj) throws IOException {
        String json = toJson(obj);
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }
}
