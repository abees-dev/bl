package org.bleachhack.util;

import net.minecraft.client.MinecraftClient;

public class ChatUtill {
  private static final MinecraftClient mc = MinecraftClient.getInstance();
  public static void sendPlayerMsg(String message) {
    if (mc == null || mc.player == null) {
      return;
    }
    mc.inGameHud.getChatHud().addToMessageHistory(message);
    if (message.startsWith("/")) mc.player.networkHandler.sendChatCommand(message.substring(1));
    else mc.player.networkHandler.sendChatMessage(message);
  }
}
