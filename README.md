# Tensura Tool Belt

Lets **Traveler Tool Belt**'s belts grow with you: a belt you wear earns EP from
the mobs you kill, and once it is full it evolves into the next tier, along the
same material progression the Lightman's Currency wallets climb.

## What it does

- A plain Tool Belt becomes a Copper Tool Belt, then iron, gold, diamond, and
  finally netherite, with each tier the next one up. The belt you already have
  is the one that climbs, so there is nothing to convert and no new item to
  learn. The top tier is the end of the line.
- A belt you are wearing builds EP from the mobs you kill. When its EP is full
  the belt upgrades itself, and everything it was carrying comes with it; the
  tools stay in the belt as it moves up a tier.
- EP only goes to worn gear through **Curios**, so the belt has to be in your
  Curios belt slot while you fight. Without Curios it still works as a belt, but
  it never progresses, and the mod says so in the log at startup rather than
  leaving you guessing.
- Every tier uses **Gear Evolution**'s own material values (leather 550,
  chainmail 1350, iron 3200, gold 5000, diamond 7800, netherite 17500), so a
  belt climbs at the same pace as armor. The wallets add an emerald rung
  (5,000 → 6,400) that Traveler Tool Belt has no tier for, so the diamond belt
  spans that band instead, with no dead stretch where the belt earns EP but
  nothing happens.
- Without Gear Evolution nothing breaks. The belts craft, hold and swap exactly
  as Traveler Tool Belt ships them; they just never progress.

| Belt | Earns from EP | Upgrades into |
|---|---|---|
| Tool Belt | 0 EP | Copper Tool Belt |
| Copper Tool Belt | 550 EP | Iron Tool Belt |
| Iron Tool Belt | 1,350 EP | Gold Tool Belt |
| Gold Tool Belt | 3,200 EP | Diamond Tool Belt |
| Diamond Tool Belt | 5,000 EP | Netherite Tool Belt |
| Netherite Tool Belt | 7,800 EP | — (top tier) |

## Dependencies

| | Mod | Why |
|---|---|---|
| **Required** | [Traveler Tool Belt](https://modrinth.com/mod/traveler-tool-belt) | The belts this addon evolves |
| Optional | Gear Evolution | The belt upgrade ladder; without it the belts never progress |
| Optional | Tensura: Reincarnated | The EP the belts earn through, needed for the ladder |
| Optional | [Curios API](https://modrinth.com/mod/curios) | The only worn-gear slot Gear Evolution awards EP through; without it the belts never progress |
| Optional | ManasCore | Tensura's own library, installed alongside it |

Made for the **Tensura RPG** pack (Minecraft 1.21.1, NeoForge).

## Credits

- Built on [Traveler Tool Belt](https://modrinth.com/mod/traveler-tool-belt) by Tiviacz1337.
- Ladder and evolution by [Gear Evolution](https://www.curseforge.com/minecraft/mc-mods/gear-evolution).

## License

GNU Lesser General Public License v2.1 — see [`LICENSE`](LICENSE).
Copyright (c) 2026 Bitwise Madness.

## AI disclosure

Parts of this project were written with the assistance of AI coding tools.
AI-assisted code, data and documentation ship only after human review by the
maintainers.
