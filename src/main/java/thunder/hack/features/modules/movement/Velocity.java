/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  meteordevelopment.orbit.EventHandler
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$PositionAndOnGround
 *  net.minecraft.network.packet.s2c.common.CommonPingS2CPacket
 *  net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
 *  net.minecraft.network.packet.s2c.play.ExplosionS2CPacket
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Position
 */
package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.injection.accesors.IExplosionS2CPacket;
import thunder.hack.injection.accesors.ISPacketEntityVelocity;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;

public class Velocity
extends Module {
    public Setting<Boolean> onlyAura = new Setting<Boolean>("OnlyDuringAura", false);
    public Setting<Boolean> pauseInWater = new Setting<Boolean>("PauseInLiquids", false);
    public Setting<Boolean> explosions = new Setting<Boolean>("Explosions", true);
    public Setting<Boolean> cc = new Setting<Boolean>("PauseOnFlag", false);
    public Setting<Boolean> fire = new Setting<Boolean>("PauseOnFire", false);
    private final Setting<modeEn> mode = new Setting<modeEn>("Mode", modeEn.Matrix);
    public Setting<Float> vertical = new Setting<Float>("Vertical", Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(100.0f), v -> this.mode.getValue() == modeEn.Custom);
    private final Setting<jumpModeEn> jumpMode = new Setting<jumpModeEn>("JumpMode", jumpModeEn.Jump, v -> this.mode.getValue() == modeEn.Jump);
    public Setting<Float> horizontal = new Setting<Float>("Horizontal", Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(100.0f), v -> this.mode.getValue() == modeEn.Custom || this.mode.getValue() == modeEn.Jump);
    public Setting<Float> motion = new Setting<Float>("Motion", Float.valueOf(0.42f), Float.valueOf(0.4f), Float.valueOf(0.5f), v -> this.mode.getValue() == modeEn.Jump);
    public Setting<Boolean> fail = new Setting<Boolean>("SmartFail", true, v -> this.mode.getValue() == modeEn.Jump);
    public Setting<Float> failRate = new Setting<Float>("FailRate", Float.valueOf(0.3f), Float.valueOf(0.0f), Float.valueOf(1.0f), v -> this.mode.getValue() == modeEn.Jump && this.fail.getValue() != false);
    public Setting<Float> jumpRate = new Setting<Float>("FailJumpRate", Float.valueOf(0.25f), Float.valueOf(0.0f), Float.valueOf(1.0f), v -> this.mode.getValue() == modeEn.Jump && this.fail.getValue() != false);
    private boolean doJump;
    private boolean failJump;
    private boolean skip;
    private boolean flag;
    private int grimTicks;
    private int ccCooldown;

    public Velocity() {
        super("Velocity", Module.Category.MOVEMENT);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        EntityVelocityUpdateS2CPacket pac;
        if (Velocity.fullNullCheck()) {
            return;
        }
        if (Velocity.mc.player != null && (Velocity.mc.player.isTouchingWater() || Velocity.mc.player.isSubmergedInWater() || Velocity.mc.player.isInLava()) && this.pauseInWater.getValue().booleanValue()) {
            return;
        }
        if (Velocity.mc.player != null && Velocity.mc.player.isOnFire() && this.fire.getValue().booleanValue() && Velocity.mc.player.hurtTime > 0) {
            return;
        }
        if (this.ccCooldown > 0) {
            --this.ccCooldown;
            return;
        }
        Object t = e.getPacket();
        if (t instanceof EntityVelocityUpdateS2CPacket && (pac = (EntityVelocityUpdateS2CPacket)t).getEntityId() == Velocity.mc.player.getId() && (!this.onlyAura.getValue().booleanValue() || ModuleManager.aura.isEnabled())) {
            switch (this.mode.getValue().ordinal()) {
                case 0: {
                    if (!this.flag) {
                        e.cancel();
                        this.flag = true;
                        break;
                    }
                    this.flag = false;
                    ((ISPacketEntityVelocity)pac).setMotionX((int)(pac.getVelocityX() * -0.1));
                    ((ISPacketEntityVelocity)pac).setMotionZ((int)(pac.getVelocityZ() * -0.1));
                    break;
                }
                case 4: {
                    double vX = Math.abs(pac.getVelocityX());
                    double vZ = Math.abs(pac.getVelocityZ());
                    double[] motion = MovementUtility.forward(vX + vZ);
                    ((ISPacketEntityVelocity)pac).setMotionX((int)motion[0]);
                    ((ISPacketEntityVelocity)pac).setMotionY(0);
                    ((ISPacketEntityVelocity)pac).setMotionZ((int)motion[1]);
                    break;
                }
                case 3: {
                    ((ISPacketEntityVelocity)pac).setMotionX((int)((float)pac.getVelocityX() * this.horizontal.getValue().floatValue() / 100.0f));
                    ((ISPacketEntityVelocity)pac).setMotionY((int)((float)pac.getVelocityY() * this.vertical.getValue().floatValue() / 100.0f));
                    ((ISPacketEntityVelocity)pac).setMotionZ((int)((float)pac.getVelocityZ() * this.horizontal.getValue().floatValue() / 100.0f));
                    break;
                }
                case 2: {
                    e.cancel();
                    this.sendPacket((Packet<?>)new PlayerMoveC2SPacket.PositionAndOnGround(Velocity.mc.player.getX(), -999.0, Velocity.mc.player.getZ(), true));
                    break;
                }
                case 1: {
                    e.cancel();
                    break;
                }
                case 6: {
                    ((ISPacketEntityVelocity)pac).setMotionX((int)((float)pac.getVelocityX() * this.horizontal.getValue().floatValue() / 100.0f));
                    ((ISPacketEntityVelocity)pac).setMotionZ((int)((float)pac.getVelocityZ() * this.horizontal.getValue().floatValue() / 100.0f));
                    break;
                }
                case 5: {
                    e.cancel();
                    this.grimTicks = 6;
                    break;
                }
                case 7: {
                    e.cancel();
                    this.flag = true;
                }
            }
        }
        if ((t = e.getPacket()) instanceof ExplosionS2CPacket) {
            ExplosionS2CPacket explosion = (ExplosionS2CPacket)t;
            if (this.explosions.getValue().booleanValue()) {
                switch (this.mode.getValue().ordinal()) {
                    case 1: {
                        ((IExplosionS2CPacket)explosion).setMotionX(0.0f);
                        ((IExplosionS2CPacket)explosion).setMotionY(0.0f);
                        ((IExplosionS2CPacket)explosion).setMotionZ(0.0f);
                        break;
                    }
                    case 3: {
                        ((IExplosionS2CPacket)explosion).setMotionX(((IExplosionS2CPacket)explosion).getMotionX() * this.horizontal.getValue().floatValue() / 100.0f);
                        ((IExplosionS2CPacket)explosion).setMotionZ(((IExplosionS2CPacket)explosion).getMotionZ() * this.horizontal.getValue().floatValue() / 100.0f);
                        ((IExplosionS2CPacket)explosion).setMotionY(((IExplosionS2CPacket)explosion).getMotionY() * this.vertical.getValue().floatValue() / 100.0f);
                        break;
                    }
                    case 7: {
                        ((IExplosionS2CPacket)explosion).setMotionX(0.0f);
                        ((IExplosionS2CPacket)explosion).setMotionY(0.0f);
                        ((IExplosionS2CPacket)explosion).setMotionZ(0.0f);
                        this.flag = true;
                    }
                }
            }
        }
        if (this.mode.getValue() == modeEn.OldGrim && e.getPacket() instanceof CommonPingS2CPacket && this.grimTicks > 0) {
            e.cancel();
            --this.grimTicks;
        }
        if (e.getPacket() instanceof PlayerPositionLookS2CPacket && (this.cc.getValue().booleanValue() || this.mode.getValue() == modeEn.GrimNew)) {
            this.ccCooldown = 5;
        }
    }

    @Override
    public void onUpdate() {
        if (Velocity.mc.player != null && (Velocity.mc.player.isTouchingWater() || Velocity.mc.player.isSubmergedInWater()) && this.pauseInWater.getValue().booleanValue()) {
            return;
        }
        switch (this.mode.getValue().ordinal()) {
            case 0: {
                if (Velocity.mc.player.hurtTime <= 0 || Velocity.mc.player.isOnGround()) break;
                double var3 = Velocity.mc.player.getYaw() * ((float)Math.PI / 180);
                double var5 = Math.sqrt(Velocity.mc.player.getVelocity().x * Velocity.mc.player.getVelocity().x + Velocity.mc.player.getVelocity().z * Velocity.mc.player.getVelocity().z);
                Velocity.mc.player.setVelocity(-Math.sin(var3) * var5, Velocity.mc.player.getVelocity().y, Math.cos(var3) * var5);
                Velocity.mc.player.setSprinting(Velocity.mc.player.age % 2 != 0);
                break;
            }
            case 6: {
                if (!this.failJump && Velocity.mc.player.hurtTime <= 6 || !Velocity.mc.player.isOnGround()) break;
                if (this.failJump) {
                    this.failJump = false;
                }
                if (!this.doJump) {
                    this.skip = true;
                }
                if (Math.random() <= (double)this.failRate.getValue().floatValue() && this.fail.getValue().booleanValue()) {
                    if (Math.random() <= (double)this.jumpRate.getValue().floatValue()) {
                        this.doJump = true;
                        this.failJump = true;
                    } else {
                        this.doJump = false;
                        this.failJump = false;
                    }
                } else {
                    this.doJump = true;
                    this.failJump = false;
                }
                if (this.skip) {
                    this.skip = false;
                    return;
                }
                switch (this.jumpMode.getValue().ordinal()) {
                    case 1: {
                        Velocity.mc.player.jump();
                        break;
                    }
                    case 0: {
                        Velocity.mc.player.setVelocity(Velocity.mc.player.getVelocity().getX(), (double)this.motion.getValue().floatValue(), Velocity.mc.player.getVelocity().getZ());
                        break;
                    }
                    case 2: {
                        Velocity.mc.player.jump();
                        Velocity.mc.player.setVelocity(Velocity.mc.player.getVelocity().getX(), (double)this.motion.getValue().floatValue(), Velocity.mc.player.getVelocity().getZ());
                    }
                }
                break;
            }
            case 7: {
                if (!this.flag) break;
                if (this.ccCooldown <= 0) {
                    this.sendPacket((Packet<?>)new PlayerMoveC2SPacket.Full(Velocity.mc.player.getX(), Velocity.mc.player.getY(), Velocity.mc.player.getZ(), ((IClientPlayerEntity)Velocity.mc.player).getLastYaw(), ((IClientPlayerEntity)Velocity.mc.player).getLastPitch(), Velocity.mc.player.isOnGround()));
                    this.sendPacket((Packet<?>)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, BlockPos.ofFloored((Position)Velocity.mc.player.getPos()), Direction.DOWN));
                }
                this.flag = false;
            }
        }
        if (this.grimTicks > 0) {
            --this.grimTicks;
        }
    }

    private boolean isValidMotion(double motion, double min, double max) {
        return Math.abs(motion) > min && Math.abs(motion) < max;
    }

    @Override
    public void onEnable() {
        this.grimTicks = 0;
    }

    public static enum modeEn {
        Matrix,
        Cancel,
        Sunrise,
        Custom,
        Redirect,
        OldGrim,
        Jump,
        GrimNew;

    }

    public static enum jumpModeEn {
        Motion,
        Jump,
        Both;

    }
}

