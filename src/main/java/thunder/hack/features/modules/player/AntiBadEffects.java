/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.effect.StatusEffects
 */
package thunder.hack.features.modules.player;

import net.minecraft.entity.effect.StatusEffects;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class AntiBadEffects
extends Module {
    private final Setting<Boolean> blindness = new Setting<Boolean>("Blindness", true);
    private final Setting<Boolean> nausea = new Setting<Boolean>("Nausea", true);
    private final Setting<Boolean> miningFatigue = new Setting<Boolean>("MiningFatigue", true);
    private final Setting<Boolean> levitation = new Setting<Boolean>("Levitation", true);
    private final Setting<Boolean> slowness = new Setting<Boolean>("Slowness", true);
    private final Setting<Boolean> jumpBoost = new Setting<Boolean>("JumpBoost", true);

    public AntiBadEffects() {
        super("AntiBadEffects", Module.Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (AntiBadEffects.mc.player.hasStatusEffect(StatusEffects.BLINDNESS) && this.blindness.getValue().booleanValue()) {
            AntiBadEffects.mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
        }
        if (AntiBadEffects.mc.player.hasStatusEffect(StatusEffects.NAUSEA) && this.nausea.getValue().booleanValue()) {
            AntiBadEffects.mc.player.removeStatusEffect(StatusEffects.NAUSEA);
        }
        if (AntiBadEffects.mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE) && this.miningFatigue.getValue().booleanValue()) {
            AntiBadEffects.mc.player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
        }
        if (AntiBadEffects.mc.player.hasStatusEffect(StatusEffects.LEVITATION) && this.levitation.getValue().booleanValue()) {
            AntiBadEffects.mc.player.removeStatusEffect(StatusEffects.LEVITATION);
        }
        if (AntiBadEffects.mc.player.hasStatusEffect(StatusEffects.SLOWNESS) && this.slowness.getValue().booleanValue()) {
            AntiBadEffects.mc.player.removeStatusEffect(StatusEffects.SLOWNESS);
        }
        if (AntiBadEffects.mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) && this.jumpBoost.getValue().booleanValue()) {
            AntiBadEffects.mc.player.removeStatusEffect(StatusEffects.JUMP_BOOST);
        }
    }
}

