package toni.immersivetips;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import toni.immersivetips.foundation.ImmersiveTip;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import toni.immersivetips.foundation.TipsPersistentData;
import toni.immersivetips.foundation.TipsResourceReloadListener;
import toni.immersivetips.foundation.config.AllConfigs;

import java.util.*;

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;
#endif


#if NEO
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
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

    public static TipsPersistentData persistentData;

    public static List<ImmersiveTip> LocalTips = new ArrayList<>();
    public static List<ImmersiveTip> RemoteTips = new ArrayList<>();

    public static Map<ImmersiveTip.Priority, List<ImmersiveTip>> EnabledTips = new HashMap<>();
    public static List<ImmersiveTip> DisabledTips = new ArrayList<>();
    public static Map<ResourceLocation, List<Component>> ItemTooltips = new HashMap<>();

    public ImmersiveTips(#if NEO IEventBus modEventBus, ModContainer modContainer #endif) {
        #if FORGE
        var context = FMLJavaModLoadingContext.get();
        var modEventBus = context.getModEventBus();
        #endif

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new TipsResourceReloadListener());

        #if FORGELIKE
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        AllConfigs.register((type, spec) -> {
            #if FORGE
            ModLoadingContext.get().registerConfig(type, spec);
            #elif NEO
            modContainer.registerConfig(type, spec);
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

        var highSize = high != null && !high.isEmpty() ? high.size() : 0;
        var mediumSize = high != null && !high.isEmpty() ? high.size() : 0;

        var highWeight = (1f - Math.max(0.3f, Math.min(0.4f, highSize * 0.02f)));
        var mediumWeight = (highWeight - Math.max(0.3f, Math.min(0.3f, mediumSize * 0.02f)));

        if (immediate != null && !immediate.isEmpty()) {
            list = immediate;
        }
        else if (high != null && !high.isEmpty() && weight > highWeight) {
            list = high;
        }
        else if (medium != null && !medium.isEmpty() && weight > mediumWeight) {
            list = medium;
        }
        else {
            list = low;
        }

        if (list == null || list.isEmpty())
            return null;

        ImmersiveTip tip = list.get(random.nextInt(list.size()));

        if (tip.multiplier > -1) {
            tip.multiplier -= 1;
            if (tip.multiplier <= 0) {
                list.remove(tip);
                DisabledTips.add(tip);
                persistentData.seenTips.add(tip.hashCode());
                persistentData.save();
            }
        }

        return tip;
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
        persistentData = TipsPersistentData.load();

        ImmersiveTipsClient.init();

        ClientLifecycleEvents.CLIENT_STOPPING.register((mc) -> ImmersiveTips.persistentData.save());
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
        return FMLLoader.getDist() == Dist.DEDICATED_SERVER;
        #endif
    }

}
