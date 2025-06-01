package joshie.vbc.foundation.data;

#if FABRIC
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import joshie.vbc.VillagerBrainConfig;

public class VillagerBrainConfigDatagen  implements DataGeneratorEntrypoint {

    @Override
    public String getEffectiveModId() {
        return VillagerBrainConfig.ID;
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        var pack = fabricDataGenerator.createPack();
        pack.addProvider(ConfigLangDatagen::new);
    }
}
#endif