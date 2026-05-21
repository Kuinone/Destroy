package petrolpark.mc.destroy.core.chemistry.storage;

import org.joml.Vector3f;

import net.createmod.catnip.data.Couple;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import petrolpark.mc.destroy.core.chemistry.storage.SimpleMixtureTankRenderer.ISimpleMixtureTankRenderInformation;

/**
 * Item form of a SimplePlaceableMixtureTankBlock — displays fluid fill level on the held item +
 * transfers fluid to/from the placed block on break/place.
*/
public class SimplePlaceableMixtureTankBlockItem<T extends SimplePlaceableMixtureTankBlock>
    extends PlaceableMixtureTankItem<T> implements ISimpleMixtureTankRenderInformation<ItemStack> {

    public SimplePlaceableMixtureTankBlockItem(T block, Properties properties) {
        super(block, properties.stacksTo(1));
    }

    @Override
    public int getCapacity(ItemStack stack) {
        return tankBlock.getMixtureCapacity();
    }

    @Override
    public Couple<Vector3f> getFluidBoxDimensions() {
        return tankBlock.getFluidBoxDimensions();
    }

    @Override
    public float getFluidLevel(ItemStack container, float partialTicks) {
        return getContents(container).map(fs -> (float) fs.getAmount()).orElse(0f) / getCapacity(container);
    }

    @Override
    public FluidStack getRenderedFluid(ItemStack container) {
        return getContents(container).orElse(FluidStack.EMPTY);
    }
}
