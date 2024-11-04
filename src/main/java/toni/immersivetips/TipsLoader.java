package toni.immersivetips;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import toni.immersivetips.api.CollectTips;
import toni.immersivetips.foundation.ImmersiveTip;
import toni.immersivetips.foundation.LegacyTip;
import toni.lib.utils.VersionUtils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class TipsLoader implements SimpleSynchronousResourceReloadListener {
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
        ImmersiveTips.EnabledTips.clear();
        CollectTips.EVENT.invoker().onCollectTips(tips);
        addDefaultTips(tips);
        ImmersiveTips.EnabledTips.addAll(tips);

        var resources = manager.listResources("tips", (path) -> true);
        for (ResourceLocation id : resources.keySet()) {
            try {
                InputStream stream = manager.getResource(id).get().open();
                if (id.getNamespace().equals("immersivetips"))
                    loadImmersiveTip(id, stream);

                if (id.getNamespace().equals("tipsmod"))
                    loadLegacyTip(id, stream);

            } catch (IOException e) {
                ImmersiveTips.LOGGER.error("Error occurred while loading resource tip {}", id.toString(), e);
            }
        }
    }

    private void addDefaultTips(List<ImmersiveTip> tips) {


    }

    private void loadImmersiveTip(ResourceLocation id, InputStream stream) {

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
                .ifPresent(legacyTip -> ImmersiveTips.EnabledTips.add(new ImmersiveTip("Tip", legacyTip.translate)));

        } catch (IOException e) {
            ImmersiveTips.LOGGER.error("Error occurred while reading stream for resource tip {}", id.toString(), e);
        }
    }
}
