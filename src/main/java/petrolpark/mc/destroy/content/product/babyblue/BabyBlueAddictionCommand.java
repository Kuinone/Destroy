package petrolpark.mc.destroy.content.product.babyblue;

import java.util.Collection;

import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import petrolpark.mc.destroy.DestroyAttachmentTypes;

/**
 * {@code /babyblueaddiction query|set <targets> [amount]} — op-only command to query/set a
 * player's baby blue addiction counter. Wired via {@link RegisterCommandsEvent} on the mod-bus
 * (1.21 NeoForge pattern, see PollutionCommand for the template).
*/
@EventBusSubscriber
public class BabyBlueAddictionCommand {

    @SubscribeEvent
    public static final void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("babyblueaddiction")
            .requires(cs -> cs.hasPermission(2))
            .then(Commands.literal("query").then(Commands.argument("targets", EntityArgument.player()).executes(context -> {
                return queryBabyBlueAddiction(context.getSource(), EntityArgument.getPlayer(context, "targets"));
            })))
            .then(Commands.literal("set").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes(context -> {
                return setBabyBlueAddiction(context.getSource(), EntityArgument.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "amount"));
            }))))
        );
    }

    private static int queryBabyBlueAddiction(CommandSourceStack source, ServerPlayer player) {
        PlayerBabyBlueAddictionAttachment addiction =
            player.getData(DestroyAttachmentTypes.PLAYER_BABY_BLUE_ADDICTION);
        int addictionLevel = addiction.getBabyBlueAddiction();
        source.sendSuccess(() -> Component.translatable("commands.destroy.babyblueaddiction.query",
            player.getDisplayName(), addictionLevel), true);
        return addictionLevel;
    }

    /** Returns the number of players for whom the baby blue addiction was set.*/
    private static int setBabyBlueAddiction(CommandSourceStack source,
                                            Collection<? extends ServerPlayer> players, int amount) {
        for (ServerPlayer player : players) {
            PlayerBabyBlueAddictionAttachment addiction =
                player.getData(DestroyAttachmentTypes.PLAYER_BABY_BLUE_ADDICTION);
            addiction.setBabyBlueAddiction(amount);
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.destroy.babyblueaddiction.set.single",
                amount, players.iterator().next().getDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.destroy.babyblueaddiction.set.multiple",
                amount, players.size()), true);
        }
        return players.size();
    }
}
