package com.endiq.client.mixin;

//? if <1.19.4 {
/*import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface LegacyFpsAccessor {
    @Accessor("currentFps")
    static int turtleClient$getCurrentFps() {
        throw new IllegalStateException("MinecraftClient FPS accessor was not applied");
    }
}
*///?}
