package petrolpark.mc.destroy.mixin.compat.create;

import java.util.ArrayList;
import java.util.Arrays;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.serialization.Codec;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.recipe.HeatCondition;

import net.minecraft.util.StringRepresentable;

/**
 *
 * <p>Also injects into {@link HeatCondition#testBlazeBurner} so that {@code COOLED} matches
 * heat level {@code FROSTING} (the synthetic value added by {@link HeatLevelMixin}, produced
 * by Destroy's Cooler block).</p>
*/
@Mixin(HeatCondition.class)
@Unique
public abstract class HeatConditionMixin {

    @Shadow
    @Final
    @Mutable
    private static HeatCondition[] $VALUES;

    
    @Shadow
    @Final
    @Mutable
    public static Codec<HeatCondition> CODEC;

    @SuppressWarnings("unused")
    private static final HeatCondition COOLED = heatConditionModifier$addValue("COOLED", 0xD9FEFF);

    @Invoker("<init>")
    public static HeatCondition heatConditionModifier$invokeInit(String internalName, int internalId, int color) {
        throw new AssertionError();
    }

    /**
 * Appends a new enum constant to {@link HeatCondition#$VALUES}. The constant's ordinal is
 * {@code lastExisting.ordinal() + 1}.
 * Doing the rebuild inline here — rather than in a separate {@code static {}} block — is
 * intentional: mixin's merging of multiple static initializers can re-order them in ways
 * that aren't strictly source-order, but a direct call inside this method is guaranteed
 * to run AFTER {@code $VALUES} is updated.
*/
    private static HeatCondition heatConditionModifier$addValue(String internalName, int color) {
        ArrayList<HeatCondition> heatConditions =
            new ArrayList<HeatCondition>(Arrays.asList(HeatConditionMixin.$VALUES));
        HeatCondition heatCondition = heatConditionModifier$invokeInit(
            internalName,
            heatConditions.get(heatConditions.size() - 1).ordinal() + 1,
            color);
        heatConditions.add(heatCondition);
        HeatConditionMixin.$VALUES = heatConditions.toArray(new HeatCondition[0]);
        // Rebuild CODEC now that $VALUES is extended. The original codec, built by
        // Create's <clinit> before this mixin's static init ran, captured a 3-element snapshot
        // (NONE/HEATED/SUPERHEATED) and could not parse "cooled".
        HeatConditionMixin.CODEC = StringRepresentable.fromEnum(HeatCondition::values);
        return heatCondition;
    }

    /**
 * Inject into {@link HeatCondition#testBlazeBurner}: when {@code this == COOLED},
 * pass iff the burner's heat level is {@code FROSTING} (Cooler block's signature
 * heat level, added via {@link HeatLevelMixin}).
*/
    @Inject(
        method = "Lcom/simibubi/create/content/processing/recipe/HeatCondition;testBlazeBurner(Lcom/simibubi/create/content/processing/burner/BlazeBurnerBlock$HeatLevel;)Z",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    public void destroy$inTestBlazeBurner(HeatLevel heatLevel, CallbackInfoReturnable<Boolean> ci) {
        HeatCondition thisHeatCondition = (HeatCondition) (Object) this;
        if (thisHeatCondition == COOLED) {
            ci.setReturnValue("FROSTING".equals(heatLevel.name()));
        }
    }
}
