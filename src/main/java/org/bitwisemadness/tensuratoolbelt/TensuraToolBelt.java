package org.bitwisemadness.tensuratoolbelt;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.bitwisemadness.tensuratoolbelt.datagen.AddonChainContent;
import org.bitwisemadness.tensuratoolbelt.datagen.JsonFilesProvider;
import org.slf4j.Logger;

/**
 * Mod entry point.
 *
 * <p>This mod is data: the belt ladder ships as Gear Evolution {@code addon_chains}
 * files under {@code data/tensura_tool_belt/addon_chains/}, which Gear Evolution
 * discovers in any namespace. There is no runtime code path - the class exists so the
 * jar is a loadable mod, so there is an identity for the loader to depend on, and so
 * the one configuration that would otherwise fail silently (see {@link BeltProgress})
 * is reported at startup.</p>
 */
@Mod(TensuraToolBelt.MOD_ID)
public final class TensuraToolBelt {

    public static final String MOD_ID = "tensura_tool_belt";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TensuraToolBelt(IEventBus modBus) {
        modBus.addListener(TensuraToolBelt::gatherData);
        if (BeltProgress.beltsCannotEarnEp(
                ModList.get().isLoaded(BeltProgress.EVOLUTION_ENGINE),
                ModList.get().isLoaded(BeltProgress.ACCESSORY_PROVIDER))) {
            LOGGER.warn(BeltProgress.NO_ACCESSORY_PROVIDER_WARNING);
        }
    }

    /** Feeds the belt ladder ({@link AddonChainContent}) into datagen. */
    private static void gatherData(GatherDataEvent event) {
        if (!event.includeServer()) {
            return;
        }
        event.addProvider(new JsonFilesProvider("addon_chains",
                event.getGenerator().getPackOutput(), AddonChainContent.build()));
    }
}
