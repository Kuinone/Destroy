package petrolpark.mc.destroy.content.oil.seismology;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

import petrolpark.mc.destroy.Destroy;

/** Without this handler, the BEWLR-registered
 * {@link SeismographItemRenderer} <strong>never runs in first-person view</strong> because
 * vanilla {@code ItemInHandRenderer.renderArmWithItem} dispatches on
 * {@code item instanceof MapItem} <em>before</em> falling through to the BEWLR / custom-renderer
 * path — and our SeismographItem extends MapItem (necessary for cartography table integration,
 * server-side map-tile saved-data lookup, etc.). player has to right-click to open the Screen to see the nonogram.
 *
 * <p>Fix: subscribe to NeoForge {@link RenderHandEvent}, which fires <em>before</em> vanilla's
 * {@code renderArmWithItem} dispatch decision and is cancelable. When the held stack is a
 * SeismographItem, take over and call {@link SeismographItemRenderer#renderOneHandedSeismograph}
 * (which itself calls {@code renderPlayerArm} so we don't lose the arm rendering), then cancel.
 * Vanilla's map dispatch never gets to run.</p>
 *
 * <p><b>Why this isn't in {@link SeismographItemRenderer}</b>: BEWLR ({@code renderByItem}) is
 * called as part of the {@code renderArmWithItem} dispatch when the dispatcher decides "no special
 * vanilla path applies"; for MapItem subclasses, the special vanilla path applies and BEWLR is
 * skipped. {@link RenderHandEvent} fires earlier in the pipeline — before the dispatch decision —
 * which is the only injection point that can preempt MapItem's special-case render path without a
 * mixin into {@code ItemInHandRenderer}.</p>
*/
@EventBusSubscriber(modid = Destroy.MOD_ID, value = Dist.CLIENT)
public class SeismographHandRenderHandler {

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof SeismographItem)) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        ItemInHandRenderer handItemRenderer = mc.getEntityRenderDispatcher().getItemInHandRenderer();

        // Map InteractionHand → HumanoidArm. Mirrors the player's main-hand setting (left/right
        // handedness): MAIN_HAND uses the player's main arm, OFF_HAND uses the opposite. This is
        // the same logic vanilla ItemInHandRenderer applies when picking which side to render.
        InteractionHand hand = event.getHand();
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND
            ? player.getMainArm()
            : player.getMainArm().getOpposite();

        SeismographItemRenderer.renderOneHandedSeismograph(
            event.getPoseStack(),
            event.getMultiBufferSource(),
            event.getPackedLight(),
            event.getEquipProgress(),
            arm,
            event.getSwingProgress(),
            stack,
            mc,
            handItemRenderer
        );
        event.setCanceled(true);
    }
}
