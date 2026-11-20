/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  meteordevelopment.orbit.EventHandler
 *  net.minecraft.entity.Entity
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 *  net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.util.Formatting
 */
package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Formatting;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;

public class Flight
extends Module {
    private final Setting<Mode> mode = new Setting<Mode>("Mode", Mode.Vanilla);
    private final Setting<Float> hSpeed = new Setting<Float>("Horizontal", Float.valueOf(1.0f), Float.valueOf(0.0f), Float.valueOf(10.0f), v -> !this.mode.is(Mode.StormBreak));
    private final Setting<Float> vSpeed = new Setting<Float>("Vertical", Float.valueOf(0.78f), Float.valueOf(0.0f), Float.valueOf(5.0f), v -> !this.mode.is(Mode.StormBreak));
    private final Setting<Float> boostValue = new Setting<Float>("Boost", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(1.0f), v -> this.mode.is(Mode.StormBreak));
    private final Setting<Boolean> autoToggle = new Setting<Boolean>("AutoToggle", false, v -> this.mode.is(Mode.MatrixJump));
    private final Setting<Integer> boostTicks = new Setting<Integer>("Ticks", 8, 0, 40, v -> this.mode.is(Mode.Damage));
    private final Setting<Boolean> antiKick = new Setting<Boolean>("AntiKick", false, v -> this.mode.is(Mode.Creative) || this.mode.is(Mode.Vanilla));
    private double prevX;
    private double prevY;
    private double prevZ;
    private double velocityMotion;
    public boolean onPosLook = false;
    private int flyTicks = 0;

    public Flight() {
        super("Flight", Module.Category.MOVEMENT);
    }

    @EventHandler
    public void onEventSync(EventSync event) {
        switch (this.mode.getValue().ordinal()) {
            case 0: {
                if (MovementUtility.isMoving()) {
                    double[] dir = MovementUtility.forward(this.hSpeed.getValue().floatValue());
                    Flight.mc.player.setVelocity(dir[0], 0.0, dir[1]);
                } else {
                    Flight.mc.player.setVelocity(0.0, 0.0, 0.0);
                }
                if (Flight.mc.options.jumpKey.isPressed()) {
                    Flight.mc.player.setVelocity(Flight.mc.player.getVelocity().add(0.0, (double)this.vSpeed.getValue().floatValue(), 0.0));
                }
                if (!Flight.mc.options.sneakKey.isPressed()) break;
                Flight.mc.player.setVelocity(Flight.mc.player.getVelocity().add(0.0, (double)(-this.vSpeed.getValue().floatValue()), 0.0));
                break;
            }
            case 2: {
                if (!MovementUtility.isMoving() || !Flight.mc.world.getBlockCollisions((Entity)Flight.mc.player, Flight.mc.player.getBoundingBox().expand(0.5, 0.0, 0.5).offset(0.0, -1.0, 0.0)).iterator().hasNext()) break;
                Flight.mc.player.setOnGround(true);
                Flight.mc.player.jump();
                break;
            }
            case 3: {
                if (Flight.mc.player.isOnGround()) {
                    Flight.mc.player.jump();
                    this.flyTicks = 5;
                    break;
                }
                if (this.flyTicks <= 0) break;
                if (MovementUtility.isMoving()) {
                    double[] dir = MovementUtility.forward(this.hSpeed.getValue().floatValue());
                    Flight.mc.player.setVelocity(dir[0], -0.04, dir[1]);
                } else {
                    Flight.mc.player.setVelocity(0.0, -0.04, 0.0);
                }
                --this.flyTicks;
                break;
            }
            case 4: {
                if (Flight.mc.player.age % 60 != 0) break;
                this.sendMessage(String.valueOf(Formatting.RED) + (ClientSettings.isRu() ? "\u0412 \u044d\u0442\u043e\u043c \u0440\u0435\u0436\u0438\u043c\u0435 \u043d\u0443\u0436\u043d\u043e \u043b\u043e\u043c\u0430\u0442\u044c \u0431\u043b\u043e\u043a\u0438!" : "In this mode you need to break blocks!"));
            }
        }
        if (this.antiKick.getValue().booleanValue() && (this.mode.is(Mode.Creative) || this.mode.is(Mode.Vanilla))) {
            Flight.mc.player.setVelocity(Flight.mc.player.getVelocity().add(0.0, -0.08, 0.0));
        }
    }

    @Override
    public void onUpdate() {
        if (this.mode.is(Mode.Damage) && this.flyTicks-- > this.boostTicks.getValue()) {
            Flight.mc.player.setVelocity(Flight.mc.player.getVelocity().x, this.velocityMotion, Flight.mc.player.getVelocity().getZ());
        }
        if (this.mode.is(Mode.MatrixJump)) {
            if (Flight.mc.player.fallDistance == 0.0f) {
                return;
            }
            Flight.mc.player.getAbilities().flying = false;
            Flight.mc.player.setVelocity(0.0, 0.0, 0.0);
            if (Flight.mc.options.jumpKey.isPressed()) {
                Flight.mc.player.setVelocity(Flight.mc.player.getVelocity().add(0.0, (double)this.vSpeed.getValue().floatValue(), 0.0));
            }
            if (Flight.mc.options.sneakKey.isPressed()) {
                Flight.mc.player.setVelocity(Flight.mc.player.getVelocity().add(0.0, (double)(-this.vSpeed.getValue().floatValue()), 0.0));
            }
            double[] dir = MovementUtility.forward(this.hSpeed.getValue().floatValue());
            Flight.mc.player.setVelocity(dir[0], Flight.mc.player.getVelocity().getY(), dir[1]);
        }
        if (this.mode.is(Mode.Creative)) {
            Flight.mc.player.getAbilities().flying = true;
            Flight.mc.player.getAbilities().setFlySpeed(this.hSpeed.getValue().floatValue() / 10.0f);
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        Object t;
        if (this.mode.is(Mode.MatrixJump)) {
            if (Flight.fullNullCheck()) {
                return;
            }
            if (e.getPacket() instanceof PlayerPositionLookS2CPacket) {
                this.onPosLook = true;
                this.prevX = Flight.mc.player.getVelocity().getX();
                this.prevY = Flight.mc.player.getVelocity().getY();
                this.prevZ = Flight.mc.player.getVelocity().getZ();
            }
        }
        if (this.mode.is(Mode.Damage) && (t = e.getPacket()) instanceof EntityVelocityUpdateS2CPacket) {
            EntityVelocityUpdateS2CPacket v = (EntityVelocityUpdateS2CPacket)t;
            if (v.getVelocityY() / 8000.0 > 0.2) {
                this.velocityMotion = v.getVelocityY() / 8000.0;
                this.flyTicks = this.boostTicks.getValue();
            }
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        PlayerActionC2SPacket pac;
        Object t;
        if (this.mode.is(Mode.MatrixJump) && e.getPacket() instanceof PlayerMoveC2SPacket.Full && this.onPosLook) {
            Flight.mc.player.setVelocity(this.prevX, this.prevY, this.prevZ);
            this.onPosLook = false;
            if (this.autoToggle.getValue().booleanValue()) {
                this.disable();
            }
        }
        if (this.mode.is(Mode.StormBreak) && (t = e.getPacket()) instanceof PlayerActionC2SPacket && ((pac = (PlayerActionC2SPacket)t).getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK || pac.getAction() == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK && Flight.mc.world.getBlockState(pac.getPos()).isReplaceable())) {
            double[] dir = MovementUtility.forward(2.0f * this.boostValue.getValue().floatValue());
            Flight.mc.player.setVelocity(dir[0], (double)(3.0f * this.boostValue.getValue().floatValue()), dir[1]);
        }
    }

    @Override
    public void onDisable() {
        Flight.mc.player.getAbilities().flying = false;
        Flight.mc.player.getAbilities().setFlySpeed(0.05f);
    }

    private static enum Mode {
        Vanilla,
        MatrixJump,
        AirJump,
        MatrixGlide,
        StormBreak,
        Damage,
        Creative;

    }
}

