package toni.immersivetips.foundation;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import toni.immersivetips.ImmersiveTips;
import toni.immersivetips.api.CollectTips;
import toni.lib.utils.VersionUtils;

import com.mojang.serialization.JsonOps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TipsResourceReloadListener implements SimpleSynchronousResourceReloadListener {
    public static final Codec<LegacyTip> LEGACY_TIP_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf("translate").forGetter((tip) -> tip.translate)
        ).apply(instance, LegacyTip::new)
    );

    @Override
    public ResourceLocation getFabricId() {
        return VersionUtils.resource(ImmersiveTips.ID, "tips");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        var tips = new ArrayList<ImmersiveTip>();
        ImmersiveTips.LocalTips.clear();
        CollectTips.EVENT.invoker().onCollectTips(tips);
        addDefaultTips(tips);
        ImmersiveTips.LocalTips.addAll(tips);

        var resources = manager.listResources("tips", (path) -> true);
        for (ResourceLocation id : resources.keySet()) {
            try {
                InputStream stream = manager.getResource(id).get().open();
                if (id.getNamespace().equals("immersivetips"))
                    loadImmersiveTipList(id, stream);

                if (id.getNamespace().equals("tipsmod"))
                    loadLegacyTip(id, stream);

            } catch (IOException e) {
                ImmersiveTips.LOGGER.error("Error occurred while loading resource tip {}", id.toString(), e);
            }
        }

        loadEnabledTips();
    }

    public static void loadEnabledTips() {
        ImmersiveTips.EnabledTips.clear();
        ImmersiveTip.Priority.VALUES.forEach(priority -> ImmersiveTips.EnabledTips.put(priority, new ArrayList<>()));

        for (ImmersiveTip tip : ImmersiveTips.LocalTips) {
            if (ImmersiveTips.persistentData.seenTips.contains(tip.hashCode()))
                continue;

            ImmersiveTips.EnabledTips.get(tip.priority).add(tip);
        }

        for (ImmersiveTip tip : ImmersiveTips.RemoteTips) {
            if (ImmersiveTips.persistentData.seenTips.contains(tip.hashCode()))
                continue;

            ImmersiveTips.EnabledTips.get(tip.priority).add(tip);
        }
    }

    private void addDefaultTips(List<ImmersiveTip> tips) {


    }

    private void loadImmersiveTipList(ResourceLocation id, InputStream stream) {
        try (InputStreamReader reader = new InputStreamReader(stream)) {
            JsonObject jsonElement = JsonParser.parseReader(reader).getAsJsonObject();

            loadPriorityList(id, ImmersiveTip.Priority.IMMEDIATE, jsonElement);
            loadPriorityList(id, ImmersiveTip.Priority.HIGH, jsonElement);
            loadPriorityList(id, ImmersiveTip.Priority.MEDIUM, jsonElement);
            loadPriorityList(id, ImmersiveTip.Priority.LOW, jsonElement);
        } catch (IOException e) {
            ImmersiveTips.LOGGER.error("Error occurred while reading stream for resource tip {}", id.toString(), e);
        }
    }

    private void loadPriorityList(ResourceLocation id, ImmersiveTip.Priority priority, JsonObject json) {
        JsonElement tipList = json.get(priority.getSerializedName());
        if (tipList == null)
            return;

        if (!tipList.isJsonArray())
        {
            ImmersiveTips.LOGGER.error("Error! JSON data for list '{}' in {} is not an array!", priority.getSerializedName(), id);
            return;
        }

        var list = tipList.getAsJsonArray();

        list.forEach(element -> {
            ImmersiveTip.CODEC.parse(JsonOps.INSTANCE, element)
                .resultOrPartial(errorMsg -> ImmersiveTips.LOGGER.error("Failed to decode ImmersiveTip: {}", errorMsg))
                .ifPresent(tip -> {
                    tip.priority = priority;

                    // make sure that immediate prio messages are not set to show indefinitely
                    if (tip.priority == ImmersiveTip.Priority.IMMEDIATE)
                        tip.multiplier = Math.max(tip.multiplier, 1);

                    ImmersiveTips.LocalTips.add(tip);
                });
        });
    }


    private void loadLegacyTip(ResourceLocation id, InputStream stream) {
        try (InputStreamReader reader = new InputStreamReader(stream)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            JsonElement tipElement = jsonElement.getAsJsonObject().get("tip");

            if (tipElement == null)
            {
                ImmersiveTips.LOGGER.error("Failed to find 'tip' element in the JSON data for {}", id);
                return;
            }

            LEGACY_TIP_CODEC.parse(JsonOps.INSTANCE, tipElement)
                .resultOrPartial(errorMsg -> ImmersiveTips.LOGGER.error("Failed to decode LegacyTip: {}", errorMsg))
                .ifPresent(legacyTip -> ImmersiveTips.LocalTips.add(new ImmersiveTip("Tip", legacyTip.translate)));

        } catch (IOException e) {
            ImmersiveTips.LOGGER.error("Error occurred while reading stream for resource tip {}", id.toString(), e);
        }
    }
}
