package toni.immersivetips.foundation;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TipsPersistentData {
    public static final File FILE = new File("immersivetips.json");
    public static Date timeStarted = new Date();

    public static final Codec<TipsPersistentData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("playtime").forGetter((data) -> data.playtime),
            Codec.INT.listOf().optionalFieldOf("seenTips", new ArrayList<>()).forGetter(data -> data.seenTips)
        ).apply(instance, TipsPersistentData::new)
    );

    public TipsPersistentData(Integer playtime, List<Integer> seenTips) {
        this.playtime = playtime;
        this.seenTips = seenTips;
    }

    public int playtime;
    public List<Integer> seenTips;

    public void save() {
        long millis = new Date().getTime() - timeStarted.getTime();
        playtime += (int) (millis / 1000);
        timeStarted = new Date();

        try (FileWriter writer = new FileWriter(FILE)) {
            JsonElement jsonElement = CODEC.encodeStart(JsonOps.INSTANCE, this)
                .getOrThrow(#if mc < 211 false, #endif error -> {
                    throw new RuntimeException("Failed to encode ImmersiveTips PersistentData: " + error);
                });
            writer.write(jsonElement.toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save ImmersiveTips PersistentData! ", e);
        }
    }

    public static TipsPersistentData load() {
        if (!FILE.exists())
            return new TipsPersistentData(0, new ArrayList<>());

        try (FileReader reader = new FileReader(FILE)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            return CODEC.parse(JsonOps.INSTANCE, jsonElement)
                .getOrThrow(#if mc < 211 false, #endif error -> {
                    throw new RuntimeException("Failed to decode ImmersiveTips PersistentData: " + error);
                });
        } catch (IOException e) {
            throw new RuntimeException("Failed to load ImmersiveTips PersistentData! ", e);
        }
    }
}
