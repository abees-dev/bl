package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingBlockList;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.ChatUtill;
import org.bleachhack.util.shader.LocationsPrision;

import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

public class AutoMinePrison extends Module {
  private final MinecraftClient mc = MinecraftClient.getInstance();

  private final Vec3d spawnLocationVec3d = new Vec3d(722.500, 114.0000, -2096.500);

  private final int ZoneOneSlotInv = 10;
  private final int ZoneTwoSlotInv = 12;
  private final int ZoneThreeSlotInv = 14;

  private final List<Integer> ZoneOneSlot = Arrays.asList(10, 11, 12, 13, 14, 19, 20, 21, 22);

  public static int settingMode = -1;
  public static boolean isSelectZone = false;

  private int currentTargetIndex = 0;
  private final double reachDistance = 0.2; // Khoảng cách chấp nhận khi đến đích
  private int timer = 0;
  private int timeDeylaySelectZone = 0;
  private int stepSelectZone = 0;
  private final int deylay = 10;

  public AutoMinePrison() {
    super("AutoMinePrison", KEY_UNBOUND, ModuleCategory.PLAYER, "Automatically mines in prison.",
        new SettingMode("Warp", "Stone", "Coal", "Copper", "Iron", "Gold", "Lapis", "Restone", "Diamond", "Emerald",
            "BlueIce", "Prismane", "Blue Stone", "Amethyst Block", "Bone Block").withDesc("The warp to mine."),
        new SettingToggle("Diamond", true).withDesc("Mine diamond."));
  }

  @BleachSubscribe
  public void onTick(EventTick event) {
    if (mc.player == null || mc.player.getWorld() == null)
      return;

    settingMode = getSetting(0).asMode().getMode();

    List<Vec3d> locations = getLocations();

    Vec3d targetPos = locations.get(currentTargetIndex);
    Vec3d playerPos = mc.player.getPos();

    if (playerPos.squaredDistanceTo(spawnLocationVec3d) <= 100) {
      selectZone();
      isSelectZone = true;
      return;
    }

    if (Math.sqrt(playerPos.squaredDistanceTo(targetPos)) > 75) {
      isSelectZone = true;
      selectZone();
      return;
    }

    isSelectZone = false;

    if (playerPos.squaredDistanceTo(targetPos) <= reachDistance * reachDistance) {
      currentTargetIndex = (currentTargetIndex + 1) % locations.size();
      targetPos = locations.get(currentTargetIndex);
    }

    moveTo(targetPos);

  }

  private void moveTo(Vec3d targetPos) {
    if (mc.player == null)
      return;

    double dx = targetPos.x - mc.player.getX();
    double dz = targetPos.z - mc.player.getZ();
    double speed = 0.15; // Tốc độ di chuyển
    double jumpBoost = 0.42; // Lực nhảy (giống Minecraft bình thường)

    double length = Math.sqrt(dx * dx + dz * dz);
    if (length > 0) {
      dx /= length;
      dz /= length;
    }

    double velocityY = mc.player.getVelocity().y;
    double playerY = mc.player.getY();
    if (playerY < targetPos.y) {
      // Nếu độ cao của người chơi nhỏ hơn độ cao của mục tiêu, tăng lực nhảy
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
    switch (getSetting(0).asMode().getMode()) {
      case 0:
        return LocationsPrision.STONE_LOCATIONS;
      case 1:
        return LocationsPrision.COAL_LOCATIONS;
      case 2:
        return LocationsPrision.COPPER_LOCATIONS;
      case 3:
        return LocationsPrision.IRON_LOCATIONS;
      case 4:
        return LocationsPrision.GOLD_LOCATIONS;
      case 5:
        return LocationsPrision.LAPIS_LOCATIONS;
      case 6:
        return LocationsPrision.REDSTONE_LOCATIONS;
      case 7:
        return LocationsPrision.DIAMOND_LOCATIONS;
      case 8:
        return LocationsPrision.EMERALD_LOCATIONS;
      case 9:
        return LocationsPrision.BLUE_ICE_LOCATIONS;
      case 10:
        return LocationsPrision.PRISMANE_LOCATIONS;
      case 11:
        return LocationsPrision.BLUE_STONE_LOCATIONS;
      case 12:
        return LocationsPrision.AMETHYST_BLOCK_LOCATIONS;
      case 13:
        return LocationsPrision.BONE_BLCCK_LOCATIONS;
      default:
        return LocationsPrision.STONE_LOCATIONS;
    }
  }

  private void selectZone() {
    if (timeDeylaySelectZone++ < 100) {
      return;
    }

    int zoneIndex = getSetting(0).asMode().getMode();
    if (zoneIndex >= 9) {
      zoneIndex = getSetting(0).asMode().getMode() - 9;
    } else {
      zoneIndex = getSetting(0).asMode().getMode();
    }

    int slotInv = getSlotInv();

    switch (stepSelectZone) {
      case 0:
        ChatUtill.sendPlayerMsg("/warp mine");
        stepSelectZone++;
        timer = 0;
        break;
      case 1:
        if (timer++ >= deylay) {
          mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slotInv, 0,
              SlotActionType.PICKUP, mc.player);
          stepSelectZone++;
          timer = 0;
        }
        break;
      case 2:
        if (timer++ >= deylay) {
          mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
              ZoneOneSlot.get(zoneIndex), 0, SlotActionType.PICKUP, mc.player);
          stepSelectZone++;
          timer = 0;
        }
        break;
      case 3:
        if (timer++ >= deylay) {
          stepSelectZone = 0;
          timer = 0;
          timeDeylaySelectZone = 0;

        }
        break;
    }
  }

  private int getSlotInv() {
    if (getSetting(0).asMode().getMode() < 9) {
      return ZoneOneSlotInv;
    }
    return ZoneTwoSlotInv;
  }
}
