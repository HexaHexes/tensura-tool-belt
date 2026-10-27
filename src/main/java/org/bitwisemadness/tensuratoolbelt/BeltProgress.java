package org.bitwisemadness.tensuratoolbelt;

/**
 * The one way this mod can fail silently, kept as a pure rule.
 *
 * <p>Traveler Tool Belt wears a belt through Curios, Accessories or Trinkets, but
 * Gear Evolution's accessory support is Curios-typed throughout
 * ({@code compat/neoforge/CuriosGearAccess}, plus the kill-EP hooks and the
 * Curios change listener it installs): with Curios absent there is no EP path, no
 * tracking when the belt is put on, and no write-back of an evolved belt into its
 * slot. Nothing in this mod's data can provide one, so the honest thing it can do is
 * say so at startup instead of looking like a ladder that never fills up.</p>
 *
 * <p>Deliberately free of Minecraft types, so {@code TensuraToolBeltTest} can pin the
 * rule without a Minecraft runtime - the entry point is what reads the loaded mod
 * list and logs.</p>
 */
final class BeltProgress {

    /** Traveler Tool Belt, whose belts the chains evolve. */
    static final String BELT_MOD = "travelertoolbelt";
    /** The evolution engine that reads the chains. */
    static final String EVOLUTION_ENGINE = "gearevolution";
    /** The only accessory provider Gear Evolution awards worn-gear EP through. */
    static final String ACCESSORY_PROVIDER = "curios";

    /** Logged once at startup when the belts cannot earn anything. */
    static final String NO_ACCESSORY_PROVIDER_WARNING = EVOLUTION_ENGINE
            + " is installed but " + ACCESSORY_PROVIDER + " is not: Gear Evolution awards worn-gear EP only through "
            + ACCESSORY_PROVIDER + ", so the belts of " + BELT_MOD + " will never earn EP or evolve. Install "
            + ACCESSORY_PROVIDER + " (Traveler Tool Belt's Accessories integration is not an EP source), or this mod's"
            + " ladder does nothing.";

    private BeltProgress() {
    }

    /**
     * Whether worn belts will never earn EP in this instance.
     *
     * <p>Without the engine there is no ladder to warn about; its data simply sits
     * inert, which is the documented optional-dependency behaviour.</p>
     */
    static boolean beltsCannotEarnEp(boolean gearEvolutionLoaded, boolean curiosLoaded) {
        return gearEvolutionLoaded && !curiosLoaded;
    }
}
