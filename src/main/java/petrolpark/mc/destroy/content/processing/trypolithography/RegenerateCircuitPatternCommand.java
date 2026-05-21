package petrolpark.mc.destroy.content.processing.trypolithography;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import petrolpark.mc.destroy.Destroy;

/**
 * {@code /regeneratecircuitpattern <pattern>} — op-only command to clear a cached
 * {@link CircuitPatternHandler}-generated pattern and broadcast the updated map to all clients.
*/
@EventBusSubscriber(modid = Destroy.MOD_ID)
public class RegenerateCircuitPatternCommand {

    private static final SimpleCommandExceptionType ERROR_UNKNOWN =
        new SimpleCommandExceptionType(Component.translatable("argument.id.unknown"));

    @SubscribeEvent
    public static final void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("regeneratecircuitpattern")
            .requires(cs -> cs.hasPermission(2))
            .then(Commands.argument("pattern", ResourceLocationArgument.id())
                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                    Destroy.CIRCUIT_PATTERN_HANDLER.getPatternsWithGenerators().stream()
                        .map(ResourceLocation::toString).toList(),
                    builder))
                .executes(context -> regenerateCircuitPattern(
                    context.getSource(),
                    context.getArgument("pattern", ResourceLocation.class)))
            )
        );
    }

    public static int regenerateCircuitPattern(CommandSourceStack source, ResourceLocation pattern) throws CommandSyntaxException {
        if (!Destroy.CIRCUIT_PATTERN_HANDLER.getPatternsWithGenerators().contains(pattern)) throw ERROR_UNKNOWN.create();
        Destroy.CIRCUIT_PATTERN_HANDLER.removePattern(pattern);
        Destroy.CIRCUIT_PATTERN_HANDLER.setDirty();
        CatnipServices.NETWORK.sendToAllClients(
            new CircuitPatternsS2CPacket(Destroy.CIRCUIT_PATTERN_HANDLER.getAllPatterns()));
        source.sendSuccess(() -> Component.translatable("commands.destroy.regeneratecircuitpattern", pattern), true);
        return 1;
    }
}
