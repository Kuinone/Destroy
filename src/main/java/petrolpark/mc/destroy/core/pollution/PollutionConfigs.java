package petrolpark.mc.destroy.core.pollution;

import net.createmod.catnip.config.ConfigBase;

public class PollutionConfigs extends ConfigBase {

    public final ConfigBool enablePollution = b(true, "enable", "Releasing chemicals increases pollution, and pollution has effects on the world");

    public final ConfigFloat acidRainReplacementChance = f(0.1f, 0f, 1f, "acidRainReplacementChance", "Chance that a randomly ticked Block is destroyed or changed by Acid Rain");
    public final ConfigFloat acidRainOxidationChance = f(0.2f, 0f, 1f, "acidRainOxidationChance", "Chance that a randomly ticked oxidizable Block is oxidized by Acid Rain");
    public final ConfigBool temperatureAffected = b(true, "temperatureAffected", Comments.temperatureAffected);
    public final ConfigBool ozoneDepletionGivesCancer = b(true, "ozoneDepletionGivesCancer", Comments.ozoneDepletionGivesCancer);
    // public final ConfigBool itemCarboxylationAffected = b(true, "itemCarboxylationAffected", "Items which carboxylate like Quicklime do so faster with higher Greenhouse Gas levels");
    public final ConfigBool vatUVPowerAffected = b(true, "vatUVPowerAffected", "Whether the level of Ozone Depletion increases the amount of UV power supplied to Vats by the sun");
    
    @Override
	public String getName() {
		return "pollution";
	};

    public static class Client extends ConfigBase {

        public final ConfigBool smog = b(true, "smog", "The sky and grass turn browner the higher the Smog level");
        public final ConfigBool rainColorChanges = b(true, "rainColorChanges", "The rain turns greener the higher the Acid Rain level");

        @Override
        public String getName() {
            return "pollution";
        };
    };

    private static class Comments {
        static String

        temperatureAffected = "Outdoor temperature (which affects Distillation Towers and Vats) increases with Greenhouse Gas and Ozone Depletion levels",
        ozoneDepletionGivesCancer = "The likelihood of getting the cancer awareness pop-up from the sun increases with the Ozone Depletion level";
    };
    
};
