package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingBlockList;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.ChatUtill;

import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

public class AutoMinePrison extends Module {
  private final MinecraftClient mc = MinecraftClient.getInstance();
  private final List<Vec3d> diamondLocations = Arrays.asList(
      new Vec3d(729.405, 143.0625, -868.528),
      new Vec3d(701.501, 143.0625, -868.567),
      new Vec3d(701.462, 143.0625, -840.500),
      new Vec3d(729.747, 143.0625, -840.678));
  private final List<Vec3d> redstoneLocations = Arrays.asList(
      new Vec3d(700, 143, -1014.058),
      new Vec3d(730, 143, -1014.058),
      new Vec3d(700, 143, -1044.058),
      new Vec3d(730, 143, -1044.058));;

  private final List<Vec3d> lapisLocations = Arrays.asList(
      new Vec3d(700.157, 143, -1085.893),
      new Vec3d(730.157, 143, -1085.893),
      new Vec3d(700.157, 143, -1055.893),
      new Vec3d(730.157, 143, -1055.893));

  private final List<Vec3d> goldLocations = Arrays.asList(
      new Vec3d(701.653, 143.0625, -1156.1156),
      new Vec3d(701.653, 143.0625, -1128.1156),
      new Vec3d(729.653, 143.0625, -1128.1156),
      new Vec3d(729.653, 143.0625, -1156.1156));

  private final List<Vec3d> ironLocations = Arrays.asList(
      new Vec3d(701.204, 143, -1228.482),
      new Vec3d(701.204, 143, -1200.482),
      new Vec3d(729.204, 143, -1200.482),
      new Vec3d(729.204, 143, -1228.482));

  private final List<Vec3d> copperLocations = Arrays.asList(
      new Vec3d(702.419, 143, -795.469),
      new Vec3d(728.419, 143, -795.469),
      new Vec3d(702.419, 143, -769.469),
      new Vec3d(728.419, 143, -769.469));

  private final List<Vec3d> coalLocations = Arrays.asList(
      new Vec3d(700, 143, -1302.173),
      new Vec3d(730, 143, -1302.173),
      new Vec3d(700, 143, -1272.173),
      new Vec3d(730, 143, -1272.173));

  private final List<Vec3d> stoneLocations = Arrays.asList(
      new Vec3d(728.459, 143.0, -291.369),
      new Vec3d(702.523, 143, -291.195),
      new Vec3d(702.523, 143, -265.620),
      new Vec3d(728.010, 143, -265.725));

  private final Vec3d spawnLocationVec3d = new Vec3d(722.500, 114.0000, -2096.500);

  private final int ZoneOneSlotInv = 10;
  private final int ZoneTwoSlotInv = 12;
  private final int ZoneThreeSlotInv = 14;

  private final List<Integer> ZoneOneSlot = Arrays.asList(10, 11, 12, 13, 14, 19, 20, 21, 22);

  private int currentTargetIndex = 0;
  private final double reachDistance = 0.2; // Khoảng cách chấp nhận khi đến đích
  private int timer = 0;
  private int stepSelectZone = 0;
  private final int deylay = 10;

  public AutoMinePrison() {
    super("AutoMinePrison", KEY_UNBOUND, ModuleCategory.PLAYER, "Automatically mines in prison.",
        new SettingMode("Warp", "Stone", "Coal", "Copper", "Iron", "Gold", "Lapis", "Restone", "Diamond")
            .withDesc("The warp to mine."),
        new SettingToggle("Diamond", true).withDesc("Mine diamond."));
  }

  @BleachSubscribe
  public void onTick(EventTick event) {
    if (mc.player == null || mc.player.getWorld() == null)
      return;

    List<Vec3d> locations = getLocations();

    Vec3d targetPos = locations.get(currentTargetIndex);
    Vec3d playerPos = mc.player.getPos();

    if (playerPos.squaredDistanceTo(spawnLocationVec3d) <= 100) {
      selectZone();
      return;
    }

    if (Math.sqrt(playerPos.squaredDistanceTo(targetPos)) > 75) {
      selectZone();
      return;
    }

    if (playerPos.squaredDistanceTo(targetPos) <= reachDistance * reachDistance) {
      currentTargetIndex = (currentTargetIndex + 1) % locations.size();
      targetPos = locations.get(currentTargetIndex);
    }

    moveTo(targetPos);

    mc.options.sneakKey.setPressed(true);
  }

  private void moveTo(Vec3d targetPos) {
    if (mc.player == null)
      return;

    double dx = targetPos.x - mc.player.getX();
    double dz = targetPos.z - mc.player.getZ();
    double dy = targetPos.y - mc.player.getY(); // Tính độ cao cần di chuyển
    double speed = 0.25; // Tốc độ di chuyển
    double jumpBoost = 0.42; // Lực nhảy (giống Minecraft bình thường)

    double length = Math.sqrt(dx * dx + dz * dz);
    if (length > 0) {
      dx /= length;
      dz /= length;
    }

    double velocityY = mc.player.getVelocity().y;
    double playerY = mc.player.getY();
    if (dy - playerY > 0.5) {
      velocityY = jumpBoost;
    }

    mc.player.setVelocity(dx * speed, velocityY, dz * speed);
  }

  @Override
  public void onDisable(boolean inWorld) {
    super.onDisable(inWorld);
    // Tắt chế độ sneak
    mc.options.sneakKey.setPressed(false);
  }

  private List<Vec3d> getLocations() {
    if (getSetting(0).asMode().getMode() == 0)
      return stoneLocations;
    if (getSetting(0).asMode().getMode() == 1)
      return coalLocations;
    if (getSetting(0).asMode().getMode() == 2)
      return copperLocations;
    if (getSetting(0).asMode().getMode() == 3)
      return ironLocations;
    if (getSetting(0).asMode().getMode() == 4)
      return goldLocations;
    if (getSetting(0).asMode().getMode() == 5)
      return lapisLocations;
    if (getSetting(0).asMode().getMode() == 6)
      return redstoneLocations;
    if (getSetting(0).asMode().getMode() == 7)
      return diamondLocations;
    return stoneLocations;
  }

  private void selectZone() {
    switch (stepSelectZone) {
      case 0:
        ChatUtill.sendPlayerMsg("/warp mine");
        stepSelectZone++;
        timer = 0;
        break;
      case 1:
        if (timer++ >= deylay) {
          mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, ZoneOneSlotInv, 0,
              SlotActionType.PICKUP, mc.player);
          stepSelectZone++;
          timer = 0;
        }
        break;
      case 2:
        if (timer++ >= deylay) {
          mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
              ZoneOneSlot.get(getSetting(0).asMode().getMode()), 0, SlotActionType.PICKUP, mc.player);
          stepSelectZone++;
          timer = 0;
        }
        break;
      case 3:
        if (timer++ >= deylay) {
          stepSelectZone = 0;
          timer = 0;
        }
        break;
    }
  }
}
