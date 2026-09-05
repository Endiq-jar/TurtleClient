package com.endiq.client.mixin;

//? if <1.19 {
/*import com.endiq.client.TurtleClientClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class LegacyChatMixin {
    @Inject(method = "onGameMessage", at = @At("TAIL"))
    private void turtleClient$receiveMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        TurtleClientClient.onGameMessage(packet.getMessage().getString());
    }
}
*///?}
