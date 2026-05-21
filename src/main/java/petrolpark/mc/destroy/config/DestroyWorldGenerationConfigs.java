package petrolpark.mc.destroy.config;

import net.neoforged.neoforge.common.ModConfigSpec.Builder;

public class DestroyWorldGenerationConfigs extends DestroyConfigBase {

    /**
 * Increment this number if all worldgen config entries should be overwritten
 * in this update. Worlds from the previous version will overwrite potentially
 * changed values with the new defaults.
*/
    public static final int FORCED_UPDATE_VERSION = 0;

    public final ConfigBool disable = b(false, "disableWorldGen", Comments.disable);

    @Override
    public void registerAll(Builder builder) {
        super.registerAll(builder);
        // TODO: fill ore feature entries
        // AllOreFeatureConfigEntries.fillConfig(builder, Destroy.MOD_ID);
    }

    @Override
    public String getName() {
        return "worldGen";
    }

    private static class Comments {
        static String disable = "Prevents Destroy Ores from generating";
    }
}
