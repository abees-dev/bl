package org.bleachhack.module.mods;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import org.bleachhack.event.events.EventOpenScreen;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.*;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.ChatUtill;
import org.bleachhack.util.InvUtils;
import org.bleachhack.util.SlotUtils;


public class AutoVault extends Module {
    private static final Item[] ORES_ITEMS = {
            Items.STONE, Items.COAL_BLOCK, Items.COPPER_BLOCK, Items.IRON_BLOCK,
            Items.GOLD_BLOCK, Items.LAPIS_BLOCK, Items.REDSTONE_BLOCK, Items.DIAMOND_BLOCK, Items.EMERALD_BLOCK,
            Items.PACKED_ICE, Items.PRISMARINE_SHARD, Items.CYAN_DYE, Items.SMALL_AMETHYST_BUD, Items.CALCITE
    };

    private int pv = 1;
    public static int timer = 0;
    public static int delay = 0;
    public boolean isStop = false;

    public AutoVault() {
        super("AutoVault", -1, ModuleCategory.CHEST, "Automatically dumps your inventory into a chest",
                new SettingSlider("Total Vault", 1, 50, 1, 0).withDesc("The setting max pv for player"),
                new SettingSlider("Start Vault", 1, 50, 5, 0).withDesc("The setting start pv for, if default = vault 1 when vault is full it will move to vault 2"),
                new SettingSlider("Delay", 60, 600, 60, 0).withDesc("The setting delay in seconds"),
                new SettingToggle("Filter", false).withDesc("Filters certain blocks.").withChildren(
                        new SettingMode("Mode", "Blacklist", "Whitelist").withDesc("How to handle the list."),
                        new SettingItemList("Edit Items", "Edit Filtered Items").withDesc("Edit the filtered Items."))
        );
    }

    @Override
    public void onEnable(boolean inWorld) {
        super.onEnable(inWorld);
        pv = getSetting(1).asSlider().getValue().intValue();
        delay = getSetting(2).asSlider().getValue().intValue();
    }

    @BleachSubscribe
    public void onTick(EventTick event) {
        if (timer < delay * 20) {
            timer++;
            isStop = true;
            return;
        }

        if (isStop) {
            isStop = false;
            timer = 0;
        }

        if (pv == -1) {
            BleachLogger.info("All vaults are full, stopping");
            setEnabled(false);
            pv = getSetting(1).asSlider().getValueInt();
            return;
        }

        timer = 0;
        if (mc.player == null) return;

        ChatUtill.sendPlayerMsg("/pv " + pv);
    }

    @BleachSubscribe
    public void onOpenScreen(EventOpenScreen event) {
        // Chest is open
        if (event.getScreen() instanceof GenericContainerScreen) {

            mc.execute(() -> {
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        boolean isFullInventory = isFullPV();
                        BleachLogger.info("Inventory is full: " + isFullInventory);
                        if (isFullInventory) {
                            pv++;
                            if (pv > getSetting(0).asSlider().getValue()) {
                                pv = -1;
                                closeInventory();
                                return;
                            }
                            BleachLogger.info("Vault " + (pv - 1) + " is full, moving to vault " + pv);
                            mc.execute(() -> {
                                closeInventory();
                                ChatUtill.sendPlayerMsg("/pv " + pv);
                            });
                            return;
                        }
                        dumpItem();

                        mc.execute(this::closeInventory);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            });


        }
    }


    private boolean isFullPV() {
        if (mc.player == null) return false;
        ScreenHandler screenHandler = mc.player.currentScreenHandler;
        if (screenHandler == null) return false;
        for (int i = 0; i < SlotUtils.indexToId(SlotUtils.MAIN_START); i++) {
            if (screenHandler.getSlot(i).getStack().getItem() == Items.AIR) {
                return false;
            }
        }
        return true;
    }

    private void dumpItem() {
        if (mc.player == null) return;
        ScreenHandler screenHandler = mc.player.currentScreenHandler;
        int playerInvOffset = SlotUtils.indexToId(SlotUtils.MAIN_START);
        SettingToggle filter = getSetting(3).asToggle();

        SettingList<Item> listItem = filter.getChild(1).asList(Item.class);

        boolean isBlacklist = filter.getChild(0).asMode().getMode() == 0;

        int totalItems = 0;

        for (int i = playerInvOffset; i < playerInvOffset + 4 * 9; i++) {
            if (!screenHandler.getSlot(i).hasStack()) continue;
            Item currentItem = screenHandler.getSlot(i).getStack().getItem();
            if (currentItem == Items.AIR) {
                continue;
            }

            boolean shouldSkip = isBlacklist ? listItem.contains(currentItem) : !listItem.contains(currentItem);
            if (shouldSkip) continue;
            totalItems += screenHandler.getSlot(i).getStack().getCount();
            InvUtils.shiftClick().slotId(i);
        }

        BleachLogger.info("Dumped " + totalItems + " items");
    }

    private void closeInventory() {
//        mc.execute(() -> {
//            if (mc.currentScreen != null) {
//                mc.setScreen(null);
//            }
//        });
        if (mc.player != null && mc.player.currentScreenHandler != null) {
            mc.player.closeHandledScreen();
        }
    }
}
