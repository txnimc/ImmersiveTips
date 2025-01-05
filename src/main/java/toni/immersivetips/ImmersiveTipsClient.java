package toni.immersivetips;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.screens.*;
import toni.immersivetips.foundation.data.RemoteTipConfig;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

#if FABRIC
    #if after_21_1
        import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
        import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.client.ConfigScreenFactoryRegistry;
        import net.neoforged.neoforge.client.gui.ConfigurationScreen;
        #endif

    #if current_20_1
    #endif
#endif

public class ImmersiveTipsClient {
    public static Set<Class<? extends Screen>> EnabledScreens = new HashSet<>();


    public static void init() {
        #if AFTER_21_1
            #if FABRIC
            ConfigScreenFactoryRegistry.INSTANCE.register(ImmersiveTips.ID, ConfigurationScreen::new);
            #endif
        #endif

        addEnabledScreen(LevelLoadingScreen.class);
        addEnabledScreen(PauseScreen.class);
        addEnabledScreen(ProgressScreen.class);
        addEnabledScreen(ConnectScreen.class);
        addEnabledScreen(DeathScreen.class);
        addEnabledScreen(DisconnectedScreen.class);

        #if AFTER_21_1
        addEnabledScreen(GenericMessageScreen.class);
        #endif

        ItemTooltipCallback.EVENT.register((item, context, #if mc >= 211 flag, #endif list) -> {
            var tips = ImmersiveTips.ItemTooltips.get(item.getItem().builtInRegistryHolder().key().location());
            if (tips != null) {
                list.addAll(tips);
            }
        });

        String url = "https://raw.githubusercontent.com/txnimc/ImmersiveTips/refs/heads/main/tooltips.json";
        CompletableFuture.runAsync(() -> RemoteTipConfig.fetchAndParseJson(url));
    }

    public static void addEnabledScreen(Class<? extends Screen> screenClass) {
        EnabledScreens.add(screenClass);
    }
}
