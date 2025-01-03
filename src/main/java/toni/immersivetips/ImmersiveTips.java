package toni.immersivetips;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.gui.screens.*;
import net.minecraft.server.packs.PackType;
import toni.immersivetips.foundation.ImmersiveTip;
import toni.immersivetips.foundation.config.AllConfigs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;


#if FABRIC
    import net.fabricmc.loader.api.FabricLoader;
    import net.fabricmc.api.EnvType;
    import net.fabricmc.api.ClientModInitializer;
    import net.fabricmc.api.ModInitializer;
    #if after_21_1
    import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
    import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.client.ConfigScreenFactoryRegistry;
    import net.neoforged.neoforge.client.gui.ConfigurationScreen;
    #endif

    #if current_20_1
    import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
    #endif
#endif

#if FORGE
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.FMLCommonHandler;
#endif


#if NEO
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;
#endif


#if FORGELIKE
@Mod("immersivetips")
#endif
public class ImmersiveTips #if FABRIC implements ModInitializer, ClientModInitializer #endif
{
    public static final String MODNAME = "Immersive Tips";
    public static final String ID = "immersivetips";
    public static final Logger LOGGER = LogManager.getLogger(MODNAME);
    private static final Random random = new Random();

    public static Set<Class<? extends Screen>> EnabledScreens = new HashSet<>();

    public static List<ImmersiveTip> AllTips = new ArrayList<>();
    public static Map<ImmersiveTip.Priority, List<ImmersiveTip>> EnabledTips = new HashMap<>();
    public static List<ImmersiveTip> DisabledTips = new ArrayList<>();

    public ImmersiveTips(#if NEO IEventBus modEventBus, ModContainer modContainer #endif) {
        #if FORGE
        var context = FMLJavaModLoadingContext.get();
        var modEventBus = context.getModEventBus();
        #endif

        #if FORGELIKE
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        AllConfigs.register((type, spec) -> {
            #if FORGE
            ModLoadingContext.get().registerConfig(type, spec);
            #elif NEO
            modContainer.registerConfig(type, spec);
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
            #endif
        });
        #endif
    }

    public static ImmersiveTip getNextTip() {
        List<ImmersiveTip> list = null;
        double weight = Math.random();

        // check immediate prio tips and remove all that are expired
        var immediate = EnabledTips.get(ImmersiveTip.Priority.IMMEDIATE);
        var high = EnabledTips.get(ImmersiveTip.Priority.HIGH);
        var medium = EnabledTips.get(ImmersiveTip.Priority.MEDIUM);
        var low = EnabledTips.get(ImmersiveTip.Priority.LOW);

        if (immediate != null && !immediate.isEmpty()) {
            list = immediate;
        }
        else if (weight > 0.5f && high != null && !high.isEmpty()) {
            list = high;
        }
        else if (weight > 0.25f  && medium != null && !medium.isEmpty()) {
            list = medium;
        }
        else {
            list = low;
        }

        ImmersiveTip tip = list.get(random.nextInt(list.size()));

        if (tip.multiplier > -1) {
            tip.multiplier -= 1;
            if (tip.multiplier <= 0) {
                list.remove(tip);
                DisabledTips.add(tip);
            }
        }

        return tip;
    }


    public static void addEnabledScreen(Class<? extends Screen> screenClass) {
        EnabledScreens.add(screenClass);
    }

    #if FABRIC @Override #endif
    public void onInitialize() {
        #if FABRIC
            AllConfigs.register((type, spec) -> {
                #if AFTER_21_1
                NeoForgeConfigRegistry.INSTANCE.register(ImmersiveTips.ID, type, spec);
                #else
                ForgeConfigRegistry.INSTANCE.register(ImmersiveTips.ID, type, spec);
                #endif
            });
        #endif
    }

    #if FABRIC @Override #endif
    public void onInitializeClient() {
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

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new TipsLoader());

        String url = "https://raw.githubusercontent.com/your-repo/your-project/main/data.json";
        CompletableFuture.supplyAsync(() -> RemoteTipConfig.fetchAndParseJson(url))
            .thenAccept(remoteTipConfigs -> {
                // Process the parsed list on completion
                if (remoteTipConfigs != null) {
                    remoteTipConfigs.forEach(System.out::println);
                } else {
                    System.err.println("Failed to fetch or parse data.");
                }
            });
    }

    // Forg event stubs to call the Fabric initialize methods, and set up cloth config screen
    #if FORGELIKE
    public void commonSetup(FMLCommonSetupEvent event) { onInitialize(); }
    public void clientSetup(FMLClientSetupEvent event) { onInitializeClient(); }
    #endif

    public static boolean isDedicatedServer() {
        #if FABRIC
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
        #elif NEO
        return FMLLoader.getDist() == Dist.DEDICATED_SERVER;
        #else
        return FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer();
        #endif
    }

}
