package petrolpark.mc.destroy.content.oil;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import petrolpark.mc.destroy.DestroyAttachmentTypes;

/**
 * {@code /crudeoil <position> query|set|change} — op-only command to query, set, or change the
 * {@link ChunkCrudeOil} amount in a given chunk. Wired via {@link RegisterCommandsEvent} on the
 * mod-bus (1.21 NeoForge pattern — same as {@code BabyBlueAddictionCommand} / {@code PollutionCommand}).
*/
@EventBusSubscriber
public class CrudeOilCommand {

    @SubscribeEvent
    public static final void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("crudeoil")
            .requires(cs -> cs.hasPermission(2))
            .then(Commands.argument("position", BlockPosArgument.blockPos())
                .then(Commands.literal("query")
                    .executes(CrudeOilCommand::queryCrudeOil)
                ).then(Commands.literal("set")
                    .then(Commands.argument("amount", IntegerArgumentType.integer())
                        .executes(CrudeOilCommand::setCrudeOil)
                    )
                ).then(Commands.literal("change")
                    .then(Commands.argument("amount", IntegerArgumentType.integer())
                        .executes(CrudeOilCommand::changeCrudeOil)
                    )
                )
            )
        );
    }

    private static int queryCrudeOil(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayer(); // may be null (console)
        BlockPos pos = context.getArgument("position", Coordinates.class).getBlockPos(source);
        LevelChunk chunk = source.getLevel().getChunkAt(pos);
        ChunkCrudeOil crudeOil = chunk.getData(DestroyAttachmentTypes.CHUNK_CRUDE_OIL);
        crudeOil.generate(chunk, player);
        int amount = crudeOil.getAmount();
        source.sendSuccess(() -> Component.translatable("commands.destroy.crudeoil", amount, pos.getX(), pos.getY(), pos.getZ()), true);
        return amount;
    }

    private static int setCrudeOil(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        BlockPos pos = context.getArgument("position", Coordinates.class).getBlockPos(source);
        LevelChunk chunk = source.getLevel().getChunkAt(pos);
        ChunkCrudeOil crudeOil = chunk.getData(DestroyAttachmentTypes.CHUNK_CRUDE_OIL);
        crudeOil.generate(chunk, null);
        int amount = crudeOil.setAmount(context.getArgument("amount", Integer.class));
        source.sendSuccess(() -> Component.translatable("commands.destroy.crudeoil", amount, pos.getX(), pos.getY(), pos.getZ()), true);
        return amount;
    }

    private static int changeCrudeOil(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        BlockPos pos = context.getArgument("position", Coordinates.class).getBlockPos(source);
        LevelChunk chunk = source.getLevel().getChunkAt(pos);
        ChunkCrudeOil crudeOil = chunk.getData(DestroyAttachmentTypes.CHUNK_CRUDE_OIL);
        crudeOil.generate(chunk, null);
        int amount = crudeOil.decreaseAmount(-context.getArgument("amount", Integer.class));
        source.sendSuccess(() -> Component.translatable("commands.destroy.crudeoil", amount, pos.getX(), pos.getY(), pos.getZ()), true);
        return amount;
    }
}
