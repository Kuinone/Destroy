package petrolpark.mc.destroy.compat.createbigcannons;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import petrolpark.mc.destroy.compat.createbigcannons.block.CreateBigCannonsBlocks;
import petrolpark.mc.destroy.compat.createbigcannons.block.entity.CreateBigCannonBlockEntityTypes;
import petrolpark.mc.destroy.compat.createbigcannons.entity.CreateBigCannonsEntityTypes;

/**
 * Top-level init for the CBC compat module. Called from {@link petrolpark.mc.destroy.Destroy}
 * constructor under a {@code Mods.BIG_CANNONS.executeIfInstalled} guard (so the class is never
 * classloaded when CBC is absent — avoids NoClassDefFoundError on CBC types).
*/
public class CreateBigCannons {

    public static void init(IEventBus modEventBus) {
        DestroyMunitionPropertiesHandlers.init();
        CreateBigCannonsBlocks.register();
        CreateBigCannonBlockEntityTypes.register();
        CreateBigCannonsEntityTypes.register();

        modEventBus.addListener(CreateBigCannons::onCommonSetup);
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(DestroyBlobEffects::registerBlobEffects);
    }
}
