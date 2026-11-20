/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.ClientPlayerEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package thunder.hack.injection;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.features.modules.movement.ElytraBooster;

@Mixin(value={ClientPlayerEntity.class})
public class MixinElytraBooster {
    @Inject(method={"tickMovement"}, at={@At(value="HEAD")})
    private void onTickMovement(CallbackInfo info) {
        ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;
        if (!ElytraBooster.getInstance().isEnabled()) {
            return;
        }
        if (player.isFallFlying()) {
            player.input.movementForward = 0.0f;
            player.input.movementSideways = 0.0f;
        }
    }
}

