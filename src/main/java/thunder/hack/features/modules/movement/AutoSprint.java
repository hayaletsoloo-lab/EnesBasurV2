/*
 * Decompiled with CFR 0.152.
 */
package thunder.hack.features.modules.movement;

import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.setting.Setting;

public class AutoSprint
extends Module {
    public static final Setting<Boolean> sprint = new Setting<Boolean>("KeepSprint", true);
    public static final Setting<Float> motion = new Setting<Float>("Motion", Float.valueOf(1.0f), Float.valueOf(0.0f), Float.valueOf(1.0f), v -> sprint.getValue());
    private final Setting<Boolean> stopWhileUsing = new Setting<Boolean>("StopWhileUsing", false);
    private final Setting<Boolean> pauseWhileAura = new Setting<Boolean>("PauseWhileAura", false);

    public AutoSprint() {
        super("AutoSprint", Module.Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        AutoSprint.mc.player.setSprinting(!(AutoSprint.mc.player.getHungerManager().getFoodLevel() <= 6 || AutoSprint.mc.player.horizontalCollision || !(AutoSprint.mc.player.input.movementForward > 0.0f) || AutoSprint.mc.player.isSneaking() || AutoSprint.mc.player.isUsingItem() && this.stopWhileUsing.getValue() != false || ModuleManager.aura.isEnabled() && Aura.target != null && this.pauseWhileAura.getValue() != false));
    }
}

