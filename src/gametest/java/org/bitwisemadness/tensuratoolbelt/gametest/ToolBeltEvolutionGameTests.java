package org.bitwisemadness.tensuratoolbelt.gametest;

import dev.gearevo.gearevolution.GearEvolutionServices;
import dev.gearevo.gearevolution.chain.AddonGearChainOverride;
import dev.gearevo.gearevolution.chain.ChainEvolutionResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.bitwisemadness.tensuratoolbelt.TensuraToolBelt;

/**
 * Runtime coverage for the belt ladder. Run with {@code gradlew runGameTestServer}:
 * NeoForge's patched server Main boots a headless GameTestServer in this project's
 * {@code run/} folder with the pack's dependency mods staged from the
 * {@code gametestMods} configuration into {@code run/mods/} - no CurseForge
 * instance is involved.
 *
 * <p>The suite proves the shipped {@code addon_chains} files are actually loaded by
 * Gear Evolution (its catalog is queried, not the files), that the rungs hand off
 * exactly - no tier skipped and none pre-earned - that Gear Evolution's own
 * resolver walks the ladder, and that the ladder covers every item Traveler Tool
 * Belt registers. These classes are inert in production: NeoForge registers
 * {@code @GameTestHolder} classes only in a GameTest run.</p>
 */
@GameTestHolder(TensuraToolBelt.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ToolBeltEvolutionGameTests {

    /**
     * The whole ladder, at literal values. Gear Evolution's canonical material
     * progression - leather, chainmail, iron, gold, diamond, netherite - at its own
     * values and its flat 0.003 climb rate, each belt tier taking the rung in its
     * position: {@code belt} is leather, {@code copper_belt} sits in chainmail's
     * slot. The wallets share every one of these ceilings; their emerald rung
     * (5000..6400) has no belt, so the diamond belt carries that band.
     */
    private static final List<Tier> LADDER = List.of(
            new Tier("travelertoolbelt:belt", 0, 550, 0.003, "travelertoolbelt:copper_belt"),
            new Tier("travelertoolbelt:copper_belt", 550, 1350, 0.003, "travelertoolbelt:iron_belt"),
            new Tier("travelertoolbelt:iron_belt", 1350, 3200, 0.003, "travelertoolbelt:gold_belt"),
            new Tier("travelertoolbelt:gold_belt", 3200, 5000, 0.003, "travelertoolbelt:diamond_belt"),
            new Tier("travelertoolbelt:diamond_belt", 5000, 7800, 0.003, "travelertoolbelt:netherite_belt"),
            new Tier("travelertoolbelt:netherite_belt", 7800, 17500, 0.003, null));

    /** The lightest rung's floor: the ladder starts at nothing earned. */
    private static final int FIRST_FLOOR = 0;
    /** The top rung's ceiling: the canonical netherite ceiling; belts end here. */
    private static final int TERMINAL_CEILING = 17500;
    /** The band the wallets spend on their emerald rung; the diamond belt carries it. */
    private static final int EMERALD_BAND_FLOOR = 5000;
    private static final int EMERALD_BAND_TOP = 6400;

    private record Tier(String item, int minEp, int maxEp, double epGain, String evolution) {
    }

    private ToolBeltEvolutionGameTests() {
    }

    // ------------------------------------------------------------------ tools

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private static AddonGearChainOverride chain(String item) {
        return GearEvolutionServices.addonChains().get(ResourceLocation.parse(item))
                .orElseThrow(() -> new GameTestAssertException("addon chain not loaded by Gear Evolution: " + item));
    }

    /** Every item Traveler Tool Belt registers, whatever it is named. */
    private static TreeSet<String> registeredBeltModItems() {
        TreeSet<String> ids = new TreeSet<>();
        for (ResourceLocation id : BuiltInRegistries.ITEM.keySet()) {
            if (id.getNamespace().equals("travelertoolbelt")) {
                ids.add(id.toString());
            }
        }
        return ids;
    }

    // ------------------------------------------------------------------ tests

    /**
     * The rungs are registered with Gear Evolution through {@code addon_chains} -
     * its mechanism for items it ships no builtin chain for, which is every tool
     * belt. Asserts the file's item, the EP band, the climb rate and the hand-off
     * of each rung at literal values.
     */
    @GameTest(template = "empty")
    public static void beltLadderRegisteredWithGearEvolution(GameTestHelper helper) {
        for (Tier tier : LADDER) {
            String where = tier.item();
            AddonGearChainOverride entry = chain(where);
            require(entry.item().toString().equals(tier.item()),
                    where + " registers item " + entry.item());
            require(entry.minEP() == tier.minEp(),
                    where + " minEP=" + entry.minEP() + " expected " + tier.minEp());
            require(entry.maxEP() == tier.maxEp(),
                    where + " maxEP=" + entry.maxEP() + " expected " + tier.maxEp());
            require(Math.abs(entry.epGain() - tier.epGain()) < 1.0E-9,
                    where + " epGain=" + entry.epGain() + " expected " + tier.epGain());
            if (tier.evolution() == null) {
                require(entry.evolution() == null,
                        where + " is the top rung but evolves to " + entry.evolution());
            } else {
                require(entry.evolution() != null && entry.evolution().toString().equals(tier.evolution()),
                        where + " evolves to " + entry.evolution() + " expected " + tier.evolution());
            }
            require(entry.branchOptions().isEmpty(),
                    where + " carries branch options; the belt ladder is linear");
        }
        helper.succeed();
    }

    /**
     * The ladder as Gear Evolution sees it is contiguous: floors ascend, every rung
     * hands off exactly onto the next rung's floor (so nothing is skipped and no
     * tier is entered part-way through), exactly one rung is terminal, and the top
     * rung ends on the canonical netherite ceiling.
     */
    @GameTest(template = "empty")
    public static void beltLadderIsContiguous(GameTestHelper helper) {
        List<AddonGearChainOverride> rungs = LADDER.stream().map(tier -> chain(tier.item())).toList();

        require(rungs.get(0).minEP() == FIRST_FLOOR,
                "the ladder starts at EP " + rungs.get(0).minEP() + ", not " + FIRST_FLOOR);

        int terminals = 0;
        for (int i = 0; i < rungs.size(); i++) {
            AddonGearChainOverride rung = rungs.get(i);
            require(rung.minEP() < rung.maxEP(),
                    LADDER.get(i).item() + " has an empty EP band " + rung.minEP() + ".." + rung.maxEP());
            boolean last = i == rungs.size() - 1;
            if (last) {
                require(rung.evolution() == null, "the top rung still evolves to " + rung.evolution());
                require(rung.maxEP() == TERMINAL_CEILING,
                        "the top rung's ceiling is " + rung.maxEP() + " expected " + TERMINAL_CEILING);
                terminals++;
            } else {
                AddonGearChainOverride next = rungs.get(i + 1);
                require(rung.evolution() != null, LADDER.get(i).item() + " has no hand-off");
                require(rung.evolution().toString().equals(LADDER.get(i + 1).item()),
                        LADDER.get(i).item() + " hands off to " + rung.evolution()
                                + " expected " + LADDER.get(i + 1).item());
                require(rung.maxEP() == next.minEP(),
                        LADDER.get(i).item() + " hands off at EP " + rung.maxEP()
                                + " but the next rung starts at " + next.minEP());
            }
        }
        require(terminals == 1, terminals + " terminal rungs; the ladder must have exactly one");

        // The wallets' emerald rung has no belt: gold must hand off exactly where
        // it begins, and the diamond belt must carry its band rather than leave
        // 5000..6400 as dead EP that no belt can spend.
        AddonGearChainOverride gold = chain("travelertoolbelt:gold_belt");
        require(gold.maxEP() == EMERALD_BAND_FLOOR,
                "the gold belt hands off at " + gold.maxEP() + " - the emerald band's floor is " + EMERALD_BAND_FLOOR);
        AddonGearChainOverride diamond = chain("travelertoolbelt:diamond_belt");
        require(diamond.minEP() <= EMERALD_BAND_FLOOR && diamond.maxEP() > EMERALD_BAND_TOP,
                "the diamond belt spans " + diamond.minEP() + ".." + diamond.maxEP()
                        + " and does not cover the emerald band " + EMERALD_BAND_FLOOR + ".." + EMERALD_BAND_TOP);
        helper.succeed();
    }

    /** Gear Evolution's own resolver walks the ladder end to end. */
    @GameTest(template = "empty")
    public static void resolverWalksTheBeltLadder(GameTestHelper helper) {
        for (int i = 0; i < LADDER.size() - 1; i++) {
            String from = LADDER.get(i).item();
            String expected = LADDER.get(i + 1).item();
            ResourceLocation next = ChainEvolutionResolver.resolveNext(ResourceLocation.parse(from));
            require(next != null && next.toString().equals(expected),
                    "resolver walks " + from + " to " + next + " expected " + expected);
        }
        String top = LADDER.get(LADDER.size() - 1).item();
        ResourceLocation beyond = ChainEvolutionResolver.resolveNext(ResourceLocation.parse(top));
        require(beyond == null,
                "the top rung " + top + " still resolves onwards to " + beyond);
        helper.succeed();
    }

    /**
     * The ladder covers the mod: every item Traveler Tool Belt registers is a rung,
     * and every rung exists. A tool belt tier added upstream without its chain file
     * fails here instead of silently never evolving.
     */
    @GameTest(template = "empty")
    public static void ladderCoversEveryTravelerToolBeltItem(GameTestHelper helper) {
        TreeSet<String> registered = registeredBeltModItems();
        TreeSet<String> ladderItems = new TreeSet<>();
        for (Tier tier : LADDER) {
            ladderItems.add(tier.item());
            require(BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(tier.item())),
                    "rung item is not registered: " + tier.item());
        }
        require(registered.equals(ladderItems),
                "Traveler Tool Belt registers " + new ArrayList<>(registered)
                        + " but the ladder covers " + ladderItems);
        helper.succeed();
    }
}
