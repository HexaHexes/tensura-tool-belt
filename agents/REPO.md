# Repo rules — Tensura Tool Belt

Repo-specific rules and notes. The org-wide `AGENTS.md` beside this file is maintained
in [`neoforge-mod-template`](https://github.com/HexaHexes/neoforge-mod-template) and
pulled in verbatim - **never hand-edit it.** Everything in this file is this repo's own
and is edited freely; keep it aligned with the org rules rather than restating them.

**Repo:** `tensura-tool-belt` · **Mod ID:** `tensura_tool_belt` · **Branch:** `main`
**Platform:** Minecraft 1.21.1 / NeoForge 21.1.259 / JDK 21 · **Jar:** `build/libs/tensura_tool_belt-<version>.jar`

Registers Traveler Tool Belt's six belts with Tensura: Gear Evolution's `addon_chains/`
so they earn EP and evolve tier to tier, on Gear Evolution's canonical material
progression - the same values the Lightman's Currency wallets climb in
`lightmans-tensura`. Pure data: no code beyond the mod entry point, no
mixins, nothing packaged that runs.

**Dependencies**
- Required: Traveler Tool Belt (`travelertoolbelt`) - the belts being evolved.
- Optional: Tensura: Gear Evolution (`gearevolution`) - loads and drives the chains;
  it requires Tensura (`tensura`) and ManasCore (`manascore`). Without it the chains
  are inert and the belts never progress.

**Remote:** `github.com/HexaHexes/tensura-tool-belt` (default branch `main`).

**Test:** `./gradlew build` (compiles + the JUnit suite: `BeltLadderTest`, 4 tests, and
`TensuraToolBeltTest`, 3 - no Minecraft runtime needed) and
`./gradlew runGameTestServer` (`ToolBeltEvolutionGameTests`, 4 ladder tests, plus
`ToolBeltContentsGameTests.beltKeepsItsToolsAcrossEvolution`). `expectedTests = 5` in
`build.gradle` must match the `@GameTest` count, and the run stages the pack closure into
`run/mods` from the `gametestMods` configuration first. The suite is deliberately run
without Curios, which is why every run logs the `BeltProgress` warning - that is the
warning working, not a failure.

---

## Gotchas (do not regress them)

- **The ladder is Gear Evolution's canonical material progression.** Every tier
  uses the values GE ships for leather → chainmail → iron → gold → diamond →
  netherite (from its builtin `gear_existence` chestplate ladders), each belt tier
  taking the rung in its position: `belt` (called leather in Traveler Tool Belt's
  own config) 0 → 550 @ 0.003, `copper_belt` (chainmail's slot) 550 → 1350 @
  0.003, `iron_belt` 1350 → 3200 @ 0.003, `gold_belt` 3200 → 5000 @ 0.003,
  `diamond_belt` 5000 → 7800 @ 0.003, `netherite_belt` 7800 → 17500 @ 0.003 and
  terminal. The climb rate is flat 0.003 because that is what canonical GE uses on
  every rung of the progression. Two numbers are ours, not GE's: **gold's 5000**
  (canonical armor skips gold entirely - golden armor is a side branch to silver at
  500 - so 5000 was chosen as the midpoint between iron 3200 and diamond 7800), and
  **emerald's absence**: `lightmans-tensura`'s wallet inserts a wallet-only rung
  5000 → 6400, which has no belt - `gold_belt` hands off exactly at its floor (5000)
  and `diamond_belt` spans the whole band by starting at 5000 too, so no EP is
  dead. Hand-offs land exactly on the next rung's floor, so no tier is skipped and
  none is entered part-way - both test suites assert that, and neither may be
  loosened to allow a gap. Both suites also pin gold's hand-off at the emerald
  floor; if `lightmans-tensura`'s emerald band moves, this ladder moves with it.
- **Traveler Tool Belt ships exactly six items** - `belt`, `copper_belt`, `iron_belt`,
  `gold_belt`, `diamond_belt`, `netherite_belt` (verified against
  travellertoolbelt 1.0.4's `ModItems`; there are no other items in its registry). A
  new tier upstream must get a chain file, or
  `ladderCoversEveryTravelerToolBeltItem` fails - that is the point of the test, do not
  weaken it into a subset check.
- **GE keys an addon chain off its `item` field, not the file name or path**, and scans
  `addon_chains/` in *every* namespace - which is why the files live under
  `data/tensura_tool_belt/addon_chains/` and why `belt_leather.json` may describe
  `travelertoolbelt:belt`. Keep the file names as they are: both suites pin the exact
  six file names, and `chainDirectoryHoldsExactlyTheLadder` rejects strays.
- **`maxEP` is required by Gear Evolution's chain parser** (`DefinitionNumbers.integer`
  throws when the key is absent, and a throwing chain is skipped with a log line, not a
  crash). The terminal rung therefore carries `maxEP: 17500` - the canonical netherite
  ceiling, where the belt ladder ends (the wallets run on past it into GE's magisteel
  tail, 18000 → 52000; belts have no such tiers) - rather than omitting it. `minEP` defaults to 0 when
  absent; `evolution` is genuinely optional and its absence is what makes a rung
  terminal.
- **No `evolutionOptions`.** The ladder is linear; the GameTest asserts every rung has
  empty `branchOptions`, so adding a fork means touching the tests deliberately (GE
  resolves branches by its own config's strategy, which is not data - see
  `lightmans-tensura` and `tensura-iaf-growth` for what branching costs).
- **A belt keeps its tools through evolution, and no compat is needed to make that
  true.** Settled by reading both evolution paths and then proving it in
  `ToolBeltContentsGameTests.beltKeepsItsToolsAcrossEvolution`, which fills a copper
  belt, sets its EP to the copper ceiling, and drives Gear Evolution's real accessory
  path (`GearKillEpHandler.applyAccessoryKill`) until it becomes an iron belt. Three
  facts make the data copy unnecessary, and all three have to hold: Traveler Tool Belt
  keeps contents in the *vanilla* `DataComponents.CONTAINER` component
  (`ToolBeltWrapper.loadBeltContents`/`setChanged`) rather than in state of its own;
  its slot count comes from the *item* (`ToolBeltItem.getSlotCount`), so there is no
  per-tier data to reconcile - which is exactly what Gear Evolution's Traveler's
  Backpack compat exists to do for backpacks (skin, per-tier slot components, legacy
  NBT); and every evolution path rebuilds the stack from the old component patch -
  Tensura's `DeathHandler` as `new ItemStack(holder, count, stack.getComponentsPatch())`,
  Gear Evolution's `MiningEpLogic` fallback (the one addon chains use) through
  `ItemStack.transmuteCopy`, which is the same thing. Before adding a compat, re-check
  those three: if Traveler Tool Belt ever moves contents out of a vanilla component, or
  a path starts building a bare stack, the contents are silently lost and this test is
  where it will show up.
- **Gear Evolution exposes no post-evolution hook.** Its own per-mod compats
  (`TravelersBackpackEvolutionCompat`, `SophisticatedBackpackEvolutionCompat`, the Ice
  and Fire and Simply Swords ones) are hardcoded calls inside
  `GearEquipPipeline.completeEvolution`, and `GearEvolutionServices` publishes catalogs
  only. So any future fix-up this mod needs *after* an evolution needs a mixin into
  Gear Evolution - an org-level exception that is not sanctioned here, and the reason
  the answer above is data-level rather than a compat.
- **A belt earns EP only while worn, and only through Curios - which is the one way
  this mod fails silently.** Gear Evolution's accessory support is Curios-typed
  throughout (`compat/neoforge/CuriosGearAccess`: the `CurioChangeEvent` listener, the
  kill EP sources, `reconcileEquipped`, and the `ChangedSlotWriteback` /
  `PostCommitPlayerQueue` write-back of an evolved belt into its slot), and
  `GearEvolutionNeoForge` installs it only when the `curios` mod is loaded. There is no
  Accessories or Trinkets equivalent anywhere in its jar - while Traveler Tool Belt
  supports both (`neoforge/compat/BeltCurioIntegration` and `compat/BeltAccessory`; its
  two optional Modrinth dependencies are Curios API and Accessories). So an
  Accessories-only or Trinkets-only pack wears the belt fine and simply never earns EP,
  with no error at all. `BeltProgress` reports exactly that configuration at startup,
  and  `TensuraToolBeltTest` pins both the rule and the fact that the warning names all
  three mods. Do not weaken it into a config-dependent message: the mod list is the only
  input there.
- **The pack ships Curios, so the Accessories-only failure mode is not the supported
  configuration.** Verified 2026-10-27 against the Tensura RPG instance's `mods/`:
  `curios-neoforge-9.5.1+1.21.1.jar` is present and there is no Accessories jar. Keep the
  warning regardless - it is what tells a future pack without Curios that the ladder is
  inert (issue #2, closed on this evidence and to be reopened if that changes).

- **An Accessories bridge cannot be data, and is not this repo's for the taking.** Gear
  Evolution's two hook setters (`GearKillEpHandler.setAccessoryKillHook` /
  `setAccessoryKillSourceHook`) are public but have no getters, so anything that sets
  them replaces Gear Evolution's own Curios hook rather than adding to it - and they
  cover EP awards only; the tracking and the write-back live in the Curios-typed class
  above. Closing the gap properly is either a pack-level decision (ship Curios) or a
  Gear Evolution change upstream; a mod that forced it would be a sanctioned
  mixin workstream of its own, like `ds-tensura-fix`, not a data change here.
- **Mining EP never reaches a belt.** It goes to the held tool, so a belt is a
  kill-EP-only item; do not describe the belts as earning EP from mining in any
  player-facing text. Our GameTests run without Curios and call the accessory API
  directly (`GearKillEpHandler.applyAccessoryKill`), which is why they prove the
  evolution and its write-back, but not that the Curios hook fires in a live pack -
  that part is the user's in-game check.
- **EP accrual is Gear Evolution's doing, not this mod's.** The chains only describe
  bands and hand-offs; if belts stop earning EP, that is a GE-side behaviour change
  (its kill/mining EP handlers and `GearEquipLogic`), not a data problem here.

## Scope doctrine - repo additions

- **Data only.** The repo's whole user-visible content is six JSON files. Any code
  added beyond the entry point needs a reason that data cannot satisfy; the org's
  "public API only, no mixins" rule applies unchanged. Nothing in `src/main` touches
  Traveler Tool Belt, Tensura or Gear Evolution: the only reflection into a third-party
  mod is in `src/gametest`, where it is the way the tests reach a belt's contents.

## Naming - this repo

- Repo and folder: `tensura-tool-belt`; `mod_id`: `tensura_tool_belt`, matching the
  `@Mod` constant, `settings.gradle` and `archivesName`; Java base package
  `org.bitwisemadness.tensuratoolbelt`, with the Maven `group` in `build.gradle`
  matching.

## CI & release model - this repo

- `ci.yml` runs the template's three checks (junit, gametest, version-bump).
- `release.yml` publishes a GitHub release for `v<mod_version>` on a push to `main`,
  attaching the built jar, and does nothing when that release already exists. It is
  the template's file (promoted in `neoforge-mod-template` #12) with exactly the two
  edits the template itself asks for when promoting: the jar name is now derived from
  `mod_id` like the template's, and the release notes are rewritten to be this mod's
  player-facing notes rather than the template's example.
- `publish.yml` publishes the jar as a Maven artifact to the org's GitHub Packages
  registry when that release is published, via the `maven-publish` block in
  `build.gradle` (`artifactId` is `mod_id`, pom metadata reads `gradle.properties`).
  `workflow_dispatch` republishes a version after a packaging fix without a new
  release.
- Pre-releases are opt-in: an `-alpha`/`-beta` version publishes only when the
  repository variable `ALPHA_RELEASES` is `true`. It is enabled on this repo, so
  `1.0.0-alpha.1` is a downloadable pre-release; a version with no matching release
  and the variable switched off is skipped with a log line, not a failure.
- The release workflow builds with `./gradlew build`, so the jar it attaches is the
  one the JUnit suite passed against, not a separate packaging path.
