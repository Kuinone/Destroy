# Destroy (Kuinone Edition)

Unofficial community edition of [Petrolpark/Destroy](https://github.com/Petrolpark-Mods/Destroy)
Based on NHBlock174's 1.21.1 port, with additional modifies and pursues realistic chemistry.


> Non-official edition. Original mod by Petrolpark.

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

This is a edition that pursues realistic chemistry. Some changes made may different from the original.

## Edition relationships

Original(1.20.1 Forge, by Petrolpark) -> Port Edition(1.21.1 Neoforge, by NHBlock174)- > Kuinone Edition(1.21.1 Neoforge, by Kuinone)


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

- **[Petrolpark](https://github.com/Petrolpark-Mods)** — original mod author. 
- **[NHBlock174](https://github.com/NHblock174)** — for the 1.21.1 port that this edition is based on, and for help with this edition's development.
- **petrolpark library** — the upstream library that this port depends on for Registrate extensions, ponder helpers, ingredient infrastructure and more.
- **The Create team** — for Create itself and the conventions this mod builds on.
- **Catnip & Ponder** — for the rendering helpers and in-game scene system that Destroy uses for its guides.
- Maintainers of **Sable**, **Create: Connected**, **Create: Big Cannons**, **Curios** and **Farmer's Delight** — for the integration surfaces and for help reproducing edge cases during compat debugging.
## License

All Rights Reserved (mirrors the upstream Destroy license).

---

**Maintainer**: [Kuinone](https://github.com/Kuinone)  
**Upstream**: [NHBlock174](https://github.com/NHBlock174/Destroy) -> [Petrolpark-Mods/Destroy](https://github.com/Petrolpark-Mods/Destroy) 
