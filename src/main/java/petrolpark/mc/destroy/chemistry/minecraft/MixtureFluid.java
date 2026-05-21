package petrolpark.mc.destroy.chemistry.minecraft;

import javax.annotation.Nullable;

import com.simibubi.create.AllFluids.TintedFluidType;
import com.simibubi.create.content.fluids.VirtualFluid;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import petrolpark.mc.destroy.DestroyDataComponents;
import petrolpark.mc.destroy.DestroyFluids;
import petrolpark.mc.destroy.chemistry.legacy.ClientMixture;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMixture;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;

import static petrolpark.mc.destroy.chemistry.legacy.LegacyReaction.GAS_CONSTANT;

/**
 * The Fluid backing a {@link LegacyMixture} payload in-world. A Mixture rides on a FluidStack via the
 * {@link DestroyDataComponents#MIXTURE} DataComponent (1.21 NeoForge replacement for the old NBT
 * sub-tag pattern). Colour + tooltip name come from the ClientMixture computed off the attached
 * CompoundTag.
*/
public class MixtureFluid extends VirtualFluid {

    public MixtureFluid(Properties properties, boolean source) {
        super(properties, source);
    }

    /**
 * Synthesize an "atmosphere" Mixture — N₂ + O₂ at the given temperature, mole fractions
 * 0.78084 + 0.21916 respectively (standard dry-air composition). Total pressure assumed 101 kPa.
*/
    public static LegacyMixture airMixture(float temperature) {
        if (temperature <= 0f || Float.isNaN(temperature))
            throw new IllegalStateException("Temperature cannot be negative or 0.");
        LegacyMixture air = new LegacyMixture();
        air.addMolecule(DestroyMolecules.NITROGEN, 101000f / 1000f / (float) GAS_CONSTANT / temperature * 0.78084f);
        air.addMolecule(DestroyMolecules.OXYGEN,   101000f / 1000f / (float) GAS_CONSTANT / temperature * 0.21916f);
        air.setTemperature(temperature);
        return air;
    }

    /**
 * Creates a Fluid Stack of the given Mixture (no custom translation key).
 * @param amount how many mB this Fluid Stack is
*/
    public static FluidStack of(int amount, ReadOnlyMixture mixture) {
        return of(amount, mixture, null);
    }

    /**
 * Converts a liquid-phase Mixture FluidStack to its gas-phase equivalent, preserving all payload.
*/
    public static FluidStack gasOf(FluidStack stack) {
        if (!DestroyFluids.isMixture(stack)) return FluidStack.EMPTY;
        ReadOnlyMixture mixture = ReadOnlyMixture.readNBT(ReadOnlyMixture::new,
            stack.getOrDefault(DestroyDataComponents.MIXTURE, new CompoundTag()));
        FluidStack gasStack = new FluidStack(DestroyFluids.GAS_MIXTURE.get(), stack.getAmount());
        addMixtureToFluidStack(gasStack, mixture);
        return gasStack;
    }

    /**
 * Creates a Fluid Stack of the given Mixture with an optional custom translation key override.
 * @param amount how many mB this Fluid Stack is
 * @param mixture may be read-only or mutable
 * @param translationKey custom name key, {@code null} or {@code ""} for algorithmic naming
*/
    public static FluidStack of(int amount, ReadOnlyMixture mixture, @Nullable String translationKey) {
        if (amount == 0) return FluidStack.EMPTY;
        FluidStack fluidStack = new FluidStack(DestroyFluids.MIXTURE.get().getSource(), amount);
        if (translationKey != null) mixture.setTranslationKey(translationKey);
        addMixtureToFluidStack(fluidStack, mixture);
        return fluidStack;
    }

    public static MixtureFluid createSource(Properties properties) {
        return new MixtureFluid(properties, true);
    }

    public static MixtureFluid createFlowing(Properties properties) {
        return new MixtureFluid(properties, false);
    }

    /**
 * Attaches the Mixture's NBT payload to the FluidStack via the {@link DestroyDataComponents#MIXTURE}
 * DataComponent.
*/
    public static FluidStack addMixtureToFluidStack(FluidStack fluidStack, ReadOnlyMixture mixture) {
        if (mixture.isEmpty()) {
            fluidStack.remove(DestroyDataComponents.MIXTURE);
            return fluidStack;
        }
        fluidStack.set(DestroyDataComponents.MIXTURE, mixture.writeNBT());
        return fluidStack;
    }

    public static class MixtureFluidType extends TintedFluidType {

        public MixtureFluidType(FluidType.Properties properties,
                                ResourceLocation stillTexture, ResourceLocation flowingTexture) {
            super(properties, stillTexture, flowingTexture);
        }

        @Override
        protected int getTintColor(FluidStack stack) {
            return MixtureFluid.getTintColor(stack);
        }

        @Override
        protected int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
            // Mixture Fluids are virtual — they should never exist as a FluidState, so no tint needed.
            return 0;
        }

        @Override
        public Component getDescription(FluidStack stack) {
            return ReadOnlyMixture.readNBT(ClientMixture::new,
                stack.getOrDefault(DestroyDataComponents.MIXTURE, new CompoundTag())).getName();
        }
    }

    /**
 * Tint colour the Fluid texture renders with in JEI / goggles / tank. Empty stack → transparent,
 * stack without a Mixture payload → -1 (unrecognized), else the ClientMixture's computed colour.
*/
    public static int getTintColor(FluidStack stack) {
        if (stack.isEmpty()) return 0x00FFFFFF;
        if (!stack.has(DestroyDataComponents.MIXTURE)) return -1;
        return ReadOnlyMixture.readNBT(ClientMixture::new, stack.get(DestroyDataComponents.MIXTURE)).getColor();
    }
}
