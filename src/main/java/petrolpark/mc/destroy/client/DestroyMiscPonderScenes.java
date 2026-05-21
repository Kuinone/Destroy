package petrolpark.mc.destroy.client;

import com.simibubi.create.AllItems;

import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import com.simibubi.create.content.redstone.link.RedstoneLinkBlock;

import petrolpark.mc.destroy.DestroyBlocks;

/**
 * Deps: BlacklightBlock (S186 ported) but the scene itself only uses generic
 * showSection / showOutline / showText — no cross-file coupling.</li>
 * <li>{@link #redstoneProgrammer} — <b>real</b>, wired on {@link DestroyBlocks#REDSTONE_PROGRAMMER}
 * in {@link DestroyPonderScenes#register}. Deps:
 * basic Ponder API only (basin + burner show-sections + text overlay). <b>Note:</b> scene
 * title "reactions" matches existing {@code ChemistryPonderScenes::reactions} on BASIN —
 * two separate storyboards sharing a title, Ponder distinguishes by registered component.</li>
 * <li>{@link #vatInteraction} — <b>stub</b>, T2a defer. Depends on full Vat subsystem
 * ({@link petrolpark.mc.destroy.core.chemistry.vat.VatSideBlockEntity#setDisplayType} +
 * {@code DisplayType.THERMOMETER/BAROMETER/PIPE/OPEN_VENT/CLOSED_VENT}). Vat stub is
 * partial (S178), but the interaction scene requires the full DisplayType enum-driven
 * side-block state machine, which needs VatSideBlockEntity full port + VatRenderer +
 * VatScreen to render meaningfully. Stub preserves registration API contract for
 * future T2a-closer.</li>
 * </ul>
 *
 * <p><b>Registration site</b> (DestroyPonderScenes.register):</p>
 * <ul>
 * <li>{@code uv} — NOT registered.</li>
 * <li>{@code redstoneProgrammer} — REDSTONE_PROGRAMMER block</li>
 * <li>{@code reactions} — MECHANICAL_MIXER block (with DestroyPonderTags.CHEMISTRY)</li>
 * <li>{@code vatInteraction} — BLAZE_BURNER block (deferred; stub behavior: scene.markAsFinished)</li>
 * </ul>
 *
 * <p><b>Rule applications</b> (zero new rule — pure pattern reuse):</p>
*/
public class DestroyMiscPonderScenes {

    /**
 * Vat interaction scene — <b>T2a STUB</b>. Plus fluid pipe placement + funnel
 * interaction + platinum ingot item drop + blaze burner/cooler substitution.
 *
 * <p>Blockers for real port:</p>
 * <ul>
 * <li>{@code VatSideBlockEntity.setDisplayType(DisplayType)} is stub (S178) — would need
 * full state-machine impl + rendering wire.</li>
 * <li>VatControllerBlockEntity stubs (S176/S179/S189 addFluid/flush/pressure) would not
 * visualize meaningfully in Ponder even if scene lambda compiled.</li>
 * <li>VatRenderer + VatSideRenderer absent (S200 roadmap T2a §1.11).</li>
 * </ul>
*/
    public static void vatInteraction(SceneBuilder builder, SceneBuildingUtil util) {
        // S201 stub — see class javadoc for T2a blocker list. Minimal scene: title + finished
        // marker so the storyboard registration is non-null and UI doesn't error-out.
        builder.title("vat_interaction", "This text is defined in a language file.");
        builder.configureBasePlate(0, 0, 6);
        builder.scaleSceneView(0.8f);
        builder.showBasePlate();
        builder.idle(10);
        builder.markAsFinished();
    }

    /**
 * Reactions intro scene — 1:1
 * Shows a basin-and-burner setup with three text overlays explaining reaction fundamentals.
 * Self-contained (no custom block dependencies beyond vanilla Ponder schematic for
 * mechanical_mixer). Wired on {@code AllBlocks.MECHANICAL_MIXER} with
 * {@code DestroyPonderTags.CHEMISTRY} tag.
*/
    public static void reactions(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("reactions", "This text is defined in a language file.");
        scene.configureBasePlate(0, 0, 9);
        scene.scaleSceneView(.5f);
        scene.showBasePlate();

        scene.world().showSection(util.select().everywhere().substract(util.select().position(2, 1, 2)).substract(util.select().fromTo(0, 0, 0, 8, 0, 8)), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(100)
            .text("This text is defined in a language file.")
            .pointAt(util.vector().blockSurface(util.grid().at(4, 4, 5), Direction.UP));
        scene.idle(120);

        scene.overlay().showText(100)
            .text("This text is defined in a language file.")
            .attachKeyFrame();
        scene.idle(120);

        BlockPos underBasin = util.grid().at(1, 1, 2);

        scene.overlay().showText(100)
            .text("This text is defined in a language file.")
            .pointAt(util.vector().blockSurface(underBasin, Direction.WEST));
        scene.idle(20);
        scene.world().hideSection(util.select().position(underBasin), Direction.WEST);
        scene.idle(20);
        ElementLink<WorldSectionElement> burner = scene.world().showIndependentSection(util.select().position(2, 1, 2), Direction.WEST);
        scene.world().moveSection(burner, util.vector().of(-1d, 0d, 0d), 0);
        scene.idle(80);

        scene.overlay().showText(100)
            .text("This text is defined in a language file.")
            .attachKeyFrame();
        scene.idle(120);

        scene.markAsFinished();
    }

    /**
 * UV / Blacklight scene — 1:1 Shows a
 * vat top (glass panel) + side blacklight, two text overlays pointing at relevant
 * schematic positions. Useful as dev-time
 * reference and preserves future revival path.
 *
 * <p>Deps: BlacklightBlock (S186 ported) — but scene uses only generic Ponder
 * showSection/showOutline/showText; the block is referenced only by schematic-side,
 * not by Java code in the storyboard.</p>
*/
    public static void uv(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("uv", "This text is defined in a language file.");
        scene.configureBasePlate(0, 0, 6);
        scene.scaleSceneView(0.8f);
        scene.showBasePlate();

        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1, 1, 1, 4, 4, 4), Direction.DOWN);
        scene.overlay().showText(60)
            .text("This text is defined in a language file.");
        scene.idle(80);

        scene.idle(10);
        scene.overlay().showOutline(PonderPalette.WHITE, "top_glass", util.select().fromTo(2, 4, 2, 3, 4, 3), 80);
        scene.overlay().showText(80)
            .text("This text is defined in a language file.")
            .pointAt(util.vector().of(3, 5, 3))
            .attachKeyFrame();
        scene.idle(100);

        scene.world().showSection(util.select().fromTo(0, 2, 2, 0, 3, 3), Direction.EAST);
        scene.idle(10);
        scene.overlay().showText(80)
            .text("This text is defined in a language file.")
            .pointAt(util.vector().blockSurface(util.grid().at(0, 3, 2), Direction.WEST))
            .attachKeyFrame();
        scene.idle(100);

        scene.markAsFinished();
    }

    /**
 * Redstone Programmer scene — 1:1 Demonstrates redstone-link receiver setup (3 blocks wrench-toggled),
 * right-click placement of REDSTONE_PROGRAMMER on middle link, stand-alone sneak-placement,
 * and oscillating redstone power demo (5×3 toggles). All dependencies 100% ready:
 *
 * <ul>
 * <li>{@link DestroyBlocks#REDSTONE_PROGRAMMER} — S117 ported (registered with PROGRAMMER_UUID
 * + PROGRAMMER_PROGRAM DataComponents).</li>
 * <li>{@code RedstoneLinkBlock.RECEIVER} — Create path preserved
 * ({@code com.simibubi.create.content.redstone.link}).</li>
 * <li>{@link AllItems#WRENCH} — Create vanilla preserved.</li>
 * </ul>
 *
 * <p>Wired on {@code DestroyBlocks.REDSTONE_PROGRAMMER} in
 * {@link DestroyPonderScenes#register}. Plays during right-click on REDSTONE_PROGRAMMER
 * ItemStack in JEI/hand.</p>
*/
    public static void redstoneProgrammer(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("redstone_programmer", "This text is defined in a language file.");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        scene.idle(10);
        for (int i = 0; i < 9; i++) {
            scene.world().showSection(util.select().position(1 + i % 3, 1 + i / 3, 3), Direction.DOWN);
            scene.idle(5);
        }

        for (int i = 0; i < 3; i++) {
            BlockPos pos = util.grid().at(1 + i, 3, 3);
            scene.overlay().showControls(util.vector().blockSurface(pos, Direction.SOUTH)
                .add(0, 0, -3 / 16f), net.createmod.catnip.math.Pointing.DOWN, 10)
                .rightClick()
                .withItem(AllItems.WRENCH.asStack());
            scene.world().modifyBlock(pos, s -> s.cycle(RedstoneLinkBlock.RECEIVER), true);
            scene.idle(10);
        }

        scene.idle(20);
        Vec3 linkVec = util.vector().blockSurface(util.grid().at(2, 3, 3), Direction.SOUTH).add(0, 0, -3 / 16f);
        scene.overlay().showControls(linkVec, net.createmod.catnip.math.Pointing.DOWN, 80).rightClick().withItem(DestroyBlocks.REDSTONE_PROGRAMMER.asStack());
        scene.overlay().showText(80)
            .text("This text is defined in a language file.")
            .pointAt(util.vector().blockSurface(util.grid().at(2, 2, 3), Direction.UP))
            .attachKeyFrame();
        scene.idle(100);

        scene.overlay().showText(80)
            .text("This text is defined in a language file.");
        scene.idle(100);

        Vec3 placementPos = util.vector().blockSurface(util.grid().at(2, 0, 1), Direction.UP);
        scene.overlay().showText(100)
            .text("This text is defined in a language file.")
            .pointAt(placementPos)
            .attachKeyFrame();
        scene.idle(20);
        scene.overlay().showControls(placementPos, net.createmod.catnip.math.Pointing.DOWN, 40).rightClick().whileSneaking().withItem(DestroyBlocks.REDSTONE_PROGRAMMER.asStack());
        scene.idle(40);
        scene.world().showSection(util.select().position(2, 1, 1), Direction.DOWN);
        scene.idle(80);

        scene.overlay().showText(100)
            .text("This text is defined in a language file.")
            .attachKeyFrame();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 3; j++) {
                net.createmod.ponder.api.scene.Selection selection = util.select().fromTo(1 + j, 1, 3, 1 + j, 3, 3);
                scene.world().toggleRedstonePower(selection);
                scene.idle(10);
            }
        }
        scene.markAsFinished();
    }
}
