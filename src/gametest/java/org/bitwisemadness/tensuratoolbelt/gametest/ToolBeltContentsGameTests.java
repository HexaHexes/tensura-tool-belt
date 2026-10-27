package org.bitwisemadness.tensuratoolbelt.gametest;

import com.mojang.authlib.GameProfile;
import dev.gearevo.gearevolution.handler.GearEquipLogic;
import dev.gearevo.gearevolution.handler.GearKillEpHandler;
import dev.gearevo.gearevolution.reconcile.GearSlotContext;
import io.github.manasmods.tensura.registry.item.misc.TensuraDataComponents;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.bitwisemadness.tensuratoolbelt.TensuraToolBelt;

/**
 * What happens to the tools in a belt when the belt evolves.
 *
 * <p>The belt's contents are Traveler Tool Belt's own state: it keeps them in a
 * vanilla container data component on the stack, and its slot count comes from the
 * item, not from stored data. This suite drives a loaded copper belt through Gear
 * Evolution's real accessory evolution path (the one a belt earns EP on, since a
 * belt is worn rather than held) and asserts the tools are still there on the
 * evolved belt. It is the headless stand-in for the in-game check, and it is what
 * tells us whether a data-copying compat is needed at all.</p>
 *
 * <p>Traveler Tool Belt is reached reflectively, the same way a compat would have
 * to: the mod is all rights reserved and exposes no API this mod compiles against.</p>
 */
@GameTestHolder(TensuraToolBelt.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ToolBeltContentsGameTests {

    /** Traveler Tool Belt's belt wrapper, the only way in and out of a belt's contents. */
    private static final String WRAPPER_CLASS = "com.tiviacz.travelertoolbelt.toolbelt.ToolBeltWrapper";

    private static final String COPPER_BELT = "travelertoolbelt:copper_belt";
    private static final String IRON_BELT = "travelertoolbelt:iron_belt";

    private ToolBeltContentsGameTests() {
    }

    // ------------------------------------------------------------------ tools

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id))
                .orElseThrow(() -> new GameTestAssertException("missing item: " + id));
    }

    private static String key(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    /**
     * A detached player, constructed directly: nothing here needs level membership,
     * and a real placeNewPlayer would fire login and chunk-sync payloads the GameTest
     * connection rejects.
     */
    private static ServerPlayer detachedPlayer(GameTestHelper helper) {
        return new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "ToolBeltGameTest"),
                ClientInformation.createDefault());
    }

    private static Object beltWrapper(ItemStack belt) {
        try {
            Class<?> wrapper = Class.forName(WRAPPER_CLASS);
            return wrapper.getMethod("fromStack", ItemStack.class).invoke(null, belt);
        } catch (ReflectiveOperationException failure) {
            throw new GameTestAssertException("Traveler Tool Belt is not reflectable: " + failure);
        }
    }

    @SuppressWarnings("unchecked")
    private static NonNullList<ItemStack> beltSlots(Object wrapper) {
        try {
            return (NonNullList<ItemStack>) wrapper.getClass().getMethod("getBeltContents").invoke(wrapper);
        } catch (ReflectiveOperationException failure) {
            throw new GameTestAssertException("Traveler Tool Belt is not reflectable: " + failure);
        }
    }

    /**
     * Puts the tools in the belt the way the belt's own screen does, and saves them.
     *
     * <p>The wrapper snapshots the belt's contents when it is built and writes the
     * whole list back on save, so the same wrapper has to do both.</p>
     */
    private static void fillBelt(ItemStack belt, ItemStack... tools) {
        Object wrapper = beltWrapper(belt);
        NonNullList<ItemStack> slots = beltSlots(wrapper);
        require(slots.size() >= tools.length,
                "belt has " + slots.size() + " slots, need " + tools.length);
        for (int i = 0; i < tools.length; i++) {
            slots.set(i, tools[i]);
        }
        try {
            wrapper.getClass().getMethod("setChanged").invoke(wrapper);
        } catch (ReflectiveOperationException failure) {
            throw new GameTestAssertException("Traveler Tool Belt is not reflectable: " + failure);
        }
    }

    /** The belt's saved contents, read back through a fresh wrapper. */
    private static List<String> beltContents(ItemStack belt) {
        List<String> contents = new ArrayList<>();
        for (ItemStack stack : beltSlots(beltWrapper(belt))) {
            if (!stack.isEmpty()) {
                contents.add(key(stack));
            }
        }
        return contents;
    }

    // ------------------------------------------------------------------ tests

    /**
     * A copper belt holding two tools, filled to its EP ceiling, evolves on a kill
     * through Gear Evolution's accessory path - and arrives at the iron tier with
     * both tools still in it.
     */
    @GameTest(template = "empty")
    public static void beltKeepsItsToolsAcrossEvolution(GameTestHelper helper) {
        ServerPlayer player = detachedPlayer(helper);
        // The accessory slot a belt lives in, which is the one Gear Evolution's
        // accessory kill hook awards EP through.
        GearSlotContext beltSlot = GearSlotContext.curios("belt", 0);

        ItemStack copper = new ItemStack(item(COPPER_BELT));
        fillBelt(copper, new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.IRON_SHOVEL));
        require(beltContents(copper).equals(List.of("minecraft:diamond_pickaxe", "minecraft:iron_shovel")),
                "precondition: the belt did not hold the tools, it held " + beltContents(copper));

        // Gear Evolution writes the EP band onto the stack from the addon chain.
        GearEquipLogic.initTrackedGear(player, copper, beltSlot);
        Double ceiling = copper.get(TensuraDataComponents.MAX_EP.get());
        require(ceiling != null && ceiling > 0,
                "Gear Evolution did not track the copper belt (no EP ceiling component)");
        copper.set(TensuraDataComponents.EP.get(), ceiling);

        LivingEntity victim = helper.spawn(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
        ItemStack[] evolved = new ItemStack[1];
        GearKillEpHandler.applyAccessoryKill(player, victim, copper, beltSlot, stack -> evolved[0] = stack);

        require(evolved[0] != null, "a full copper belt did not evolve on a kill");
        require(key(evolved[0]).equals(IRON_BELT),
                "the copper belt evolved into " + key(evolved[0]) + ", expected " + IRON_BELT);
        require(beltContents(evolved[0]).equals(List.of("minecraft:diamond_pickaxe", "minecraft:iron_shovel")),
                "the evolved belt lost its tools, it holds " + beltContents(evolved[0]));

        // The evolved belt is tracked at its own tier, so the ladder carries on.
        require(evolved[0].get(TensuraDataComponents.MAX_EP.get()) != null,
                "the evolved belt is not tracked for EP");
        helper.succeed();
    }
}
