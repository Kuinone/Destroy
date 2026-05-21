package petrolpark.mc.destroy.core.chemistry;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.fluids.FluidStack;

import petrolpark.mc.destroy.DestroyDataComponents;
import petrolpark.mc.destroy.chemistry.legacy.ClientMixture;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.client.DestroyLang;
import petrolpark.mc.destroy.client.DestroyLang.TemperatureUnit;

/**
 * Abstract base {@link DisplaySource} for reading a FluidStack from a Block Entity and rendering
 * its mixture contents (molecules, concentrations/moles, temperature) on a Display Target
 * (e.g. Display Board).
*/
public abstract class MixtureContentsDisplaySource extends DisplaySource {

    private static DecimalFormat df = new DecimalFormat();
    static {
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);
    }

    /**
 * Whether to display Molecule quantities as a number of moles rather than a concentration.
*/
    protected final boolean molesNotConcentration;

    public MixtureContentsDisplaySource(boolean molesNotConcentration) {
        this.molesNotConcentration = molesNotConcentration;
    }

    @Override
    public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {

        List<MutableComponent> tooltip = new ArrayList<>();
        MutableComponent name;
        String temperature = "";

        FluidStack fluidStack = getFluidStack(context);
        if (fluidStack == null || fluidStack.isEmpty()) return tooltip;

        CompoundTag mixtureTag = fluidStack.getOrDefault(DestroyDataComponents.MIXTURE, new CompoundTag());
        if (mixtureTag.isEmpty()) { // If this is not a Mixture
            name = fluidStack.getHoverName().copy();
        } else { // If this is a Mixture
            boolean iupac = !context.sourceConfig().getBoolean("MoleculeNameType");
            TemperatureUnit temperatureUnit = TemperatureUnit.values()[context.sourceConfig().getInt("TemperatureUnit")];
            ReadOnlyMixture mixture = ReadOnlyMixture.readNBT(ClientMixture::new, mixtureTag);

            name = mixture.getName().copy();
            temperature = temperatureUnit.of(mixture.getTemperature());
            tooltip.addAll(mixture.getContentsTooltip(iupac, true, molesNotConcentration, fluidStack.getAmount(), df).stream().map(c -> c.copy()).toList());
        }

        if (!molesNotConcentration) name.append(" " + fluidStack.getAmount()).append(CreateLang.translateDirect("generic.unit.millibuckets"));

        tooltip.add(0, name.append(" " + temperature));

        return tooltip;
    }

    /**
 * Get the Fluid Stack which should be displayed. Subclasses provide the BE-specific tank
 * extraction via {@link DisplayLinkContext#getSourceBlockEntity}.
 * @return null/empty FluidStack if no valid source fluid (base class returns empty tooltip)
*/
    @Nullable
    public abstract FluidStack getFluidStack(DisplayLinkContext context);

    @Override
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        if (isFirstLine) {
            addTemperatureUnitSelection(builder);
        } else {
            addMoleculeNameTypeSelection(builder);
        }
    }

    private void addTemperatureUnitSelection(ModularGuiLineBuilder builder) {
        builder.addSelectionScrollInput(0, 137, (si, l) -> {
            si.forOptions(List.of(
                    DestroyLang.translate("display_source.mixture.temperature_unit.kelvin").component(),
                    DestroyLang.translate("display_source.mixture.temperature_unit.celcius").component(),
                    DestroyLang.translate("display_source.mixture.temperature_unit.farenheit").component()))
                .titled(DestroyLang.translate("display_source.mixture.temperature_unit").component());
        }, "TemperatureUnit");
    }

    public static void addMoleculeNameTypeSelection(ModularGuiLineBuilder builder) {
        builder.addSelectionScrollInput(0, 137, (si, l) -> {
            si.forOptions(List.of(
                    DestroyLang.translate("display_source.mixture.molecule_name_type.iupac").component(),
                    DestroyLang.translate("display_source.mixture.molecule_name_type.common").component()))
                .titled(DestroyLang.translate("display_source.mixture.molecule_name_type").component());
        }, "MoleculeNameType");
    }
}
