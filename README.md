# Destroy (1.21.1 NeoForge)

Unofficial community port of [Petrolpark/Destroy](https://github.com/Petrolpark-Mods/Destroy)
from Forge 1.20.1 to NeoForge 1.21.1.

> Authorised non-official port. Original mod by Petrolpark.

## Requirements

| | Version |
|---|---|
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.200+ (21.1.219+ recommended at runtime) |
| Java | 21 |

## Dependencies

### Required

- **Create** — 6.0.10
- **petrolpark library** — 1.4.31+ (1.4.32+ recommended at runtime)
- **Ponder** — bundled via Create
- **Catnip** — bundled via Create
- **Registrate** — `MC1.21-1.3.0+67`
- **Flywheel** — `1.0.5`

### Optional

- **JEI** 19.25.0.321+ — recipe browser integration. **Now optional** in this port: the JEI plugin is gated by `Mods.JEI.isLoading()` and the mod runs without it.
- **Curios** — gas mask / lab coat slot bindings.
- **Create: Big Cannons** — `custom_explosive_mix` and friends usable as cannon shells with propellant-blob effects.
- **Create: Connected** — `FluidVessel` integration with Destroy's mixture-aware fluid network.
- **Farmer's Delight** — tag compatibility (`c:foods/raw_porkchop` etc.).

## What's different from 1.20.1 upstream

This is a port; gameplay behaviour is preserved 1:1 wherever the underlying API permits.
The following are the visible additions and changes.

### Extended compatibility added during the port

- **Create: Big Cannons** — `custom_explosive_mix` and the other explosive blocks now register as CBC munitions with custom propellant-blob effects.
- **Create: Connected** — the `FluidVessel*Mixin` family makes Connected's fluid vessels mixture-aware, so they cooperate with Destroy's pipe network for in-place mixture merging.
- **CircuitPatternIngredient** — datapack ingredient type registered eagerly so the Colourimeter / Pollutometer / Redstone Programmer recipes load reliably (was silently dropped in early port builds).
- **NeoForge tag namespace migration** — 21 datapack tag files migrated from the `forge:` to the `neoforge:` namespace.
- **JEI molecule drill-down** — `ChemicalSpeciesRecipeManagerPlugin` lets you look up "which recipes consume molecule X" and "which recipes produce a Mixture containing X", powered by `MixtureFluidIngredient.getReferencedMolecules()`.
- **Display Link → Pollutometer** — Display Links now correctly read from Pollutometers, emitting the selected pollution type's current level as a percentage or progress bar.

### Runtime bug fixes shipped during the port

Several of these were upstream behaviours that only surfaced under 1.21's stricter APIs (`RuntimeDistCleaner`, `StreamCodec.unit`, new `match_tool` loot schema, etc.) — they are mentioned here so testers know what behaviour to expect.

- Mixed-explosive primed entity rendering (asymmetric `SuperByteBuffer.center()` between base and label partials)
- Mixed-explosive block model `cullface: "dpwn"` typo (upstream)
- `cordite_rods` missing from the creative tab (registry-id drift from `cordite`)
- `/reload` crash in worlds without a Vat (`BlockIngredient` types now registered eagerly in `FMLCommonSetupEvent`)
- Plains / Desert Inn pieces never generating (missing `ServerAboutToStartEvent` wiring restored)
- Multiple dedicated-server class-load crashes from `RuntimeDistCleaner` (client refs moved into nested `@OnlyIn(Dist.CLIENT)` handlers)
- Distillation output temperatures drifting (~10⁻⁴ K per tick) — quantised to 0.1 K so cross-mod tanks can stack the resulting Mixture FluidStacks
- Crying tear particles invisible in multiplayer (now broadcast via `ServerLevel.sendParticles`)
- `TearParticle.Data` missing `equals` / `hashCode` (so vanilla `StreamCodec.unit` no longer drops new instances silently)
- `ItemMixtureTank.fill` not merging mixtures on Flask / Cylinder / Test Tube fill paths
- Creative Pump network refresh chain (`notifyMultiUpdated` propagation, `onSpeedChanged → updatePressureChange`, and stale `FluidNetwork.targets`)
- Cooler block entity captured by a Mechanical Bearing leaking "virtual air" (liquid air ex nihilo) — virtual-state propagation tightened
- Vat-side block right-clicking a Flask + vanilla water now triggers `MixtureConversionRecipe` (matches pump-side behaviour)
- Tree Tap × Sable mod compatibility — Sable's `@Redirect` on `getBreakingPos()` bypassed TreeTap's subclass override; parent class changed to plain `KineticBlockEntity` and the breaking loop inlined
- Tree Tap permanently stuck after a stop+resume stress cycle (0 → non-zero speed transition now kicks `ticksUntilNextProgress`)
- Six ore loot tables and storage blocks migrated to the 1.21 `match_tool.predicates.minecraft:enchantments` schema (Python-batch converted)
- JEI tooltip drift on molecule hover (catnip `AbstractSimiWidget` was missing the `afterRender` companion call, breaking PoseStack push/pop balance)
- JEI first-open freeze (Pump now reads recipes via `RecipesUpdatedEvent` instead of a constructor-time call)
- Recipe-reload caused Ponder Periodic Table page to go blank (listener re-installed on each reload)
- Pumpjack output produced flowing fluid instead of source (Registrate `.getSource()` fix)
- Blowpipe first-person glass rendering orientation

## Building

```bash
./gradlew jar
```

Produces `build/libs/destroy-1.21.1-0.2.0.jar` (about 9 MB).

> Build requires a local `_Migration-Toolkit-1.21/` directory as a sibling of
> the project root, containing the petrolpark library Maven layout and the
> compat-only jars referenced by `build.gradle`. End users who just want to
> run the mod can install the jar directly into a NeoForge instance and do
> not need the toolkit.

## Acknowledgements

- **[Petrolpark](https://github.com/Petrolpark-Mods)** — original mod author. This port is unofficial but authorised via Discord.
- **petrolpark library** — the upstream library that this port depends on for Registrate extensions, ponder helpers, ingredient infrastructure and more.
- **The Create team** — for Create itself and the conventions this mod builds on.
- **Catnip & Ponder** — for the rendering helpers and in-game scene system that Destroy uses for its guides.
- Maintainers of **Sable**, **Create: Connected**, **Create: Big Cannons**, **Curios** and **Farmer's Delight** — for the integration surfaces and for help reproducing edge cases during compat debugging.

## License

All Rights Reserved (mirrors the upstream Destroy license).

---

**Maintainer**: [NHblock714](https://github.com/NHblock714)  
**Upstream**: [Petrolpark-Mods/Destroy](https://github.com/Petrolpark-Mods/Destroy)
