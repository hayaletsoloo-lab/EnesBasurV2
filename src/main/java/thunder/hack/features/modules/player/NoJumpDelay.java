/*
 * Decompiled with CFR 0.152.
 */
package thunder.hack.features.modules.player;

import thunder.hack.features.modules.Module;
import thunder.hack.injection.accesors.ILivingEntity;
import thunder.hack.setting.Setting;

public class NoJumpDelay
extends Module {
    private final Setting<Integer> delay = new Setting<Integer>("Delay", 1, 0, 4);

    public NoJumpDelay() {
        super("NoJumpDelay", Module.Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (((ILivingEntity)NoJumpDelay.mc.player).getLastJumpCooldown() > this.delay.getValue()) {
            ((ILivingEntity)NoJumpDelay.mc.player).setLastJumpCooldown(this.delay.getValue());
        }
    }
}

