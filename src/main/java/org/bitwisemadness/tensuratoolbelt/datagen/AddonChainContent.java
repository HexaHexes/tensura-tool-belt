package org.bitwisemadness.tensuratoolbelt.datagen;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The belt ladder this mod ships, as Gear Evolution {@code addon_chains} files,
 * in the mod's own namespace ({@code data/tensura_tool_belt/addon_chains/}).
 * Gear Evolution discovers {@code addon_chains} in any namespace, so the files
 * land where they are read from.
 *
 * <p>The ladder is Gear Evolution's canonical material progression - leather,
 * copper, iron, gold, diamond, netherite - at its own values: every canonical
 * chestplate climbs at {@link #EP_GAIN}, each belt tier takes the rung in its
 * position ({@code belt} is leather, {@code copper_belt} sits in chainmail's
 * slot), and every band hands off exactly onto the next rung's floor. The
 * wallets' emerald rung (5000..6400) has no belt, so the gold belt hands off
 * where it begins and the diamond belt spans it rather than leave dead EP.</p>
 *
 * <p>{@code build()} self-validates before anything is written: the datagen run
 * fails loudly on a drifted table instead of shipping a broken ladder, which is
 * what makes the table the single source of truth.</p>
 */
public final class AddonChainContent {

    /** Where the chains ship, inside the jar. */
    private static final String CHAIN_DIRECTORY = "data/tensura_tool_belt/addon_chains";

    /** The item the chains evolve, and the namespace every rung must name. */
    private static final String MOD_NAMESPACE = "travelertoolbelt";

    /** The climb rate of every canonical Gear Evolution chestplate ladder. */
    private static final double EP_GAIN = 0.003;

    /** The canonical netherite ceiling; the wallets run on past it, belts end here. */
    private static final int TERMINAL_CEILING = 17500;

    /** The wallets spend 5000..6400 on their emerald rung, which has no belt. */
    private static final int EMERALD_BAND_FLOOR = 5000;
    private static final int EMERALD_BAND_TOP = 6400;

    private static final Rung[] LADDER = {
            rung("belt_leather", "travelertoolbelt:belt", 0, 550, "travelertoolbelt:copper_belt"),
            rung("belt_copper", "travelertoolbelt:copper_belt", 550, 1350, "travelertoolbelt:iron_belt"),
            rung("belt_iron", "travelertoolbelt:iron_belt", 1350, 3200, "travelertoolbelt:gold_belt"),
            rung("belt_gold", "travelertoolbelt:gold_belt", 3200, 5000, "travelertoolbelt:diamond_belt"),
            rung("belt_diamond", "travelertoolbelt:diamond_belt", 5000, 7800, "travelertoolbelt:netherite_belt"),
            rung("belt_netherite", "travelertoolbelt:netherite_belt", 7800, TERMINAL_CEILING, null),
    };

    private AddonChainContent() {
    }

    /** Every chain file, keyed by its path inside the jar. */
    public static Map<String, JsonObject> build() {
        Map<String, JsonObject> files = new LinkedHashMap<>();
        for (Rung rung : LADDER) {
            files.put(CHAIN_DIRECTORY + "/" + rung.file() + ".json", chainJson(rung));
        }
        validate(files);
        return files;
    }

    private static Rung rung(String file, String item, int minEp, int maxEp, String evolution) {
        return new Rung(file, item, minEp, maxEp, EP_GAIN, evolution);
    }

    /** Field order matches what Gear Evolution reads, so the output stays canonical. */
    private static JsonObject chainJson(Rung rung) {
        JsonObject json = new JsonObject();
        json.addProperty("item", rung.item());
        json.addProperty("minEP", rung.minEp());
        json.addProperty("maxEP", rung.maxEp());
        json.addProperty("epGain", rung.epGain());
        if (rung.evolution() != null) {
            json.addProperty("evolution", rung.evolution());
        }
        return json;
    }

    /** Fails the datagen run when the table no longer hangs together as a ladder. */
    private static void validate(Map<String, JsonObject> files) {
        List<String> errors = new ArrayList<>();
        if (files.size() != LADDER.length) {
            errors.add("expected " + LADDER.length + " chain files, built " + files.size());
        }
        for (int i = 0; i < LADDER.length; i++) {
            Rung rung = LADDER[i];
            JsonObject chain = files.get(CHAIN_DIRECTORY + "/" + rung.file() + ".json");
            if (chain == null) {
                errors.add(rung.file() + ": no chain file built");
                continue;
            }
            if (!chain.get("item").getAsString().startsWith(MOD_NAMESPACE + ":")) {
                errors.add(rung.file() + " names an item outside the belt mod");
            }
            if (rung.minEp() >= rung.maxEp()) {
                errors.add(rung.file() + " has an empty EP band " + rung.minEp() + ".." + rung.maxEp());
            }
            boolean last = i == LADDER.length - 1;
            if (last) {
                if (rung.evolution() != null) {
                    errors.add(rung.file() + " is the top rung but still evolves");
                }
                if (rung.maxEp() != TERMINAL_CEILING) {
                    errors.add(rung.file() + " must end on the canonical netherite ceiling " + TERMINAL_CEILING);
                }
                continue;
            }
            Rung next = LADDER[i + 1];
            if (next.minEp() != rung.maxEp()) {
                errors.add(rung.file() + " hands off at " + rung.maxEp()
                        + " but the next rung starts at " + next.minEp());
            }
            if (!next.item().equals(rung.evolution())) {
                errors.add(rung.file() + " does not hand off to the next rung");
            }
        }
        if (LADDER.length > 0) {
            Rung gold = LADDER[3];
            Rung diamond = LADDER[4];
            if (gold.maxEp() != EMERALD_BAND_FLOOR) {
                errors.add("the gold belt must hand off where the wallets' emerald rung begins");
            }
            if (diamond.minEp() > EMERALD_BAND_FLOOR || diamond.maxEp() <= EMERALD_BAND_TOP) {
                errors.add("the diamond belt must span the wallets' emerald band");
            }
        }
        if (!errors.isEmpty()) {
            throw new IllegalStateException("the belt ladder table is broken: " + String.join("; ", errors));
        }
    }

    private record Rung(String file, String item, int minEp, int maxEp, double epGain, String evolution) {
    }
}
