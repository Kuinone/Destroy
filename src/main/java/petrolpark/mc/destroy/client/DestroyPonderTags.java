package petrolpark.mc.destroy.client;

import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyItems;

/**
 * Registrar for Destroy's Ponder tags. Ponder "tags" are category groupings visible in the
 * Ponder UI tag index (e.g. the "Chemistry" tab shows all Chemistry-tagged blocks). 1.21 skeleton ports only the 3 top-level tag
 * registrations; block-to-tag additions defer to scene-batch sessions (S159+) since they
 * reference blocks/items spread across several subdirs.
*/
public class DestroyPonderTags {

    public static final ResourceLocation
        CHEMISTRY       = Destroy.asResource("chemistry"),
        DESTROY         = Destroy.asResource("destroy"),
        VAT_SIDE_BLOCKS = Destroy.asResource("vat_side_blocks");

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
        // Top-level tag registrations — 3 tabs in the Ponder index. TEST_TUBE + VAT_CONTROLLER
        // items not yet ported; S158 falls back to LOGO as the icon for all 3 until upstream
        // lands (cosmetic-only, shown in Ponder tag index).
        helper.registerTag(CHEMISTRY)
            .addToIndex()
            .item(DestroyItems.LOGO)  // TODO(S159+): TEST_TUBE when ported
            .register();

        helper.registerTag(DESTROY)
            .addToIndex()
            .item(DestroyItems.LOGO)
            .register();

        helper.registerTag(VAT_SIDE_BLOCKS)
            .addToIndex()
            .item(DestroyItems.LOGO)  // TODO(S159+): VAT_CONTROLLER (block) when Vat subdir 后续 port
            .register();

        // S159+ 起: HELPER.addToTag(CHEMISTRY).add(AllBlocks.BASIN).add(BUBBLE_CAP).etc
        // 依赖 block/item registration 到位之后逐个追加.
    }
}
