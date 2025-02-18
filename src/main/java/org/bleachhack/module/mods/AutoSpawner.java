package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.InvUtils;
import org.bleachhack.util.SlotUtils;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;


public class AutoSpawner extends Module {

  public AutoSpawner() {
    super("AutoSpawner", KEY_UNBOUND, ModuleCategory.MISC, "Automatically.");
  }

  private boolean isStating = false;

  @Override
	public void onEnable(boolean inWorld) {
    isStating = false;
    super.onEnable(inWorld);
    BleachLogger.info("AutoSpawner enabled");
	}

  @BleachSubscribe
  public void onTick(EventTick event) {
    if(isStating) {
      return;
    }

    isStating = true;
    BleachLogger.info("AutoSpawner starting");

    try {
      new Thread(()-> {
        try {
          stealItemsSpawner();
          Thread.sleep(500);
          stealItemsSpawner();
          Thread.sleep(500);
          craftItem();
          Thread.sleep(500);
          dumpNetheriteBlock();
          Thread.sleep(500);
          getExp();
          closeInventory();
          Thread.sleep(300000);
          isStating = false;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }).start();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }





  private void stealItemsSpawner() throws InterruptedException{
    simulateRightClickOnBlock();
    Thread.sleep(1000);
    clickStorage();
    Thread.sleep(1000);
    stealAllItems();
    Thread.sleep(1000);
    closeInventory();
    Thread.sleep(1000);
    sendPlayerMsg("/trash");
    Thread.sleep(1000);
    trashItemsWithoutNetherite();
    Thread.sleep(1000);
  }

  private void clickStorage() {
    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 13, 0, SlotActionType.PICKUP, mc.player);
  }

  private void stealAllItems() {
    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 51, 0, SlotActionType.QUICK_MOVE, mc.player);
  }

  private void closeInventory() {
    mc.options.inventoryKey.setPressed(true);
    mc.options.inventoryKey.setPressed(false);
  }

  private void trashItemsWithoutNetherite() throws InterruptedException {
      if (mc.player == null || mc.interactionManager == null) return;
      int playerInvOffset = SlotUtils.indexToId(SlotUtils.MAIN_START);
      ScreenHandler screenHandler = mc.player.currentScreenHandler;

      for (int i = playerInvOffset; i < playerInvOffset + 4 * 9; i++) {

          if(!screenHandler.getSlot(i).hasStack()) continue;
          Item item = screenHandler.getSlot(i).getStack().getItem();

          if (item != Items.BONE && item != Items.WITHER_SKELETON_SKULL && item != Items.COAL) continue;
          try {
            Thread.sleep(100);
            InvUtils.shiftClick().slotId(i);
          } catch (Exception e) {
              e.printStackTrace();
              BleachLogger.error("Error while moving item to storage: " + item.getName().getString());
              closeInventory();
          }
      }
      Thread.sleep(100);
      closeInventory();
  }

  private void dumpNetheriteBlock() throws InterruptedException {
      sendPlayerMsg("/pv 1");

      Thread.sleep(500);

      if (mc.player == null || mc.interactionManager == null) return;
      int playerInvOffset = SlotUtils.indexToId(SlotUtils.MAIN_START);
      ScreenHandler screenHandler = mc.player.currentScreenHandler;

      for (int i = playerInvOffset; i < playerInvOffset + 4 * 9; i++) {

          if(!screenHandler.getSlot(i).hasStack()) continue;
          Item item = screenHandler.getSlot(i).getStack().getItem();

          if (item != Items.NETHERITE_BLOCK) continue;
          try {
            Thread.sleep(100);
            InvUtils.shiftClick().slotId(i);
          } catch (Exception e) {
              e.printStackTrace();
              BleachLogger.error("Error while moving item to storage: " + item.getName().getString());
              closeInventory();
          }
      }

      Thread.sleep(100);
      closeInventory();
  }

  private void simulateRightClickOnBlock() {
      ClientPlayerEntity player = mc.player;
      if (player != null && mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
        BlockHitResult hitResult = (BlockHitResult) mc.crosshairTarget;
        int side = hitResult.getSide().getId(); // Side of the block that was hit
        ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();
        networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, side));
    }
  }

  private void craftItem() {
    sendPlayerMsg("/craft");
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    closeInventory();
  }

  private void getExp() {
    simulateRightClickOnBlock();
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 15, 0, SlotActionType.PICKUP, mc.player);
  }

  public void sendPlayerMsg(String message) {
    if (mc == null || mc.player == null) {
      return;
    }
    mc.inGameHud.getChatHud().addToMessageHistory(message);
    if (message.startsWith("/")) mc.player.networkHandler.sendChatCommand(message.substring(1));
    else mc.player.networkHandler.sendChatMessage(message);
  }
}