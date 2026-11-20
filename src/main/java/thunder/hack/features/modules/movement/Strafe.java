/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  meteordevelopment.orbit.EventHandler
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction$Axis
 */
package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import thunder.hack.core.Core;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventMove;
import thunder.hack.events.impl.EventSprint;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.injection.accesors.ISPacketEntityVelocity;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.MovementUtility;

public class Strafe
extends Module {
    private final Setting<Boost> boost = new Setting<Boost>("Boost", Boost.None);
    private final Setting<Float> setSpeed = new Setting<Float>("speed", Float.valueOf(1.3f), Float.valueOf(0.0f), Float.valueOf(2.0f), v -> this.boost.getValue() == Boost.Elytra);
    private final Setting<Float> velReduction = new Setting<Float>("Reduction", Float.valueOf(6.0f), Float.valueOf(0.1f), Float.valueOf(10.0f), v -> this.boost.getValue() == Boost.Damage);
    private final Setting<Float> maxVelocitySpeed = new Setting<Float>("MaxVelocity", Float.valueOf(0.8f), Float.valueOf(0.1f), Float.valueOf(2.0f), v -> this.boost.getValue() == Boost.Damage);
    private final Setting<Boolean> sunrise = new Setting<Boolean>("Sunrise", true, v -> this.boost.getValue() == Boost.Elytra);
    public static double oldSpeed;
    public static double contextFriction;
    public static double fovval;
    public static boolean needSwap;
    public static boolean needSprintState;
    public static boolean disabled;
    public static int noSlowTicks;
    static long disableTime;

    public Strafe() {
        super("Strafe", Module.Category.MOVEMENT);
    }

    public double calculateSpeed(EventMove move) {
        float n8;
        float f;
        float speedAttributes = this.getAIMoveSpeed();
        float frictionFactor = Strafe.mc.world.getBlockState((BlockPos)new BlockPos.Mutable().set(Strafe.mc.player.getX(), this.getBoundingBox().getMin(Direction.Axis.Y) - move.getY(), Strafe.mc.player.getZ())).getBlock().getSlipperiness() * 0.91f;
        float n6 = (float)(oldSpeed > 0.32 && Strafe.mc.player.isUsingItem() ? 0.88 : (double)0.91f);
        float f2 = f = Strafe.mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) && Strafe.mc.player.isUsingItem() ? 0.88f : n6;
        if (Strafe.mc.player.isOnGround()) {
            n6 = frictionFactor;
        }
        float n7 = (float)((double)0.1631f / Math.pow(n6, 3.0));
        if (Strafe.mc.player.isOnGround()) {
            n8 = speedAttributes * n7;
            if (move.getY() > 0.0) {
                n8 += this.boost.getValue() == Boost.Elytra && InventoryUtility.getElytra() != -1 && disabled && System.currentTimeMillis() - disableTime < 300L ? 0.65f : 0.2f;
            }
            disabled = false;
        } else {
            n8 = 0.0255f;
        }
        boolean noslow = false;
        double max2 = oldSpeed + (double)n8;
        double max = 0.0;
        if (Strafe.mc.player.isUsingItem() && move.getY() <= 0.0 && !this.sunrise.getValue().booleanValue()) {
            double d = 0.2873;
            double n10 = oldSpeed + (double)n8 * 0.25;
            double motionY2 = move.getY();
            if (motionY2 != 0.0 && Math.abs(motionY2) < 0.08) {
                n10 += 0.055;
            }
            max = Math.max(0.043, n10);
            if (max2 > d) {
                noslow = true;
                ++noSlowTicks;
            } else {
                noSlowTicks = Math.max(noSlowTicks - 1, 0);
            }
        } else {
            noSlowTicks = 0;
        }
        max2 = noSlowTicks > 3 ? max - 0.019 : Math.max(noslow ? 0.0 : 0.25, max2) - (Strafe.mc.player.age % 2 == 0 ? 0.001 : 0.002);
        contextFriction = n6;
        if (!Strafe.mc.player.isOnGround()) {
            needSprintState = !((IClientPlayerEntity)Strafe.mc.player).getLastSprinting();
            needSwap = true;
        } else {
            needSprintState = false;
        }
        return max2;
    }

    public float getAIMoveSpeed() {
        boolean prevSprinting = Strafe.mc.player.isSprinting();
        Strafe.mc.player.setSprinting(false);
        float speed = Strafe.mc.player.getMovementSpeed() * 1.3f;
        Strafe.mc.player.setSprinting(prevSprinting);
        return speed;
    }

    public static void disabler(int elytra) {
        if (elytra == -1) {
            return;
        }
        if (System.currentTimeMillis() - disableTime > 190L) {
            if (elytra != -2) {
                Strafe.mc.interactionManager.clickSlot(0, elytra, 1, SlotActionType.PICKUP, (PlayerEntity)Strafe.mc.player);
                Strafe.mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, (PlayerEntity)Strafe.mc.player);
            }
            Strafe.mc.player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)Strafe.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            Strafe.mc.player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)Strafe.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            if (elytra != -2) {
                Strafe.mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, (PlayerEntity)Strafe.mc.player);
                Strafe.mc.interactionManager.clickSlot(0, elytra, 1, SlotActionType.PICKUP, (PlayerEntity)Strafe.mc.player);
            }
            disableTime = System.currentTimeMillis();
        }
        disabled = true;
    }

    @Override
    public void onEnable() {
        oldSpeed = 0.0;
        fovval = (Double)Strafe.mc.options.getFovEffectScale().getValue();
        Strafe.mc.options.getFovEffectScale().setValue(0.0);
    }

    @Override
    public void onDisable() {
        Strafe.mc.options.getFovEffectScale().setValue(fovval);
    }

    public boolean canStrafe() {
        if (Strafe.mc.player.isSneaking()) {
            return false;
        }
        if (Strafe.mc.player.isInLava()) {
            return false;
        }
        if (ModuleManager.speed.isEnabled()) {
            return false;
        }
        if (Strafe.mc.player.isSubmergedInWater()) {
            return false;
        }
        return !Strafe.mc.player.getAbilities().flying;
    }

    public Box getBoundingBox() {
        return new Box(Strafe.mc.player.getX() - 0.1, Strafe.mc.player.getY(), Strafe.mc.player.getZ() - 0.1, Strafe.mc.player.getX() + 0.1, Strafe.mc.player.getY() + 1.0, Strafe.mc.player.getZ() + 0.1);
    }

    @EventHandler
    public void onMove(EventMove event) {
        int elytraSlot = InventoryUtility.getElytra();
        if (this.boost.getValue() == Boost.Elytra && elytraSlot != -1 && MovementUtility.isMoving() && !Strafe.mc.player.isOnGround() && Strafe.mc.world.getBlockCollisions((Entity)Strafe.mc.player, Strafe.mc.player.getBoundingBox().offset(0.0, event.getY(), 0.0)).iterator().hasNext() && disabled) {
            oldSpeed = this.setSpeed.getValue().floatValue();
        }
        if (this.canStrafe()) {
            if (MovementUtility.isMoving()) {
                double[] motions = MovementUtility.forward(this.calculateSpeed(event));
                event.setX(motions[0]);
                event.setZ(motions[1]);
            } else {
                oldSpeed = 0.0;
                event.setX(0.0);
                event.setZ(0.0);
            }
            event.cancel();
        } else {
            oldSpeed = 0.0;
        }
    }

    @EventHandler
    public void onSync(EventSync e) {
        oldSpeed = Math.hypot(Strafe.mc.player.getX() - Strafe.mc.player.prevX, Strafe.mc.player.getZ() - Strafe.mc.player.prevZ) * contextFriction;
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        EntityVelocityUpdateS2CPacket velocity;
        if (e.getPacket() instanceof PlayerPositionLookS2CPacket) {
            oldSpeed = 0.0;
        }
        if (e.getPacket() instanceof EntityVelocityUpdateS2CPacket && (velocity = (EntityVelocityUpdateS2CPacket)e.getPacket()).getEntityId() == Strafe.mc.player.getId() && this.boost.getValue() == Boost.Damage) {
            if (Strafe.mc.player.isOnGround()) {
                return;
            }
            double vX = velocity.getVelocityX();
            double vZ = velocity.getVelocityZ();
            if (vX < 0.0) {
                vX *= -1.0;
            }
            if (vZ < 0.0) {
                vZ *= -1.0;
            }
            oldSpeed = (vX + vZ) / (double)(this.velReduction.getValue().floatValue() * 1000.0f);
            oldSpeed = Math.min(oldSpeed, (double)this.maxVelocitySpeed.getValue().floatValue());
            ((ISPacketEntityVelocity)velocity).setMotionX(0);
            ((ISPacketEntityVelocity)velocity).setMotionY(0);
            ((ISPacketEntityVelocity)velocity).setMotionZ(0);
        }
    }

    @EventHandler
    public void actionEvent(EventSprint eventAction) {
        if (this.canStrafe() && Core.serverSprint != needSprintState) {
            eventAction.setSprintState(!Core.serverSprint);
        }
        if (needSwap) {
            eventAction.setSprintState(!((IClientPlayerEntity)Strafe.mc.player).getLastSprinting());
            needSwap = false;
        }
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (!(this.boost.getValue() != Boost.Elytra || InventoryUtility.getElytra() == -1 || Strafe.mc.player.isOnGround() || !(Strafe.mc.player.fallDistance > 0.0f) || disabled || Strafe.mc.world.getBlockCollisions((Entity)Strafe.mc.player, Strafe.mc.player.getBoundingBox().offset(0.0, (double)-1.1f, 0.0)).iterator().hasNext() && this.sunrise.getValue().booleanValue())) {
            Strafe.disabler(InventoryUtility.getElytra());
        }
    }

    private static enum Boost {
        None,
        Elytra,
        Damage;

    }
}

