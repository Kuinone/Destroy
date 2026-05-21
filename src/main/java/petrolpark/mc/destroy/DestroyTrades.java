package petrolpark.mc.destroy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllItems;

import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

/**
 * Custom Innkeeper villager trades — 5 levels (NOVICE / APPRENTICE / JOURNEYMAN / EXPERT / MASTER)
 * for the {@link DestroyVillagers#INNKEEPER} profession.
 * 1.21 changes are limited to API boilerplate.
*/
// 1.21 NeoForge: default bus is GAME (mod-bus events take Bus.MOD);
@EventBusSubscriber(modid = Destroy.MOD_ID)
public class DestroyTrades {

    public static final List<VillagerTrades.ItemListing> INNKEEPER_NOVICE_TRADES = new ArrayList<>();
    public static final List<VillagerTrades.ItemListing> INNKEEPER_APPRENTICE_TRADES = new ArrayList<>();
    public static final List<VillagerTrades.ItemListing> INNKEEPER_JOURNEYMAN_TRADES = new ArrayList<>();
    public static final List<VillagerTrades.ItemListing> INNKEEPER_EXPERT_TRADES = new ArrayList<>();
    public static final List<VillagerTrades.ItemListing> INNKEEPER_MASTER_TRADES = new ArrayList<>();

    
    private static ItemStack waterPotion() {
        return PotionContents.createItemStack(Items.POTION, Potions.WATER);
    }

    static {
        INNKEEPER_NOVICE_TRADES.addAll(List.of(
            (trader, rand) -> new MerchantOffer(
                priceA(Items.GLASS_BOTTLE, 1), waterPotion(), 24, 2, 0.05f),
            (trader, rand) -> new MerchantOffer(
                priceA(Items.EMERALD, 1), new ItemStack(DestroyItems.APPLE_JUICE_CARTON.get(), 1), 16, 2, 0.05f)
        ));

        INNKEEPER_APPRENTICE_TRADES.addAll(List.of(
            (trader, rand) -> new MerchantOffer(
                priceA(Items.EMERALD, 2), new ItemStack(DestroyItems.UNDISTILLED_MOONSHINE_BOTTLE.get(), 1), 16, 1, 0.05f),
            (trader, rand) -> new MerchantOffer(
                priceA(Items.EMERALD, 10), new ItemStack(DestroyItems.YEAST.get(), 1), 12, 5, 0.05f)
        ));

        INNKEEPER_JOURNEYMAN_TRADES.addAll(List.of(
            (trader, rand) -> new MerchantOffer(
                priceA(Items.EMERALD, 6), new ItemStack(DestroyItems.MOONSHINE_BOTTLE.get(), 1), 16, 10, 0.05f),
            (trader, rand) -> new MerchantOffer(
                priceA(Items.EMERALD, 3), new ItemStack(AllItems.BUILDERS_TEA.get(), 1), 16, 10, 0.05f)
        ));

        // Beds — 16 colors
        for (var bed : new net.minecraft.world.item.Item[] {
            Items.BLUE_BED, Items.CYAN_BED, Items.GRAY_BED, Items.LIME_BED, Items.PINK_BED,
            Items.BLACK_BED, Items.BROWN_BED, Items.GREEN_BED, Items.WHITE_BED, Items.ORANGE_BED,
            Items.PURPLE_BED, Items.YELLOW_BED, Items.MAGENTA_BED, Items.LIGHT_BLUE_BED,
            Items.LIGHT_GRAY_BED, Items.RED_BED
        }) {
            final var bedFinal = bed;
            INNKEEPER_EXPERT_TRADES.add((trader, rand) -> new MerchantOffer(
                priceA(Items.EMERALD, 2), new ItemStack(bedFinal, 1), 12, 15, 0.05f));
        }

        INNKEEPER_MASTER_TRADES.addAll(List.of(
            // Chorus Wine — uses optional secondary price (chorus fruit). 1.21 wraps priceB in Optional<ItemCost>.
            (trader, rand) -> new MerchantOffer(
                priceA(Items.EMERALD, 15),
                Optional.of(new net.minecraft.world.item.trading.ItemCost(Items.CHORUS_FRUIT, 2)),
                new ItemStack(DestroyItems.CHORUS_WINE_BOTTLE.get(), 1), 8, 30, 0.10f),
            (trader, rand) -> new MerchantOffer(
                priceA(Items.EMERALD, 11), new ItemStack(DestroyItems.BANGERS_AND_MASH.get(), 1), 12, 30, 0.10f),
            (trader, rand) -> new MerchantOffer(
                priceA(Items.EMERALD, 25), new ItemStack(DestroyBlocks.AGING_BARREL.get(), 1), 1, 30, 0.20f)
        ));
    }

    /** 1.21 MerchantOffer takes {@code ItemCost} instead of raw ItemStack for the price.*/
    private static net.minecraft.world.item.trading.ItemCost priceA(net.minecraft.world.item.Item item, int count) {
        return new net.minecraft.world.item.trading.ItemCost(item, count);
    }

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != DestroyVillagers.INNKEEPER.get()) return;
        var trades = event.getTrades();
        trades.get(1).addAll(INNKEEPER_NOVICE_TRADES);
        trades.get(2).addAll(INNKEEPER_APPRENTICE_TRADES);
        trades.get(3).addAll(INNKEEPER_JOURNEYMAN_TRADES);
        trades.get(4).addAll(INNKEEPER_EXPERT_TRADES);
        trades.get(5).addAll(INNKEEPER_MASTER_TRADES);
    }
}
