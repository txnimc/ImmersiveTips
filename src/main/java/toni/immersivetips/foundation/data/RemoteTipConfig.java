package toni.immersivetips.foundation.data;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import toni.immersivetips.ImmersiveTips;
import toni.immersivetips.foundation.ImmersiveTip;
import toni.immersivetips.foundation.TipsResourceReloadListener;
import toni.lib.utils.VersionUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RemoteTipConfig {
    String id;
    List<String> tooltipLines;

    String modId;
    String itemName;
    List<String> tags;

    String title;
    String condition;
    String priority;

    public static void fetchAndParseJson(String url) {
        try {
            String jsonResponse = fetchJson(url);

            Gson gson = new Gson();
            Type listType = new TypeToken<List<RemoteTipConfig>>() {}.getType();

            var remoteTipConfigs = (List<RemoteTipConfig>) gson.fromJson(jsonResponse, listType);
            Minecraft.getInstance().execute(() -> loadTips(remoteTipConfigs));
        } catch (IOException | InterruptedException | JsonSyntaxException e) {
            ImmersiveTips.LOGGER.error(e.getMessage());
        }
    }

    private static String fetchJson(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch JSON: HTTP " + response.statusCode());
        }

        return response.body();
    }

    public static void loadTips(List<RemoteTipConfig> list) {
        var count = 0;
        var errors = 0;

        for (RemoteTipConfig tip : list) {
            try {
                if (tip.priority != null) {
                    ImmersiveTips.RemoteTips.add(new ImmersiveTip(
                        Optional.of(tip.title == null ? "Tip" : tip.title),
                        Optional.of(String.join("\n", tip.tooltipLines)),
                        Optional.ofNullable(tip.condition),
                        Optional.ofNullable(null),
                        Optional.of(15f),
                        Optional.of(-1),
                        Optional.of(ImmersiveTip.Priority.valueOf(tip.priority))
                    ));
                } else {
                    ImmersiveTips.ItemTooltips
                        .computeIfAbsent(VersionUtils.resource(tip.modId, tip.itemName), k -> new ArrayList<>())
                        .addAll(tip.tooltipLines.stream().map(RemoteTipConfig::translateWithStyle).toList());
                }

                count++;
            }
            catch (Exception e) {
                ImmersiveTips.LOGGER.error(e.getMessage());
                errors++;
            }

        }

        TipsResourceReloadListener.loadEnabledTips();
        ImmersiveTips.LOGGER.info("Loaded " + count + " tips (" + errors + " errors)");
    }

    public static Component translateWithStyle(String text) {
        MutableComponent rootComponent = Component.literal("");
        String[] parts = text.split("§");
        Style currentStyle = Style.EMPTY;

        for (String part : parts) {
            if (part.isEmpty()) continue;

            char code = part.charAt(0);
            String content = part.substring(1);

            switch (code) {
                case '0' -> currentStyle = Style.EMPTY.withColor(TextColor.parseColor("#000000") #if mc >= 211 .getOrThrow() #endif ); // Black
                case '1' -> currentStyle = Style.EMPTY.withColor(TextColor.parseColor("#0000AA") #if mc >= 211 .getOrThrow() #endif ); // Dark Blue
                case '2' -> currentStyle = Style.EMPTY.withColor(TextColor.parseColor("#00AA00") #if mc >= 211 .getOrThrow() #endif ); // Dark Green
                case '3' -> currentStyle = Style.EMPTY.withColor(TextColor.parseColor("#00AAAA") #if mc >= 211 .getOrThrow() #endif ); // Dark Aqua
                case '4' -> currentStyle = Style.EMPTY.withColor(TextColor.parseColor("#AA0000") #if mc >= 211 .getOrThrow() #endif ); // Dark Red
                case '5' -> currentStyle = Style.EMPTY.withColor(TextColor.parseColor("#AA00AA") #if mc >= 211 .getOrThrow() #endif ); // Dark Purple
                case '6' -> currentStyle = Style.EMPTY.withColor(TextColor.parseColor("#FFAA00") #if mc >= 211 .getOrThrow() #endif ); // Gold
                case '7' -> currentStyle = Style.EMPTY.withColor(TextColor.parseColor("#AAAAAA") #if mc >= 211 .getOrThrow() #endif ); // Gray
                case '8' -> currentStyle = Style.EMPTY.withColor(TextColor.parseColor("#555555") #if mc >= 211 .getOrThrow() #endif ); // Dark Gray
                case '9' -> currentStyle = Style.EMPTY.withColor(TextColor.parseColor("#5555FF") #if mc >= 211 .getOrThrow() #endif ); // Blue
                case 'a' -> currentStyle = Style.EMPTY.withColor(TextColor.parseColor("#55FF55") #if mc >= 211 .getOrThrow() #endif ); // Green
                case 'b' -> currentStyle = Style.EMPTY.withColor(TextColor.parseColor("#55FFFF") #if mc >= 211 .getOrThrow() #endif ); // Aqua
                case 'c' -> currentStyle = Style.EMPTY.withColor(TextColor.parseColor("#FF5555") #if mc >= 211 .getOrThrow() #endif ); // Red
                case 'd' -> currentStyle = Style.EMPTY.withColor(TextColor.parseColor("#FF55FF") #if mc >= 211 .getOrThrow() #endif ); // Light Purple
                case 'e' -> currentStyle = Style.EMPTY.withColor(TextColor.parseColor("#FFFF55") #if mc >= 211 .getOrThrow() #endif ); // Yellow
                case 'f' -> currentStyle = Style.EMPTY.withColor(TextColor.parseColor("#FFFFFF") #if mc >= 211 .getOrThrow() #endif ); // White
                case 'l' -> currentStyle = currentStyle.withBold(true);
                case 'm' -> currentStyle = currentStyle.withStrikethrough(true);
                case 'n' -> currentStyle = currentStyle.withUnderlined(true);
                case 'o' -> currentStyle = currentStyle.withItalic(true);
                case 'r' -> currentStyle = Style.EMPTY;
                default -> {}
            }

            if (!content.isEmpty()) {
                Component styledComponent = Component.literal(content).setStyle(currentStyle);
                rootComponent.append(styledComponent); // Append styled text to the root
            }
        }

        return rootComponent;
    }
}
