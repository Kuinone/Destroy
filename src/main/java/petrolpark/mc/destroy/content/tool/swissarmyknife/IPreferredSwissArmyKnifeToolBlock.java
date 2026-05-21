package petrolpark.mc.destroy.content.tool.swissarmyknife;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Marker interface: any Block that implements this tells the SwissArmyKnifeItem auto-selector
 * to skip its BlockTag-based default and use the returned Tool. Only called client-side.
*/
public interface IPreferredSwissArmyKnifeToolBlock {

    /**
 * @param level the client-side level
 * @param pos the targeted block position
 * @param state the targeted block state
 * @param shiftDown whether the player is shift-crouching (if false this is the "mine" tool,
 * if true it's the "interact" tool — e.g. axe-strip/hoe-till)
*/
    SwissArmyKnifeItem.Tool getToolForSwissArmyKnife(Level level, BlockPos pos, BlockState state, boolean shiftDown);
}
