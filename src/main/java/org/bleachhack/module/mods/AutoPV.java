package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.ChatUtill;
import org.bleachhack.util.InvUtils;
import org.bleachhack.util.SlotUtils;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;

public class AutoPV extends Module {
  private static final Item[] ORES_ITEMS = {
      Items.STONE, Items.COAL_BLOCK, Items.COPPER_BLOCK, Items.IRON_BLOCK,
      Items.GOLD_BLOCK, Items.LAPIS_BLOCK, Items.REDSTONE_BLOCK, Items.DIAMOND_BLOCK, Items.EMERALD_BLOCK,
      Items.PACKED_ICE, Items.PRISMARINE_SHARD, Items.CYAN_DYE, Items.SMALL_AMETHYST_BUD, Items.CALCITE
  };

  private int pv = 1;
  private int timer = 0;
  private static final int DELAY = 200;
  private int delayTimer = 0; // Bộ đếm delay riêng biệt

  public AutoPV() {
    super("AutoPV", -1, ModuleCategory.PLAYER, "Automatically dumps your inventory into a chest",
        new SettingSlider("PV", 1, 50, 1, 0).withDesc("The setting max pv for player"),
        new SettingSlider("Default PV", 1, 50, 5, 0).withDesc("The setting default pv for player"),
        new SettingSlider("Delay", 1, 60, 1, 0).withDesc("The setting delay for open inventory (munites)"));
  }

  private int step = 0;

  @Override
  public void onEnable(boolean inWorld) {
    super.onEnable(inWorld);
    pv = (int) getSetting(1).asSlider().getValue().doubleValue();
    ChatUtill.sendPlayerMsg("AutoPV enabled");
  }

  @BleachSubscribe
  public void onTick(EventTick event) {
    int settingMode = AutoMinePrison.settingMode;
    if (settingMode == -1 || timer++ < DELAY || AutoMinePrison.isSelectZone) {
      return;
    }

    int delaySetting = (int) getSetting(2).asSlider().getValue().doubleValue();

    int delay = 60 * 20 * delaySetting; // default 1 phút

    if (delayTimer++ < delay) { // Delay 1 giây (20 ticks)
      return;
    }

    timer = 0;
    delayTimer = 0; // Reset bộ đếm delay

    Item item = ORES_ITEMS[settingMode];

    if (step == 0) {
      ChatUtill.sendPlayerMsg("/pv " + pv);
      step++;
      delayTimer = delay - 20;
      return;
    }

    if (step == 1) {
      BleachLogger.info("Step 1");
      if (isFullPV()) {
        step = 0;
        pv++;
        closeInventory();
        delayTimer = delay - 5;
        return;
      } else {
        step++;
        delayTimer = delay - 5;
        return;
      }
    }

    if (step == 2) {
      BleachLogger.info("Step 2");
      dumpItem(item);
      closeInventory();
      step = 0;
      delayTimer = 0;
    }
  }

  private boolean isFullPV() {
    ScreenHandler screenHandler = mc.player.currentScreenHandler;
    for (int i = 0; i < SlotUtils.indexToId(SlotUtils.MAIN_START); i++) {
      if (screenHandler.getSlot(i).getStack().getItem() == Items.AIR) {
        return false;
      }
    }
    return true;
  }

  private void dumpItem(Item item) {
    ScreenHandler screenHandler = mc.player.currentScreenHandler;
    int playerInvOffset = SlotUtils.indexToId(SlotUtils.MAIN_START);
    for (int i = playerInvOffset; i < playerInvOffset + 4 * 9; i++) {
      if (screenHandler.getSlot(i).getStack().getItem() == item) {
        InvUtils.shiftClick().slotId(i);
        BleachLogger.info("Move item to storage: " + item.getName().getString());
      }

    }
    closeInventory();
  }

  private void closeInventory() {
    if (mc.player != null && mc.player.currentScreenHandler != null) {
      mc.player.closeHandledScreen();
    }
  }
}
