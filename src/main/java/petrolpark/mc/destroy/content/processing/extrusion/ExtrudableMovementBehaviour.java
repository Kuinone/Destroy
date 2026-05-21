package petrolpark.mc.destroy.content.processing.extrusion;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.render.SuperBufferFactory;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.DestroyBlocks;
import petrolpark.mc.destroy.core.data.advancement.DestroyAdvancementBehaviour;

/**
 * MovementBehaviour attached to every {@link BlockExtrusion}-registered Block. Watches for the
 * block being pushed through an {@link ExtrusionDieBlock} on a contraption and, when detected,
 * substitutes the contraption's local block with the extruded variant.
*/
public class ExtrudableMovementBehaviour implements MovementBehaviour {

    private final BlockExtrusion extrusion;

    public ExtrudableMovementBehaviour(BlockExtrusion extrusion) {
        this.extrusion = extrusion;
    }

    @Override
    public void onSpeedChanged(MovementContext context, Vec3 oldMotion, Vec3 motion) {
        CompoundTag data = context.data;
        if (data.getBoolean("Extruding")) {
            Direction direction = getDirection(context);
            if (!VecHelper.isVecPointingTowards(motion, direction)
                && !VecHelper.isVecPointingTowards(motion, direction.getOpposite())) {
                abandonExtrusion(context);
            }
        }
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        BlockState dieState = context.world.getBlockState(pos);

        CompoundTag data = context.data;

        if (DestroyBlocks.EXTRUSION_DIE.has(dieState) && !data.getBoolean("Extruding") && !data.getBoolean("Extruded")) {
            Axis axis = dieState.getValue(BlockStateProperties.AXIS);

            for (AxisDirection axisDirection : AxisDirection.values()) {
                Direction direction = Direction.get(axisDirection, axis);
                BlockState state = extrusion.getExtruded(context.state, direction);
                if (VecHelper.isVecPointingTowards(context.relativeMotion, direction) && !state.isAir()) {
                    data.putBoolean("Extruding", true);
                    data.putInt("ExtrusionDirection", direction.ordinal());
                    data.put("ExtrusionDiePos", NbtUtils.writeBlockPos(pos));
                    data.put("ExtrudedBlockState", NbtUtils.writeBlockState(state));
                    break;
                }
            }
        } else if (data.getBoolean("Extruding")) {
            BlockPos diePos = NbtUtils.readBlockPos(data, "ExtrusionDiePos").orElse(BlockPos.ZERO);
            Direction direction = getDirection(context);

            if (context.contraption.entity instanceof OrientedContraptionEntity oce
                && oce.getInitialYaw() != oce.yaw) direction = direction.getOpposite();

            if (pos.equals(diePos.relative(direction))) {
                context.contraption.getBlocks().put(context.localPos,
                    new StructureBlockInfo(context.localPos, getBlockState(context), null));
                if (!context.world.isClientSide()) {
                    DestroyAdvancementBehaviour advancementBehaviour =
                        BlockEntityBehaviour.get(context.world, diePos, DestroyAdvancementBehaviour.TYPE);
                    if (advancementBehaviour != null) {
                        advancementBehaviour.awardDestroyAdvancement(DestroyAdvancementTrigger.EXTRUDE);
                    }
                }
                data.putBoolean("Extruded", true);
            }

            abandonExtrusion(context);
        }
    }

    /**
 * Renders the progressively-extruded block inside a contraption. Logic:
 * <ol>
 * <li>Early out if data doesn't indicate Extruding / Extruded state</li>
 * <li>Compute progress 0..1: 0 if Extruded, else displacement along movement axis</li>
 * <li>Build a fresh {@link ExtrudedBlockModel} each frame (its geometry depends on
 * progress) — acceptable because extrusion is rare + short-lived</li>
 * <li>Convert to catnip {@link SuperByteBuffer} + render into solid buffer</li>
 * </ol>
*/
    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
                                    ContraptionMatrices matrices, MultiBufferSource buffer) {
        CompoundTag data = context.data;

        if (!data.getBoolean("Extruding") && !data.getBoolean("Extruded")) return;
        PoseStack ms = matrices.getViewProjection();
        PoseStack modelTransform = matrices.getModel();
        VertexConsumer vbSolid = buffer.getBuffer(RenderType.solid());

        Direction direction = getDirection(context);
        float progress = 0f;

        if (data.getBoolean("Extruded")) {
            progress = 0f;
        } else {
            BlockPos diePos = NbtUtils.readBlockPos(data, "ExtrusionDiePos").orElse(BlockPos.ZERO);
            Vec3 displacement = context.position.subtract(Vec3.atLowerCornerOf(diePos));
            progress = (float) direction.getAxis().choose(displacement.x(), displacement.y(), displacement.z());
            boolean invertProgress = direction.getAxisDirection() == AxisDirection.POSITIVE;
            if (context.contraption.entity instanceof OrientedContraptionEntity oce) {
                if (oce.yaw != oce.getInitialYaw()) invertProgress = !invertProgress;
            }
            if (invertProgress) progress = 1f - progress;
        }

        // Per-frame model allocation: extrusion is rare + short-lived; cleaner than cache
        ms.pushPose();
        BakedModel model = new ExtrudedBlockModel(getBlockState(context), direction, progress);
        SuperByteBuffer extrudedBlockBuffer = SuperBufferFactory.getInstance()
            .createForBlock(model, Blocks.AIR.defaultBlockState());
        if (modelTransform != null) extrudedBlockBuffer.transform(modelTransform);

        extrudedBlockBuffer.renderInto(ms, vbSolid);
        ms.popPose();
    }

    private void abandonExtrusion(MovementContext context) {
        CompoundTag data = context.data;
        data.putBoolean("Extruding", false);
        data.remove("ExtrusionDiePos");
        data.remove("ExtrusionDirection");
        // Keep ExtrudedBlockState — still needed so the extruded block can be placed on Extruded=true
    }

    private static Direction getDirection(MovementContext context) {
        return Direction.values()[context.data.getInt("ExtrusionDirection")];
    }

    @SuppressWarnings("deprecation")
    private static BlockState getBlockState(MovementContext context) {
        if (!context.data.contains("ExtrudedBlockState")) return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        return NbtUtils.readBlockState(net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(),
            context.data.getCompound("ExtrudedBlockState"));
    }
}
