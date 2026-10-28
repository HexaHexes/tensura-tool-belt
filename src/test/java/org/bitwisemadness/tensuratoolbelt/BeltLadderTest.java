package org.bitwisemadness.tensuratoolbelt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/**
 * The belt ladder as shipped: the six {@code addon_chains} files, read from the
 * packaged resources. This suite needs no Minecraft runtime, so it is the cheap
 * gate that runs before the GameTest server - the GameTest is what proves Gear
 * Evolution actually loads and walks these files in game.
 *
 * <p>Values are duplicated here deliberately: the test must not read the ladder
 * it is validating.</p>
 */
class BeltLadderTest {

    private static final String CHAIN_DIRECTORY = "data/tensura_tool_belt/addon_chains";
    private static final String MOD_NAMESPACE = "travelertoolbelt";
    private static final int FIRST_FLOOR = 0;
    /** The canonical netherite ceiling; the wallets run on past it, belts end here. */
    private static final int TERMINAL_CEILING = 17500;
    /** The wallets spend 5000..6400 on their emerald rung, which has no belt. */
    private static final int EMERALD_BAND_FLOOR = 5000;
    private static final int EMERALD_BAND_TOP = 6400;

    /**
     * Gear Evolution's canonical material progression - leather, chainmail, iron,
     * gold, diamond, netherite - at its own values (every canonical chestplate
     * climbs at 0.003), each belt tier taking the rung in its position: {@code belt}
     * is leather, {@code copper_belt} sits in chainmail's slot. The wallets share
     * every one of these ceilings.
     */
    private static final List<Tier> LADDER = List.of(
            new Tier("belt_leather", "travelertoolbelt:belt", 0, 550, 0.003, "travelertoolbelt:copper_belt"),
            new Tier("belt_copper", "travelertoolbelt:copper_belt", 550, 1350, 0.003, "travelertoolbelt:iron_belt"),
            new Tier("belt_iron", "travelertoolbelt:iron_belt", 1350, 3200, 0.003, "travelertoolbelt:gold_belt"),
            new Tier("belt_gold", "travelertoolbelt:gold_belt", 3200, 5000, 0.003, "travelertoolbelt:diamond_belt"),
            new Tier("belt_diamond", "travelertoolbelt:diamond_belt", 5000, 7800, 0.003,
                    "travelertoolbelt:netherite_belt"),
            new Tier("belt_netherite", "travelertoolbelt:netherite_belt", 7800, 17500, 0.003, null));

    private record Tier(String file, String item, int minEp, int maxEp, double epGain, String evolution) {
    }

    // ------------------------------------------------------------------ tools

    private static JsonObject chain(String file) {
        String resource = CHAIN_DIRECTORY + "/" + file + ".json";
        InputStream stream = BeltLadderTest.class.getClassLoader().getResourceAsStream(resource);
        assertNotNull(stream, "chain file is not packaged: " + resource);
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception failure) {
            throw new AssertionError("chain file is not readable JSON: " + resource, failure);
        }
    }

    /** The shipped chain directory, as the test classpath resolves it. */
    private static List<Path> shippedChainFiles() throws Exception {
        URL directory = BeltLadderTest.class.getClassLoader().getResource(CHAIN_DIRECTORY);
        assertNotNull(directory, "chain directory is not packaged: " + CHAIN_DIRECTORY);
        Path path = Path.of(directory.toURI());
        assertTrue(Files.isDirectory(path), "chain directory is not a directory: " + path);
        try (var entries = Files.list(path)) {
            return entries.filter(entry -> entry.getFileName().toString().endsWith(".json")).sorted().toList();
        }
    }

    private static Set<String> ladderItems() {
        TreeSet<String> items = new TreeSet<>();
        for (Tier tier : LADDER) {
            items.add(tier.item());
        }
        return items;
    }

    // ------------------------------------------------------------------ tests

    /** Every rung ships, in the mod's own namespace, at the ladder's values. */
    @Test
    void everyRungShipsAtItsLadderValues() {
        for (Tier tier : LADDER) {
            JsonObject chain = chain(tier.file());
            assertEquals(tier.item(), chain.get("item").getAsString(),
                    tier.file() + " describes the wrong item");
            assertEquals(MOD_NAMESPACE, chain.get("item").getAsString().split(":")[0],
                    tier.file() + " names an item outside the belt mod");
            assertEquals(tier.minEp(), chain.get("minEP").getAsInt(), tier.file() + " minEP");
            assertEquals(tier.maxEp(), chain.get("maxEP").getAsInt(), tier.file() + " maxEP");
            assertEquals(tier.epGain(), chain.get("epGain").getAsDouble(), 1.0E-9, tier.file() + " epGain");
            if (tier.evolution() == null) {
                assertNull(chain.get("evolution"), tier.file() + " is the top rung but still evolves");
            } else {
                assertEquals(tier.evolution(), chain.get("evolution").getAsString(),
                        tier.file() + " evolves to the wrong item");
            }
        }
    }

    /**
     * The ladder hangs together: it opens at nothing earned, each rung hands off
     * exactly onto the next rung's floor, exactly one rung is terminal, and the top
     * rung ends on the canonical netherite ceiling.
     */
    @Test
    void ladderIsContiguousAndHasOneTerminal() {
        assertEquals(FIRST_FLOOR, chain(LADDER.get(0).file()).get("minEP").getAsInt(),
                "the ladder must open at EP " + FIRST_FLOOR);

        for (int i = 0; i < LADDER.size(); i++) {
            Tier tier = LADDER.get(i);
            JsonObject rung = chain(tier.file());
            int min = rung.get("minEP").getAsInt();
            int max = rung.get("maxEP").getAsInt();
            assertTrue(min < max, tier.file() + " has an empty EP band " + min + ".." + max);

            boolean last = i == LADDER.size() - 1;
            if (!last) {
                Tier next = LADDER.get(i + 1);
                assertEquals(next.item(), rung.get("evolution").getAsString(),
                        tier.file() + " does not hand off to the next rung");
                assertEquals(next.minEp(), max,
                        tier.file() + " hands off at EP " + max + " but the next rung starts at " + next.minEp());
            }
        }

        assertEquals(TERMINAL_CEILING, chain(LADDER.get(LADDER.size() - 1).file()).get("maxEP").getAsInt(),
                "the top rung must end on the canonical netherite ceiling");

        // The emerald rung the wallets use has no belt: gold must hand off exactly
        // where it begins, and the diamond belt must span it rather than leave
        // 5000..6400 as dead EP that no belt can spend.
        assertEquals(EMERALD_BAND_FLOOR, chain("belt_gold").get("maxEP").getAsInt(),
                "the gold belt must hand off where the wallets' emerald rung begins");
        JsonObject diamond = chain("belt_diamond");
        assertTrue(diamond.get("minEP").getAsInt() <= EMERALD_BAND_FLOOR
                        && diamond.get("maxEP").getAsInt() > EMERALD_BAND_TOP,
                "the diamond belt must span the wallets' emerald band");
    }

    /**
     * Climb rates follow the canonical progression: flat 0.003 on every rung, the
     * rate every canonical chestplate ladder uses, and never decreasing.
     */
    @Test
    void climbRatesFollowTheCanonicalProgression() {
        double previous = 0;
        for (Tier tier : LADDER) {
            double rate = chain(tier.file()).get("epGain").getAsDouble();
            assertEquals(tier.epGain(), rate, 1.0E-9, tier.file() + " no longer uses the canonical rate");
            assertTrue(rate >= previous,
                    tier.file() + " climbs at " + rate + ", slower than the rung below it (" + previous + ")");
            previous = rate;
        }
    }

    /** Nothing else ships in the chain directory, and every hand-off lands on a rung. */
    @Test
    void chainDirectoryHoldsExactlyTheLadder() throws Exception {
        TreeSet<String> shipped = new TreeSet<>();
        for (Path file : shippedChainFiles()) {
            String name = file.getFileName().toString();
            shipped.add(name.substring(0, name.length() - ".json".length()));
        }
        TreeSet<String> expected = new TreeSet<>();
        for (Tier tier : LADDER) {
            expected.add(tier.file());
        }
        assertEquals(expected, shipped, "the shipped addon_chains files are not exactly the ladder");

        for (Tier tier : LADDER) {
            if (tier.evolution() != null) {
                assertTrue(ladderItems().contains(tier.evolution()),
                        tier.file() + " evolves into " + tier.evolution() + ", which is not a rung");
            }
        }
    }
}
