package petrolpark.mc.destroy.core.item;

import java.util.List;

import petrolpark.mc.destroy.MoveToPetrolparkLibrary;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 1.21.1 note: {@code net.minecraft.world.item.RecordItem} was removed; music discs are now plain
 * {@link Item}s carrying a {@code DataComponents.JUKEBOX_PLAYABLE} component pointing to a
 * {@code JukeboxSong}. This class now acts as a marker base for "silent" disc-style items that
 * suppress the track-title tooltip. The actual jukebox playability must be attached via the
 * item's {@link Properties#component(net.minecraft.core.component.DataComponentType, Object)}
 * at registration time, using a registered silent {@code JukeboxSong} (see
 * {@link petrolpark.mc.destroy.DestroySoundEvents#SILENCE SILENCE sound}).
 *
 * TODO: Provide a {@code destroy:silence} JukeboxSong datagen entry and wire it
 * into every BlankRecordItem via {@code properties.jukeboxPlayable(...)}.
*/
@MoveToPetrolparkLibrary
public class BlankRecordItem extends Item {

    public BlankRecordItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // Don't add the track title
    }
}
