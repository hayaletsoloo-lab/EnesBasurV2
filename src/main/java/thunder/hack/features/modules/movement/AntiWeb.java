/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  meteordevelopment.orbit.EventHandler
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.CobwebBlock
 */
package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.CobwebBlock;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.EventCollision;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;

public class AntiWeb
extends Module {
    public static final Setting<Mode> mode = new Setting<Mode>("Mode", Mode.Solid);
    public static final Setting<Boolean> grim = new Setting<Boolean>("Grim", false, v -> mode.is(Mode.Ignore));
    public static final Setting<Float> timer = new Setting<Float>("Timer", Float.valueOf(20.0f), Float.valueOf(1.0f), Float.valueOf(50.0f), v -> mode.getValue() == Mode.Timer);
    public Setting<Float> speed = new Setting<Float>("Speed", Float.valueOf(0.3f), Float.valueOf(0.0f), Float.valueOf(10.0f), v -> mode.getValue() == Mode.Fly);
    private boolean timerEnabled = false;

    public AntiWeb() {
        super("AntiWeb", Module.Category.MOVEMENT);
    }

    @EventHandler
    public void onPlayerUpdate(PlayerUpdateEvent e) {
        if (Managers.PLAYER.isInWeb()) {
            if (mode.getValue() == Mode.Timer) {
                if (AntiWeb.mc.player.isOnGround()) {
                    ThunderHack.TICK_TIMER = 1.0f;
                } else {
                    ThunderHack.TICK_TIMER = timer.getValue().floatValue();
                    this.timerEnabled = true;
                }
            }
            if (mode.getValue() == Mode.Fly) {
                double[] dir = MovementUtility.forward(this.speed.getValue().floatValue());
                AntiWeb.mc.player.setVelocity(dir[0], 0.0, dir[1]);
                if (AntiWeb.mc.options.jumpKey.isPressed()) {
                    AntiWeb.mc.player.setVelocity(AntiWeb.mc.player.getVelocity().add(0.0, (double)this.speed.getValue().floatValue(), 0.0));
                }
                if (AntiWeb.mc.options.sneakKey.isPressed()) {
                    AntiWeb.mc.player.setVelocity(AntiWeb.mc.player.getVelocity().add(0.0, (double)(-this.speed.getValue().floatValue()), 0.0));
                }
            }
        } else if (this.timerEnabled) {
            this.timerEnabled = false;
            ThunderHack.TICK_TIMER = 1.0f;
        }
    }

    @EventHandler
    public void onCollide(EventCollision e) {
        if (e.getState().getBlock() instanceof CobwebBlock && mode.getValue() == Mode.Solid) {
            e.setState(Blocks.DIRT.getDefaultState());
        }
    }

    public static enum Mode {
        Timer,
        Solid,
        Ignore,
        Fly;

    }
}

