package petrolpark.mc.destroy.content.product.alcohol;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyDamageSources;
import petrolpark.mc.destroy.DestroyMobEffects;
import petrolpark.mc.destroy.MoveToPetrolparkLibrary;
import petrolpark.mc.destroy.config.DestroyConfigs;
import petrolpark.mc.destroy.core.mobeffect.DestroyMobEffect;

/**
 * Hangover mob effect — applies a -10% movement-speed attribute modifier and damages afflicted
 * entities when they hear loud noises (configurable per-SoundSource threshold). Applied when the
 * player wakes up with an active Inebriation effect (see {@link InebriationMobEffect#onSleepFinished}).
*/
@MoveToPetrolparkLibrary
@EventBusSubscriber(modid = Destroy.MOD_ID)
public class HangoverMobEffect extends DestroyMobEffect {

    private static final ResourceLocation MOVEMENT_SPEED_ID =
        ResourceLocation.fromNamespaceAndPath(Destroy.MOD_ID, "effect.hangover.movement_speed");

    public HangoverMobEffect(MobEffectCategory category, int color) {
        super(category, color);
        Holder<net.minecraft.world.entity.ai.attributes.Attribute> speed = Attributes.MOVEMENT_SPEED;
        this.addAttributeModifier(speed, MOVEMENT_SPEED_ID, -0.10d, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @SubscribeEvent
    public static void onPlayerHearsSound(PlayLevelSoundEvent.AtPosition event) {
        if (event.getOriginalVolume() < DestroyConfigs.server().substances.soundSourceThresholds.get(event.getSource()).getF()) return;
        Vec3 pos = event.getPosition();
        float radius = DestroyConfigs.server().substances.hangoverNoiseTriggerRadius.getF();
        List<Entity> nearbyEntities = event.getLevel().getEntities(null,
            new AABB(pos.add(new Vec3(-radius, -radius, -radius)), pos.add(new Vec3(radius, radius, radius))));
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity livingEntity) {
                if (livingEntity.hasEffect(DestroyMobEffects.HANGOVER.getDelegate())) {
                    livingEntity.hurt(DestroyDamageSources.headache(livingEntity.level()),
                        DestroyConfigs.server().substances.soundSourceDamage.get(event.getSource()).getF());
                }
            }
        }
    }
}
