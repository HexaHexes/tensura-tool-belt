# Tensura Tool Belt

Lets **Traveler Tool Belt**'s belts grow with you: a belt you wear earns EP from the
mobs you kill, and once it is full it evolves into the next tier — the same ladder the
Lightman's Currency wallets climb.

## What it does

- **The belts climb a ladder.** A plain Tool Belt becomes a Copper Tool Belt, then
  iron, gold, diamond, and finally netherite. Each tier is the next one up, and the
  top tier is the end of the line.
- **EP is earned by playing.** A belt you are wearing builds EP from the mobs you
  kill. When its EP is full the belt upgrades itself, and everything it was carrying
  comes with it — the tools stay in the belt as it moves up a tier.
- **Wear it, do not carry it.** EP is awarded to worn gear through Curios, so the belt
  has to be in your Curios belt slot while you fight. Without Curios it still works as
  a belt, but it never progresses - and the mod says so in the log at startup rather
  than leaving you guessing.
- **Same ladder as the wallets.** Every tier uses the Lightman's Currency wallet
  ladder's own numbers. The wallets have an emerald rung and Traveler Tool Belt has no
  emerald tier, so the gold belt climbs through that band and hands off to the diamond
  belt — no dead stretch where the belt earns EP but nothing happens.
- **Without Gear Evolution nothing breaks.** The belts craft, hold and swap exactly as
  Traveler Tool Belt ships them; they just never progress.

| Belt | Earns from EP | Upgrades into |
|---|---|---|
| Tool Belt | 0 EP | Copper Tool Belt |
| Copper Tool Belt | 1,000 EP | Iron Tool Belt |
| Iron Tool Belt | 2,000 EP | Gold Tool Belt |
| Gold Tool Belt | 4,000 EP | Diamond Tool Belt |
| Diamond Tool Belt | 16,000 EP | Netherite Tool Belt |
| Netherite Tool Belt | 32,000 EP | — (top tier) |

## Getting it in-game

Install it next to Traveler Tool Belt, Tensura: Reincarnated and Gear Evolution, then
play as usual. Craft and wear your belt the way Traveler Tool Belt already works — the
same belt you are already carrying is the one that climbs, so there is nothing to
convert and no new item to learn. Wear it in your belt slot (Curios) and fight; the
belt fills up, then steps up on its own.

## Dependencies

| | Mod | Why |
|---|---|---|
| **Required** | [Traveler Tool Belt](https://modrinth.com/mod/traveler-tool-belt) | The belts this addon evolves |
| Optional | Gear Evolution | The belt upgrade ladder — without it the belts never progress |
| Optional | Tensura: Reincarnated | The EP the belts earn through, needed for the ladder |
| Optional | [Curios API](https://modrinth.com/mod/curios) | The only worn-gear slot Gear Evolution awards EP through - without it the belts never progress |
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
