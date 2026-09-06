package com.endiq.client.mixin;

//? if >=26.1 {
/*import net.minecraft.client.renderer.entity.state.EntityRenderState;
import com.endiq.client.compat.BadgeRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class BadgeRenderStateMixin implements BadgeRenderState, com.endiq.client.compat.CosmeticRenderState {
    @Unique private boolean turtleClient$badge;
    @Unique private com.endiq.client.cosmetics.CosmeticManager.Loadout turtleClient$cosmetics = com.endiq.client.cosmetics.CosmeticManager.Loadout.EMPTY;
    @Override public com.endiq.client.cosmetics.CosmeticManager.Loadout turtleClient$getCosmetics() { return turtleClient$cosmetics; }
    @Override public void turtleClient$setCosmetics(com.endiq.client.cosmetics.CosmeticManager.Loadout value) { turtleClient$cosmetics = value; }

    @Override public boolean turtleClient$hasBadge() { return turtleClient$badge; }
    @Override public void turtleClient$setBadge(boolean value) { turtleClient$badge = value; }
}
*///?} else if >=1.21.2 {
import net.minecraft.client.render.entity.state.EntityRenderState;
import com.endiq.client.compat.BadgeRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class BadgeRenderStateMixin implements BadgeRenderState, com.endiq.client.compat.CosmeticRenderState {
    @Unique private boolean turtleClient$badge;
    @Unique private com.endiq.client.cosmetics.CosmeticManager.Loadout turtleClient$cosmetics = com.endiq.client.cosmetics.CosmeticManager.Loadout.EMPTY;
    @Override public com.endiq.client.cosmetics.CosmeticManager.Loadout turtleClient$getCosmetics() { return turtleClient$cosmetics; }
    @Override public void turtleClient$setCosmetics(com.endiq.client.cosmetics.CosmeticManager.Loadout value) { turtleClient$cosmetics = value; }

    @Override public boolean turtleClient$hasBadge() { return turtleClient$badge; }
    @Override public void turtleClient$setBadge(boolean value) { turtleClient$badge = value; }
}
//?} else {
/*// Entity render states were introduced in 1.21.2.
*///?}
