package org.bleachhack.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import org.bleachhack.util.BleachLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ChatListenerMixin {

  private boolean isLogin = false;

  @Inject(method = "onGameMessage", at = @At("HEAD"))
  private void onChatMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
    String loginMessage = "Sử dụng lệnh '/L <mật khẩu đã đăng ký trước đó>' để đăng nhập. Sử dụng /pin để lấy lại mật khẩu nếu quên.";
    Text message = packet.content();

    if (message.getString().indexOf(loginMessage) != -1) {
      BleachLogger.info("Đã nhận được tin nhắn đăng nhập từ server.");
      sendPlayerMsg("/l abeesdev");
      isLogin = false;
    }

    if (message.getString().indexOf("Chào mừng bạn đã đến với GrassMineVN PE") != -1 && !isLogin) {
      isLogin = true;

      new Thread(() -> {
      try {
        Thread.sleep(1000);
        BleachLogger.info("Đang chọn server...");

        selectServer();
      } catch (InterruptedException e) {
        BleachLogger.info("Không thể chọn server.");
        e.printStackTrace();
      }
      }).start();
    }
  }

  private void selectServer() throws InterruptedException {
    MinecraftClient mc = MinecraftClient.getInstance();
    if (mc == null) {
      return;
    }

    BleachLogger.info("Đang chọn server...");
    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 36, 0, SlotActionType.PICKUP, mc.player);
    BleachLogger.info("Đang chọn cụm cày cuốc");
    Thread.sleep(1000);
    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 20, 0, SlotActionType.PICKUP, mc.player);
    BleachLogger.info("Đang chọn cụm dungeon rpg");
    Thread.sleep(1000);
    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 21, 0, SlotActionType.PICKUP, mc.player);
    Thread.sleep(1000);
    sendPlayerMsg("/autodrops");

  }

  public void sendPlayerMsg(String message) {
    MinecraftClient mc = MinecraftClient.getInstance();
    if (mc == null || mc.player == null) {
      return;
    }
    mc.inGameHud.getChatHud().addToMessageHistory(message);
    if (message.startsWith("/")) mc.player.networkHandler.sendChatCommand(message.substring(1));
    else mc.player.networkHandler.sendChatMessage(message);
  }
}
