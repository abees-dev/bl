package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingBlockList;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingToggle;

import net.minecraft.client.MinecraftClient;
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
      new Vec3d(729.768, 143.50000, -1012.366),
      new Vec3d(729.499, 143.50000, -984.492),
      new Vec3d(701.468, 143.50000, -984.420),
      new Vec3d(701.384, 143.50000, -1012.417));

  private final List<Vec3d> lapisLocations = Arrays.asList(
      new Vec3d(729.594, 143.50000, -1084.665),
      new Vec3d(729.781, 143.50000, -1056.513),
      new Vec3d(701.287, 143.50000, -1056.465),
      new Vec3d(701.697, 143.50000, -1084.202));

  private int currentTargetIndex = 0;
  private final double reachDistance = 0.2; // Khoảng cách chấp nhận khi đến đích

  public AutoMinePrison() {
    super("AutoMinePrison", KEY_UNBOUND, ModuleCategory.PLAYER, "Automatically mines in prison.",
        new SettingMode("Warp", "Diamond", "Restone", "Lapis").withDesc("The warp to mine."),
        new SettingToggle("Diamond", true).withDesc("Mine diamond."));
  }

  @BleachSubscribe
  public void onTick(EventTick event) {
    if (mc.player == null || mc.player.getWorld() == null)
      return;

    List<Vec3d> locations = getLocations();

    Vec3d targetPos = locations.get(currentTargetIndex);
    Vec3d playerPos = mc.player.getPos();

    // Nếu đã đến gần tọa độ mục tiêu, chuyển sang tọa độ tiếp theo
    if (playerPos.squaredDistanceTo(targetPos) <= reachDistance * reachDistance) {
      currentTargetIndex = (currentTargetIndex + 1) % locations.size();
      targetPos = locations.get(currentTargetIndex);
    }

    // Di chuyển đến tọa độ tiếp theo
    moveTo(targetPos);

    // Bật chế độ sneak
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
    if (dy > 0.5) { // Nếu vị trí tiếp theo cao hơn, thực hiện nhảy
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
    if (getSetting(0).asMode().getMode() == 0) {
      return diamondLocations;
    } else if (getSetting(0).asMode().getMode() == 1) {
      return redstoneLocations;
    } else if (getSetting(0).asMode().getMode() == 2) {
      return lapisLocations;
    } else {
      return diamondLocations;
    }
  }
}
