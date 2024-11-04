package toni.immersivetips.foundation.config;

import toni.lib.config.ConfigBase;

import java.util.ArrayList;
import java.util.List;

#if FABRIC
    import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
    #if after_21_1
    import net.neoforged.fml.config.ModConfig;
    import net.neoforged.neoforge.common.ModConfigSpec;
    import net.neoforged.neoforge.common.ModConfigSpec.*;
    #else
    import net.minecraftforge.fml.config.ModConfig;
    import net.minecraftforge.common.ForgeConfigSpec;
    import net.minecraftforge.common.ForgeConfigSpec.*;
    #endif
#endif

#if FORGE
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.minecraftforge.fml.config.ModConfig;
#endif

#if NEO
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.*;
#endif

public class CClient extends ConfigBase {

    public final ConfigGroup client = group(0, "client", "Client-only settings - If you're looking for general settings, look inside your world's serverconfig folder!");

    public final ConfigBool example = b(true, "example", "Example Boolean");

    public final CValue<List<String>, ConfigValue<List<String>>> whitelistedScreens
        = new CValue<>("Whitelisted Screens", builder -> builder.define("Whitelisted Screens", new ArrayList<>()), "Sets screens to manually enable tips on.");

    public final CValue<List<String>, ConfigValue<List<String>>> blacklistedScreens
        = new CValue<>("Blacklisted Screens", builder -> builder.define("Blacklisted Screens", new ArrayList<>()), "Sets screens to manually disable tips on.");

    @Override
    public String getName() {
        return "client";
    }
}