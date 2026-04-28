package moonulio.skinpresets.client.mojang;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;

public final class MojangSkinService {
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    private MojangSkinService() {
    }

    public static String applySkinFromNickname(MinecraftClient client, String nickname) throws Exception {
        String profileId = resolveProfileIdByNickname(nickname);
        String skinUrl = resolveSkinUrlFromProfileId(profileId);
        applySkinFromUrl(client, skinUrl);
        return "Скин из ника " + nickname + " применён.";
    }

    public static String applySkinFromUrl(MinecraftClient client, String skinUrl) throws Exception {
        Session session = requireSession(client);

        String body = "model=" + encode("slim") + "&url=" + encode(skinUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.minecraftservices.com/minecraft/profile/skins"))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + session.getAccessToken())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        ensureSuccess(response, "Не удалось поставить скин по URL");
        return response.body();
    }

    public static String applySkinFromFile(MinecraftClient client, Path file) throws Exception {
        Session session = requireSession(client);

        if (!Files.exists(file)) {
            throw new IllegalArgumentException("Файл не найден: " + file.toAbsolutePath());
        }

        byte[] png = Files.readAllBytes(file);
        String boundary = "----SkinPresetsBoundary" + System.currentTimeMillis();

        byte[] body = MultipartBodyBuilder.create(boundary)
                .addTextPart("variant", "slim")
                .addFilePart("file", file.getFileName().toString(), "image/png", png)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.minecraftservices.com/minecraft/profile/skins"))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + session.getAccessToken())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .PUT(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        ensureSuccess(response, "Не удалось поставить скин по файлу");
        return response.body();
    }

    private static String resolveProfileIdByNickname(String nickname) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + encode(nickname)))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        ensureSuccess(response, "Ник не найден в Mojang API");

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        if (!json.has("id")) {
            throw new IOException("Mojang API не вернул id профиля для ника: " + nickname);
        }

        return json.get("id").getAsString();
    }

    private static String resolveSkinUrlFromProfileId(String profileId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + profileId))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        ensureSuccess(response, "Не удалось получить текстуру по профилю");

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        String base64 = json.getAsJsonArray("properties")
                .get(0).getAsJsonObject()
                .get("value").getAsString();

        String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
        JsonObject textureData = JsonParser.parseString(decoded).getAsJsonObject();
        return textureData.getAsJsonObject("textures")
                .getAsJsonObject("SKIN")
                .get("url").getAsString();
    }

    private static Session requireSession(MinecraftClient client) {
        Session session = client.getSession();
        if (session == null || session.getAccessToken() == null || session.getAccessToken().isBlank()) {
            throw new IllegalStateException("Нет access token. Нужен лицензионный вход в лаунчере.");
        }
        return session;
    }

    private static void ensureSuccess(HttpResponse<String> response, String context) throws IOException {
        int code = response.statusCode();
        if (code < 200 || code >= 300) {
            throw new IOException(context + ". HTTP " + code + " -> " + response.body());
        }
    }

    private static String encode(String raw) {
        return URLEncoder.encode(raw, StandardCharsets.UTF_8);
    }
}
