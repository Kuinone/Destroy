package petrolpark.mc.destroy.compat.createbigcannons.entity;

import com.tterrag.registrate.util.entry.EntityEntry;

import net.minecraft.world.entity.MobCategory;
import rbasamoyai.createbigcannons.index.CBCMunitionPropertiesHandlers;
import rbasamoyai.createbigcannons.multiloader.EntityTypeConfigurator;
import rbasamoyai.createbigcannons.munitions.config.MunitionPropertiesHandler;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.compat.createbigcannons.entity.renderer.CustomExplosiveMixShellProjectileRenderer;

/**
 * Register the {@link CustomExplosiveMixShellProjectile} entity type and bind CBC's
 * common-shell property handler so the shell inherits ballistics + damage + fuze from CBC config.
*/
public class CreateBigCannonsEntityTypes {

    public static final EntityEntry<CustomExplosiveMixShellProjectile> CUSTOM_EXPLOSIVE_MIX_SHELL =
        Destroy.REGISTRATE
            .entity("custom_explosive_mix_shell", CustomExplosiveMixShellProjectile::new, MobCategory.MISC)
            .properties(b -> EntityTypeConfigurator.of(b)
                .size(0.2f, 0.2f)
                .fireImmune()
                .updateInterval(1)
                .updateVelocity(false)
                .trackingRange(16))
            .renderer(() -> CustomExplosiveMixShellProjectileRenderer::new)
            .onRegister(type -> MunitionPropertiesHandler.registerProjectileHandler(type, CBCMunitionPropertiesHandlers.COMMON_SHELL_BIG_CANNON_PROJECTILE))
            .register();

    public static void register() {
        // Class-load trigger.
    }
}
