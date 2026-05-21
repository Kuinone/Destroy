package petrolpark.mc.destroy;

import java.util.Optional;

import com.simibubi.create.api.contraption.BlockMovementChecks;

import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import petrolpark.mc.destroy.content.processing.dynamo.DynamoBlock;
import petrolpark.mc.destroy.core.chemistry.vat.VatControllerBlock;
import petrolpark.mc.destroy.core.chemistry.vat.VatControllerBlockEntity;
import petrolpark.mc.destroy.core.chemistry.vat.VatSideBlock;
import petrolpark.mc.destroy.core.chemistry.vat.VatSideBlockEntity;

/**
 * Registers Create {@link BlockMovementChecks} entries for Destroy-specific movement rules.
 * Two checks installed:
*/
public class DestroyMovementChecks {

    /** Resolve the {@link VatControllerBlockEntity} for a block, regardless of whether the block
 * is a Side or Controller cell. Returns empty if not part of a constructed Vat.*/
    public static Optional<VatControllerBlockEntity> getVatController(BlockState state, Level level, BlockPos pos) {
        if (state.getBlock() instanceof VatSideBlock) {
            return level.getBlockEntity(pos, DestroyBlockEntityTypes.VAT_SIDE.get())
                .map(VatSideBlockEntity::getController);
        }
        if (state.getBlock() instanceof VatControllerBlock) {
            return level.getBlockEntity(pos, DestroyBlockEntityTypes.VAT_CONTROLLER.get())
                .filter(v -> v.getVatOptional().isPresent());
        }
        return Optional.empty();
    }

    public static BlockMovementChecks.CheckResult attachedCheckVats(BlockState state, Level level,
                                                                    BlockPos pos, Direction attached) {
        return attachedCheckVats(state, level, pos, attached, false);
    }

    private static BlockMovementChecks.CheckResult attachedCheckVats(BlockState state, Level level,
                                                                     BlockPos pos, Direction attached,
                                                                     boolean cornersOnly) {
        // Faces: directly part of a Vat
        Optional<VatControllerBlockEntity> vat = getVatController(state, level, pos);
        if (vat.isPresent()) {
            return vat.get().getVatOptional().get().whereIsSideFacing(pos).getAxis() != attached.getAxis()
                && !cornersOnly
                ? BlockMovementChecks.CheckResult.SUCCESS
                : BlockMovementChecks.CheckResult.PASS;
        }

        // Edges: adjacent block is a Vat face
        BlockPos adjacentPos = pos.relative(attached);
        Optional<VatControllerBlockEntity> adjacentVat = getVatController(level.getBlockState(adjacentPos), level, adjacentPos);
        if (adjacentVat.isPresent()) {
            return adjacentVat.get().getVatOptional().get().whereIsSideFacing(adjacentPos).getAxis() != attached.getAxis()
                && !cornersOnly
                ? BlockMovementChecks.CheckResult.SUCCESS
                : BlockMovementChecks.CheckResult.PASS;
        }

        // Edges connecting to corners
        boolean sideOnAxis = false;
        boolean sideOffAxis = false;
        for (Direction face : Iterate.directions) {
            BlockPos potentialFacePos = pos.relative(face);
            boolean isVatFaceOffAxis = getVatController(level.getBlockState(potentialFacePos), level, potentialFacePos)
                .flatMap(VatControllerBlockEntity::getVatOptional)
                .map(v -> v.whereIsSideFacing(potentialFacePos))
                .map(d -> d.getAxis() != face.getAxis())
                .orElse(false);
            if (isVatFaceOffAxis) {
                if (face.getAxis() == attached.getAxis()) sideOnAxis = true;
                else sideOffAxis = true;
            }
        }
        if (sideOffAxis && !sideOnAxis) return BlockMovementChecks.CheckResult.SUCCESS;

        // Corners — recurse with cornersOnly=true to terminate
        if (!cornersOnly) {
            return attachedCheckVats(level.getBlockState(adjacentPos), level, adjacentPos,
                attached.getOpposite(), true);
        }

        return BlockMovementChecks.CheckResult.PASS;
    }

    public static void register() {
        BlockMovementChecks.registerAttachedCheck(DestroyMovementChecks::attachedCheckVats);
        BlockMovementChecks.registerMovementAllowedCheck(DynamoBlock::isMovementAllowed);
    }
}
