package petrolpark.mc.destroy.core.pollution;

import net.createmod.catnip.config.ConfigBase;

public class ClientPollutionConfigs extends ConfigBase {

    public final ConfigBool smogAffectsBlockColors = b(true, "smogAffectsBlockColors", "Whether increased levels of Smog causes foliage to turn browner");
    // Read by petrolpark.mc.destroy.client.FogHandler.
    public final ConfigBool smog = b(true, "smog", "The sky turns browner the higher the Smog level");
    // Read by
    // petrolpark.mc.destroy.mixin.client.LevelRendererMixin to gate the rain shader-color tint
    // (blue → acid green proportional to ACID_RAIN pollution). Without this config + the mixin,
    // rain renders untinted regardless of pollution (1.21 port silently dropped both).
    public final ConfigBool rainColorChanges = b(true, "rainColorChanges", "The rain turns greener the higher the Acid Rain level");

    @Override
    public String getName() {
        return "pollution";
    };
    
};
