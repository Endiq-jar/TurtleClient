package com.endiq.client.mixin;

//? if >=26.1 {
/*import net.minecraft.client.renderer.entity.state.EntityRenderState;
import com.endiq.client.compat.BadgeRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class BadgeRenderStateMixin implements BadgeRenderState, com.endiq.client.compat.CapeRenderState {
    @Unique private boolean turtleClient$badge;
    @Unique private com.endiq.client.capes.CapeManager.Outfit turtleClient$cape = com.endiq.client.capes.CapeManager.Outfit.EMPTY;
    @Override public com.endiq.client.capes.CapeManager.Outfit turtleClient$getCape() { return turtleClient$cape; }
    @Override public void turtleClient$setCape(com.endiq.client.capes.CapeManager.Outfit value) { turtleClient$cape = value; }

    @Override public boolean turtleClient$hasBadge() { return turtleClient$badge; }
    @Override public void turtleClient$setBadge(boolean value) { turtleClient$badge = value; }
}
*///?} else if >=1.21.2 {
import net.minecraft.client.render.entity.state.EntityRenderState;
import com.endiq.client.compat.BadgeRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class BadgeRenderStateMixin implements BadgeRenderState, com.endiq.client.compat.CapeRenderState {
    @Unique private boolean turtleClient$badge;
    @Unique private com.endiq.client.capes.CapeManager.Outfit turtleClient$cape = com.endiq.client.capes.CapeManager.Outfit.EMPTY;
    @Override public com.endiq.client.capes.CapeManager.Outfit turtleClient$getCape() { return turtleClient$cape; }
    @Override public void turtleClient$setCape(com.endiq.client.capes.CapeManager.Outfit value) { turtleClient$cape = value; }

    @Override public boolean turtleClient$hasBadge() { return turtleClient$badge; }
    @Override public void turtleClient$setBadge(boolean value) { turtleClient$badge = value; }
}
//?} else {
/*// Entity render states were introduced in 1.21.2.
*///?}
