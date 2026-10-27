package org.bitwisemadness.tensuratoolbelt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The mod id and the startup rule that keeps a dead ladder from being silent. Both
 * are plain Java, so this suite needs no Minecraft runtime - the CI {@code junit}
 * check also asserts a non-zero test count, so it cannot pass vacuously.
 */
class TensuraToolBeltTest {

    @Test
    void modIdIsALowercasePathSegment() {
        // MOD_ID is a compile-time constant, so referencing it does not trigger
        // class initialization - this test needs no Minecraft runtime.
        assertTrue(TensuraToolBelt.MOD_ID.matches("[a-z][a-z0-9_]{2,63}"),
                "mod id must be a lowercase resource location path segment");
    }

    /**
     * The belts only ever fail silently in one configuration: the evolution engine is
     * present to read the chains, but the accessory provider it awards worn-gear EP
     * through is not, so nothing in the pack ever grants a worn belt EP.
     */
    @Test
    void beltsOnlyWarnAboutEarningNoEpWhenTheEngineHasNoAccessoryProvider() {
        assertTrue(BeltProgress.beltsCannotEarnEp(true, false),
                "engine without Curios leaves worn belts unable to earn EP, and it must be reported");
        assertFalse(BeltProgress.beltsCannotEarnEp(true, true),
                "engine with Curios is the working configuration and must not be reported");
        assertFalse(BeltProgress.beltsCannotEarnEp(false, true),
                "without the engine there is no ladder to warn about");
        assertFalse(BeltProgress.beltsCannotEarnEp(false, false),
                "without the engine there is no ladder to warn about");
    }

    /**
     * The warning has to name all three mods to be actionable: the engine that is
     * present, the provider that is missing, and the belts that will not progress.
     */
    @Test
    void theWarningNamesEveryModTheOperatorHasToActOn() {
        String warning = BeltProgress.NO_ACCESSORY_PROVIDER_WARNING;
        assertTrue(warning.contains(BeltProgress.EVOLUTION_ENGINE), "names the engine that is installed");
        assertTrue(warning.contains(BeltProgress.ACCESSORY_PROVIDER), "names the provider to install");
        assertTrue(warning.contains(BeltProgress.BELT_MOD), "names the belts that will not progress");
    }
}
