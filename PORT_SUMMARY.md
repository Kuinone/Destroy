# Destroy 1.21.1 NeoForge Port — Summary

Community port of [Petrolpark/Destroy](https://github.com/Petrolpark-Mods/Destroy)
from Forge 1.20.1 to NeoForge 1.21.1.

- **Upstream base**: `Petrolpark-Mods/Destroy` (Forge 1.20.1)
- **Target**: NeoForge 1.21.1 + Create 6.0.10 + petrolpark library 1.4.x
- **Branch**: `1.21.1-neo`
- **Scope**: 681 Java files (≈99% of upstream's 1.20.1 code surface), 2121 resource files

---

## 1. Feature areas ported

All major Destroy subsystems are ported, organized by source package:

| Package | Description |
|---|---|
| `chemistry/` | Legacy molecule / mixture / reaction engine — periodic table, functional groups, generic reactions |
| `compat/jei/` | Full JEI integration — all RecipeCategory ports, RecipeManagerPlugin chemistry drill-down |
| `compat/curios/` | Curios slot bindings (gas mask, lab coat, etc.) |
| `compat/createbigcannons/` | CBC munition properties + shell + propellant blob effects |
| `content/oil/` | Pumpjack + seismograph + seismic survey + oil deposits |
| `content/processing/ageing/` | Ageing Barrel |
| `content/processing/centrifuge/` | Centrifuge multi-block |
| `content/processing/cooler/` | Cooler block entity |
| `content/processing/distillation/` | Distillation Tower (bubble cap + reboiler chain) |
| `content/processing/dynamo/` | Dynamo / Arc Furnace electrical conversion |
| `content/processing/extrusion/` | Extrusion Die for ingot/rod shaping |
| `content/processing/glassblowing/` | Blowpipe + Glassblowing recipe |
| `content/processing/sieve/` | Mechanical Sieve |
| `content/processing/treetap/` | Tree Tap (latex extraction) |
| `content/processing/trypolithography/` | Keypunch + Circuit Mask + Circuit Pattern Handler |
| `content/product/alcohol/` | Moonshine + Hangover + Inebriation effects |
| `content/product/firework/` | Extended-duration firework rockets |
| `content/product/fireretardant/` | Fireproofing application recipe |
| `content/product/periodictable/` | Element-displaying Periodic Table block |
| `content/redstone/programmer/` | Redstone Programmer GUI + sequencing |
| `content/sandcastle/` | Baby villager sand castle building goal + sentimental behaviour |
| `content/tool/swissarmyknife/` | Swiss Army Knife multi-tool |
| `core/chemistry/basinreaction/` | Reaction-in-Basin recipe (Create Basin chemistry) |
| `core/chemistry/hazard/` | LACRIMATOR / CHEMICAL_POISON / inhalation hazard system + Crying mob effect + tear particles |
| `core/chemistry/recipe/` | Mixture conversion + reaction + single-fluid recipes |
| `core/chemistry/storage/` | Test Tube / Beaker / Flask / Jar / Measuring Cylinder + ItemMixtureTank |
| `core/chemistry/vat/` | Vat multi-block (controller + side + UV + colorimeter + monitor + pollutometer) |
| `core/chemistry/vat/material/` | Datapack-driven vat material registry |
| `core/event/` | Common-side event subscribers (reload listeners, world load/unload, entity join, server-about-to-start) |
| `core/explosion/` | SmartExplosion + PrimedBombEntity + Mixed Explosive (custom-mix block + entity + menu + renderer) |
| `core/extendedinventory/` | Creatine-driven extra inventory slots |
| `core/fluid/` | GeniusFluidTank (mixture-aware fill merge), gas particles, tinted splash, rain |
| `core/player/` | Bladder/urinate mechanic |
| `core/pollution/` | Pollution attachment types + smog rendering + outdoor temperature + biome modifier |
| `core/registrate/` | Custom DestroyRegistrate base |
| `data/` | Datagen (block tags, item tags, recipe runtime, advancements) |
| `mixin/` | 26 mixins (vanilla + Create + JEI + farmersdelight compat) |

### Top-level registries

`DestroyBlocks`, `DestroyItems`, `DestroyFluids`, `DestroyEntityTypes`, `DestroyBlockEntityTypes`,
`DestroyMobEffects`, `DestroyPotions`, `DestroyArmorMaterials`, `DestroyDamageTypes`,
`DestroyAttachmentTypes` (NeoForge 1.21 replaces Forge Capabilities), `DestroyDataComponents`
(NeoForge 1.21 replaces ItemStack NBT), `DestroyPackets` (catnip-based `ClientboundPacketPayload`),
`DestroyPollutionTypes`, `DestroyTags`, `DestroyRecipeTypes`, `DestroyCreativeModeTabs`,
`DestroyCauldronInteractions`, `DestroyCompostables`, `DestroyVillagers`,
`DestroyVillageAddition` (inn pieces), `DestroyTrades`.

---

## 2. Major API migrations (1.20.1 → 1.21)

Applied across the codebase:

| 1.20.1 (Forge) | 1.21 (NeoForge) |
|---|---|
| `com.petrolpark.destroy.*` package | `petrolpark.mc.destroy.*` package |
| `net.minecraftforge.*` | `net.neoforged.neoforge.*` |
| `net.minecraftforge.fluids.FluidStack` | `net.neoforged.neoforge.fluids.FluidStack` |
| `new ResourceLocation("ns:path")` | `ResourceLocation.parse(...)` / `fromNamespaceAndPath(...)` |
| Forge `Serializer` | vanilla `MapCodec` + `StreamCodec` |
| `LazyOptional<IFluidHandler>` cap + `getCapability` override | static `RegisterCapabilitiesEvent` registration |
| `net.minecraftforge.event.*` | `net.neoforged.neoforge.event.*` |
| `MobEffect.applyEffectTick(LivingEntity, int) → void` | now returns `boolean` (true=keep ticking) |
| `isDurationEffectTick(int, int)` | `shouldApplyEffectTickThisTick(int, int)` |
| `addAttributeModifiers(LivingEntity, AttributeMap, int)` | drops `LivingEntity` arg — broadcast S2C packets from call site |
| `@EventBusSubscriber(bus = Bus.FORGE)` | `@EventBusSubscriber` (defaults to game bus) |
| `INBTSerializable.serializeNBT()` | `INBTSerializable.serializeNBT(HolderLookup.Provider)` |
| `MobEffectInstance(MobEffect, ...)` | `MobEffectInstance(Holder<MobEffect>, ...)` — use `.getDelegate()` |
| `Block.use(BlockState, Level, BlockPos, Player, ...)` | split into `useWithoutItem` + `useItemOn` |
| `Block` (no codec) | `Block` requires abstract `codec()` — add via `simpleCodec(...)` |
| `IClientItemExtensions.of(...)` | `RegisterClientExtensionsEvent` + `initializeClient(...)` |
| `Recipe<T>` JEI dispatch | wrap in `RecipeHolder<T>` (JEI 19.x) |
| `IEntityAdditionalSpawnData` | `IEntityWithComplexSpawn` + `RegistryFriendlyByteBuf` |
| `stack.getOrCreateTag().getCompound("X")` | `stack.get(DestroyDataComponents.X)` |

---

## 3. Runtime QA areas (major fix categories)

### 3.1 Dedicated server crashes (RuntimeDistCleaner + class-load chains)

NeoForge 1.21 has a much stricter `RuntimeDistCleaner` than Forge 1.20.1 — any reference
to `net.minecraft.client.*` from a server-loaded class causes immediate rejection.
Refactored several packet handlers, mob-effect tick paths, and JEI plugin entry points
into nested client-only `ClientHandler` static classes; moved the
`JeiProcessingRecipeMixin` from the common mixin array to the client mixin array.

### 3.2 Particle / rendering pipeline

- Distillation output temperatures quantized to 0.1 K so cross-mod tanks can stack
  Mixture FluidStacks correctly
- Crying tear particles switched from client-side `level.addParticle` to server-side
  `ServerLevel.sendParticles` broadcast for multiplayer visibility
- `TearParticle.Data` gained explicit `equals/hashCode` so vanilla `StreamCodec.unit`
  accepts new instances
- `MixedExplosiveEntityRenderer` had asymmetric `.center()` on `label` partial but
  not `base` — fixed to align both transforms

### 3.3 Datapack / loot table format migrations

`match_tool.predicate` silk-touch schema changed in 1.21 (`enchantments` array →
`predicates.minecraft:enchantments`, `enchantment` field → `enchantments`). Eight ore
loot tables and storage blocks were Python-batch-converted to the new schema.

### 3.4 Chemistry / fluid storage

- `ItemMixtureTank.fill` (for flask/cylinder/test-tube items) was extended to
  mixture-merge when the held mixture differs from the incoming one — same
  energy-averaging path used by `GeniusFluidTank` block-tank
- Creative Pump chain: `notifyMultiUpdated` propagation to adjacent pumps,
  `onSpeedChanged → updatePressureChange`, and `FluidNetwork.targets` stale-entry
  retention all corrected
- Cooler block entity captured by mechanical bearing no longer leaks "virtual air"
  (liquid air ex nihilo); virtual-state propagation tightened
- Vat-side block right-clicking a Flask + vanilla water now triggers
  `MixtureConversionRecipe` (matches pump-side behaviour)

### 3.5 Crying / sandcastle particle pipeline

`CryingMobEffect.broadcastCryingStarted` is now wired from both
`SentimentalBehaviour.onRemove` and `ChemistryHazardHelper.damage`.

### 3.6 Tree Tap (Sable mod compat)

- Stress-input crash: Sable's `BlockBreakingKineticBlockEntityMixin` hardcoded
  `BlockStateProperties.FACING` (6-axis); switched parent to
  `DirectionalKineticBlock`
- Sable's `@Redirect` replaced the parent class's `getBreakingPos()`; inlined the
  breaking loop and switched parent to plain `KineticBlockEntity`
- `neighborChanged → destroyNextTick` no longer triggers spurious progress on
  cogwheel state changes; rely on `lazyTick` for recovery
- 0→non-zero speed transition kicks `ticksUntilNextProgress` so stop+resume cycles
  don't leave the tap permanently stuck

### 3.7 World data / structure / village

- `/reload` no longer crashes in worlds without a Vat —
  `BlockIngredient.registerType(SingleBlockIngredient.TYPE)` is registered eagerly
  in `FMLCommonSetupEvent`
- `plains_inn` and `desert_inn` village pieces generate again — restored the
  missing `ServerAboutToStartEvent → DestroyVillageAddition.addBuildingToPool`
  wiring

### 3.8 JEI compat

- JEI tooltip-drift fix: missing `afterRender` companion to catnip
  `AbstractSimiWidget`'s `beforeRender/doRender/afterRender` triad caused unbalanced
  PoseStack pushes; manually paired
- JEI is now an **optional** dependency — `Mods.JEI.isLoading()` guards prevent
  `JeiProcessingRecipeMixin` from class-loading `DestroyJEI` when JEI is absent
- `destroy:cordite_rods` reappears in the creative tab — fixed registry-id drift
  from the legacy `cordite` literal in the creative-tab population code

---

## 4. Build + dependencies

- **NeoForge**: 21.1.200 dev / 21.1.219+ runtime
- **Create**: 6.0.10
- **Petrolpark library**: 1.4.31 dev (1.4.32+ recommended at runtime)
- **Catnip / Ponder**: bundled via Create
- **JEI**: 19.25.0.321 (optional)
- **Curios**: optional
- **Create Big Cannons**: optional (gated by `Mods.BIG_CANNONS.executeIfInstalled`)

See `build.gradle` + `gradle.properties` for full dependency declarations.

---

## 5. Known limitations / deferred items

- JEI Ponder integration: stub (deferred)
- Some Ponder scenes still using 1.20.1 dispatcher fall back to vanilla render

---

**Maintainer**: NHblock714
**Upstream**: [Petrolpark-Mods/Destroy](https://github.com/Petrolpark-Mods/Destroy)
**License**: All Rights Reserved (mirrors upstream)
