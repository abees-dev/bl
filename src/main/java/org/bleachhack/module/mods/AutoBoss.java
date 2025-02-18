package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.ChatUtill;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class AutoBoss extends Module {
  private static Vec3d targetPos = null;
  private static final double MOVE_SPEED = 0.4; // Increased speed
  private static boolean isGoToDao1 = false;

  private static final Vec3d TARGET_POS = new Vec3d(1256.510, 83.0, 941.618);
  private static final Vec3d HOUSE_POS = new Vec3d(1280.824, 79.0, 927.360);

  public AutoBoss() {
    super("AutoBoss", KEY_UNBOUND, ModuleCategory.PLAYER, "Automatically move to position with jumping and NoClip");
  }

  public static void startMovingTo(Vec3d pos) {
    targetPos = pos;
  }

  @BleachSubscribe
  public void onTick(EventTick event) {
    ClientPlayerEntity player = mc.player;

    if (player != null) {
      Vec3d playerPos = player.getPos();
      double distanceToTarget = playerPos.squaredDistanceTo(TARGET_POS);
      double distanceToHouse = playerPos.squaredDistanceTo(HOUSE_POS);

      if (targetPos != null) {
        moveToTarget(player, playerPos);
      }

      if (distanceToHouse < 20) {
        handleHouseWarp();
      }

      if (distanceToTarget > 9 && distanceToHouse > 20 && isGoToDao1 == false) {
        startMovingTo(TARGET_POS);
      }
    }
  }

  private void moveToTarget(ClientPlayerEntity player, Vec3d playerPos) {
    double distance = playerPos.squaredDistanceTo(targetPos);

    if (distance > 2) { // Keep moving until close enough
      Vec3d direction = targetPos.subtract(playerPos).normalize();
      player.setYaw((float) Math.toDegrees(Math.atan2(-direction.x, direction.z))); // Adjust yaw to face target
      player.setVelocity(direction.x * MOVE_SPEED, player.getVelocity().y, direction.z * MOVE_SPEED); // Move towards target
      player.input.pressingForward = true;

      if (player.isOnGround()) {
        player.jump(); // Jump when on the ground
      }
    } else {
      targetPos = null; // Stop when close enough
      player.input.pressingForward = false;
    }
  }

  private void handleHouseWarp() {
    if (!isGoToDao1) {
      ChatUtill.sendPlayerMsg("/warp dao1");
      isGoToDao1 = true;
      BleachLogger.info("Đang chuyển đến đảo 1");

      new Thread(() -> {
        try {
          Thread.sleep(4000);
          isGoToDao1 = false;
          BleachLogger.info("Đã chuyển đến đảo 1");
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }).start();
    }
  }
}
