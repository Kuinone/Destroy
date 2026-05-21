package petrolpark.mc.destroy.client;

import net.minecraft.world.item.ItemDisplayContext;

/**
 * Custom {@link ItemDisplayContext} registry for Destroy renderers. At present only
 * {@link #BLOWPIPE} is referenced — by
 * {@link petrolpark.mc.destroy.content.processing.glassblowing.BlowpipeBlockEntityRenderer}
 * when rendering the "cooling glass" result ItemStack floating on the pipe tip.
*/
public class DestroyItemDisplayContexts {

    /** BlowpipeBER now renders the cooling-glass result stack with the
 * {@code destroy:blowpipe} context, enabling per-context model transforms in the result item's
 * JSON model.
*/
    public static final ItemDisplayContext BLOWPIPE = ItemDisplayContext.valueOf("DESTROY_BLOWPIPE");

    
    public static void register() {}
}
