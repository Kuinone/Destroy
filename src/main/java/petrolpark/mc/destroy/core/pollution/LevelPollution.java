package petrolpark.mc.destroy.core.pollution;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import petrolpark.mc.destroy.DestroyAttachmentTypes;
import petrolpark.mc.destroy.DestroyPollutionTypes;
import petrolpark.mc.destroy.DestroyRegistries;
import petrolpark.mc.destroy.config.DestroyConfigs;

@EventBusSubscriber
public class LevelPollution extends Pollution<Level> {

    /**
 * Baseline outdoor "room" temperature in kelvins (≈ 16°C) used by chemistry Basin/Vat reaction
 * rate calculations. Per-Level actual outdoor temperature = this baseline + pollution deltas.
*/
    public static final float BASELINE_OUTDOOR_TEMPERATURE_K = 289f;

    public static final Serializer SERIALIZER = new Serializer();

    public LevelPollution(Level level, Map<PollutionType<Level>, Integer> values) {
        super(level, values);
    };

    public static LevelPollution create(IAttachmentHolder holder) {
        if (holder instanceof Level level) return new LevelPollution(level, DestroyRegistries.LEVEL_POLLUTION_TYPES.stream().collect(Collectors.toMap(Function.identity(), t -> 0)));
        throw new IllegalArgumentException();
    };

    @Override
    protected void syncInternal() {
        if (!holder.isClientSide()) CatnipServices.NETWORK.sendToAllClients(new LevelPollutionPacket(getValues()));
    };

    public void syncTo(ServerPlayer player) {
        CatnipServices.NETWORK.sendToClient(player, new LevelPollutionPacket(getValues()));
    };

    public static final void syncTo(Player entity) {
        if (entity instanceof ServerPlayer player) player.level().getData(DestroyAttachmentTypes.LEVEL_POLLUTION).syncTo(player);
    };

    @Override
    public PollutionType.Properties getProperties(PollutionType<Level> pollutionType) {
        return PollutionHelper.getLevelPollutionTypeProperties(pollutionType);
    };

    /**
 * Outdoor "room" temperature in kelvins for this Level, with pollution-driven deltas:
 * <ul>
 * <li>+0 up to +20 K from GREENHOUSE (linear in {@code get/max})</li>
 * <li>+0 up to +4 K from OZONE_DEPLETION (linear in {@code get/max})</li>
 * </ul>
 * When {@code temperatureAffected} server config is off, returns the flat baseline.
 *
 * @see PollutionHelper#getLocalTemperature(Level, net.minecraft.core.BlockPos)
*/
    public float getOutdoorTemperature() {
        if (!PollutionHelper.isPollutionEnabled()) return BASELINE_OUTDOOR_TEMPERATURE_K;
        if (!DestroyConfigs.server().pollution.temperatureAffected.get()) return BASELINE_OUTDOOR_TEMPERATURE_K;

        float delta = 0f;
        final PollutionType<Level> greenhouse = DestroyPollutionTypes.GREENHOUSE.get();
        final PollutionType<Level> ozone = DestroyPollutionTypes.OZONE_DEPLETION.get();
        delta += ((float) getPollution(greenhouse) / (float) getProperties(greenhouse).max()) * 20f;
        delta += ((float) getPollution(ozone) / (float) getProperties(ozone).max()) * 4f;
        return BASELINE_OUTDOOR_TEMPERATURE_K + delta;
    };

    public static class Serializer extends Pollution.Serializer<Level, LevelPollution> {

        public Serializer() {
            super(DestroyRegistries.LEVEL_POLLUTION_TYPES);
        };

        @Override
        protected LevelPollution create(Level holder, Map<PollutionType<Level>, Integer> values) {
            return new LevelPollution(holder, values);
        };

        @Override
        public Level castHolder(IAttachmentHolder holder) {
            if (holder instanceof Level level) return level;
            throw new IllegalArgumentException();
        };

    };

    @SubscribeEvent
    public static final void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        syncTo(event.getEntity());
    };

    @SubscribeEvent
    public static final void onPlayerChangeDimensions(PlayerChangedDimensionEvent event) {
        syncTo(event.getEntity());
    };

    @SubscribeEvent
    public static final void onLevelTick(LevelTickEvent.Post event) {
        event.getLevel().getData(DestroyAttachmentTypes.LEVEL_POLLUTION).tick(event.getLevel().getRandom());
    };
    
};
