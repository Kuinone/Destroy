package petrolpark.mc.destroy.content.processing.glassblowing;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import petrolpark.mc.destroy.DestroyBlocks;
import petrolpark.mc.destroy.DestroyDataComponents;

/**
 * Render-layer hooking into humanoid entity renderers (Players first, plus any other
 * Humanoid-modeled mobs) so that a player holding a Blowpipe and actively blowing (BLOWING
 * DataComponent == true) sees the pipe translated up to their mouth.
 *
 * <p>Registration entry-point {@link #onAddLayers} invoked by
 * {@link petrolpark.mc.destroy.core.event.DestroyClientModEvents}'s {@code @SubscribeEvent}
 * for {@link EntityRenderersEvent.AddLayers}.</p>
*/
public class BlowpipeItemRenderLayer<T extends LivingEntity, M extends EntityModel<T> & ArmedModel & HeadedModel> extends ItemInHandLayer<T, M> {

    private final ItemInHandRenderer itemRenderer;

    public BlowpipeItemRenderLayer(RenderLayerParent<T, M> renderer, ItemInHandRenderer itemInHandRenderer) {
        super(renderer, itemInHandRenderer);
        this.itemRenderer = itemInHandRenderer;
    }

    @Override
    protected void renderArmWithItem(LivingEntity livingEntity, ItemStack stack, ItemDisplayContext displayContext,
                                     HumanoidArm arm, PoseStack ms, MultiBufferSource buffer, int light) {
        if (DestroyBlocks.BLOWPIPE.isIn(stack)
            && livingEntity.swingTime == 0
            && stack.getOrDefault(DestroyDataComponents.BLOWPIPE_BLOWING, false)) {
            ms.pushPose();
            ModelPart modelpart = getParentModel().getHead();
            float f = modelpart.xRot;
            modelpart.xRot = Mth.clamp(modelpart.xRot, -Mth.PI / 6f, Mth.PI / 2f);
            modelpart.translateAndRotate(ms);
            modelpart.xRot = f;
            CustomHeadLayer.translateToHead(ms, false);

            itemRenderer.renderItem(livingEntity, stack, ItemDisplayContext.HEAD, false, ms, buffer, light);
            ms.popPose();
        }
    }

    /**
 * Registration entry-point for {@link EntityRenderersEvent.AddLayers}. Attaches a
 * {@link BlowpipeItemRenderLayer} to every player-skin renderer and every humanoid-model
 * entity renderer. Uses public event accessors;
*/
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        ItemInHandRenderer itemRenderer = event.getContext().getItemInHandRenderer();
        // Player skins (slim + classic)
        for (net.minecraft.client.resources.PlayerSkin.Model skinModel : event.getSkins()) {
            registerOn(event.getSkin(skinModel), itemRenderer);
        }
        // Non-player humanoid mobs (Zombies, Piglins, Villagers, etc.)
        for (net.minecraft.world.entity.EntityType<?> type : event.getEntityTypes()) {
            registerOn(event.getRenderer(type), itemRenderer);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerOn(EntityRenderer<?> entityRenderer, ItemInHandRenderer itemRenderer) {
        if (entityRenderer == null) return;
        if (!(entityRenderer instanceof LivingEntityRenderer<?, ?> livingRenderer)) return;
        if (!(livingRenderer.getModel() instanceof HumanoidModel)) return;
        BlowpipeItemRenderLayer<?, ?> layer = new BlowpipeItemRenderLayer(livingRenderer, itemRenderer);
        livingRenderer.addLayer((BlowpipeItemRenderLayer) layer);
    }
}
