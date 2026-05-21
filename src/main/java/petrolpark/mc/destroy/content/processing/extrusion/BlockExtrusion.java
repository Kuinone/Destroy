package petrolpark.mc.destroy.content.processing.extrusion;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * "Recipe" lookup for the ExtrusionDieBlock: maps source Block → function producing the extruded
 * BlockState in a given direction. Registered via {@link #register(Block, BlockExtrusion)} which
 * attaches an {@link ExtrudableMovementBehaviour} to the Block in Create's contraption-movement
 * registry.
*/
@FunctionalInterface
public interface BlockExtrusion {

    BiMap<Block, BlockExtrusion> EXTRUSIONS = HashBiMap.create();

    /**
 * Get the Block State a Block will produce when pushed through an Extrusion Die in the given
 * Direction.
 * @return the corresponding extruded BlockState, or an air BlockState if no extrusion possible
*/
    BlockState getExtruded(BlockState state, Direction extrusionDirection);

    /** Register a Block Extrusion "recipe". Must be called after all Blocks are initialized.*/
    static void register(Block block, BlockExtrusion extrusion) {
        MovementBehaviour.REGISTRY.register(block, new ExtrudableMovementBehaviour(extrusion));
        EXTRUSIONS.put(block, extrusion);
    }
}
