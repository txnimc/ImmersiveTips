package toni.immersivetips.foundation.data;

#if FABRIC
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import toni.immersivetips.ImmersiveTips;

public class ImmersiveTipsDatagen  implements DataGeneratorEntrypoint {

    @Override
    public String getEffectiveModId() {
        return ImmersiveTips.ID;
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        var pack = fabricDataGenerator.createPack();
        pack.addProvider(ConfigLangDatagen::new);
    }
}
#endif