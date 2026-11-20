/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  meteordevelopment.orbit.EventHandler
 *  net.minecraft.block.Blocks
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
package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
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
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.injection.accesors.ISPacketEntityVelocity;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.MovementUtility;

public class TargetStrafe
extends Module {
    public Setting<Boolean> jump = new Setting<Boolean>("Jump", true);
    public Setting<Float> distance = new Setting<Float>("Distance", Float.valueOf(1.3f), Float.valueOf(0.2f), Float.valueOf(7.0f));
    private final Setting<Boost> boost = new Setting<Boost>("Boost", Boost.None);
    public Setting<Float> setSpeed = new Setting<Float>("speed", Float.valueOf(1.3f), Float.valueOf(0.0f), Float.valueOf(2.0f), v -> this.boost.getValue() == Boost.Elytra);
    private final Setting<Float> velReduction = new Setting<Float>("Reduction", Float.valueOf(6.0f), Float.valueOf(0.1f), Float.valueOf(10.0f), v -> this.boost.getValue() == Boost.Damage);
    private final Setting<Float> maxVelocitySpeed = new Setting<Float>("MaxVelocity", Float.valueOf(0.8f), Float.valueOf(0.1f), Float.valueOf(2.0f), v -> this.boost.getValue() == Boost.Damage);
    public static double oldSpeed;
    public static double contextFriction;
    public static double fovval;
    public static boolean needSwap;
    public static boolean needSprintState;
    public static boolean skip;
    public static boolean switchDir;
    public static boolean disabled;
    public static int noSlowTicks;
    public static int jumpTicks;
    public static int waterTicks;
    static long disableTime;
    private static TargetStrafe instance;

    public TargetStrafe() {
        super("TargetStrafe", Module.Category.COMBAT);
        instance = this;
    }

    public static TargetStrafe getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        oldSpeed = 0.0;
        fovval = (Double)TargetStrafe.mc.options.getFovEffectScale().getValue();
        TargetStrafe.mc.options.getFovEffectScale().setValue(0.0);
        skip = true;
    }

    public boolean canStrafe() {
        if (TargetStrafe.mc.player.isSneaking()) {
            return false;
        }
        if (TargetStrafe.mc.player.isInLava()) {
            return false;
        }
        if (ModuleManager.speed.isEnabled()) {
            return false;
        }
        if (TargetStrafe.mc.player.isSubmergedInWater() || waterTicks > 0) {
            return false;
        }
        return !TargetStrafe.mc.player.getAbilities().flying;
    }

    public boolean needToSwitch(double x, double z) {
        if (TargetStrafe.mc.player.horizontalCollision || (TargetStrafe.mc.options.leftKey.isPressed() || TargetStrafe.mc.options.rightKey.isPressed()) && jumpTicks <= 0) {
            jumpTicks = 10;
            return true;
        }
        for (int i = (int)(TargetStrafe.mc.player.getY() + 4.0); i >= 0; --i) {
            BlockPos playerPos = new BlockPos((int)Math.floor(x), (int)Math.floor(i), (int)Math.floor(z));
            if (TargetStrafe.mc.world.getBlockState(playerPos).getBlock().equals(Blocks.LAVA) || TargetStrafe.mc.world.getBlockState(playerPos).getBlock().equals(Blocks.FIRE)) {
                return true;
            }
            if (TargetStrafe.mc.world.isAir(playerPos)) continue;
            return false;
        }
        return false;
    }

    @Override
    public void onDisable() {
        TargetStrafe.mc.options.getFovEffectScale().setValue(fovval);
    }

    public double calculateSpeed(EventMove move) {
        float n8;
        float f;
        --jumpTicks;
        float speedAttributes = this.getAIMoveSpeed();
        float frictionFactor = TargetStrafe.mc.world.getBlockState((BlockPos)new BlockPos.Mutable().set(TargetStrafe.mc.player.getX(), this.getBoundingBox().getMin(Direction.Axis.Y) - move.getY(), TargetStrafe.mc.player.getZ())).getBlock().getSlipperiness() * 0.91f;
        float n6 = (float)(oldSpeed > 0.32 && TargetStrafe.mc.player.isUsingItem() ? 0.88 : (double)0.91f);
        float f2 = f = TargetStrafe.mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) && TargetStrafe.mc.player.isUsingItem() ? 0.88f : n6;
        if (TargetStrafe.mc.player.isOnGround()) {
            n6 = frictionFactor;
        }
        float n7 = (float)((double)0.1631f / Math.pow(n6, 3.0));
        if (TargetStrafe.mc.player.isOnGround()) {
            n8 = speedAttributes * n7;
            if (move.getY() > 0.0) {
                n8 += this.boost.getValue() == Boost.Elytra && InventoryUtility.getElytra() != -1 && disabled ? 0.65f : 0.2f;
            }
            disabled = false;
        } else {
            n8 = 0.0255f;
        }
        boolean noslow = false;
        double max2 = oldSpeed + (double)n8;
        double max = 0.0;
        if (TargetStrafe.mc.player.isUsingItem() && move.getY() <= 0.0) {
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
        max2 = noSlowTicks > 3 ? max - 0.019 : Math.max(noslow ? 0.0 : 0.25, max2) - (TargetStrafe.mc.player.age % 2 == 0 ? 0.001 : 0.002);
        contextFriction = n6;
        if (!TargetStrafe.mc.player.isOnGround()) {
            needSprintState = !((IClientPlayerEntity)TargetStrafe.mc.player).getLastSprinting();
            needSwap = true;
        } else {
            needSprintState = false;
        }
        return max2;
    }

    public Box getBoundingBox() {
        return new Box(TargetStrafe.mc.player.getX() - 0.1, TargetStrafe.mc.player.getY(), TargetStrafe.mc.player.getZ() - 0.1, TargetStrafe.mc.player.getX() + 0.1, TargetStrafe.mc.player.getY() + 1.0, TargetStrafe.mc.player.getZ() + 0.1);
    }

    public float getAIMoveSpeed() {
        boolean prevSprinting = TargetStrafe.mc.player.isSprinting();
        TargetStrafe.mc.player.setSprinting(false);
        float speed = TargetStrafe.mc.player.getMovementSpeed() * 1.3f;
        TargetStrafe.mc.player.setSprinting(prevSprinting);
        return speed;
    }

    public static void disabler(int elytra) {
        if (elytra == -1) {
            return;
        }
        if (System.currentTimeMillis() - disableTime > 190L) {
            if (elytra != -2) {
                TargetStrafe.mc.interactionManager.clickSlot(0, elytra, 1, SlotActionType.PICKUP, (PlayerEntity)TargetStrafe.mc.player);
                TargetStrafe.mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, (PlayerEntity)TargetStrafe.mc.player);
            }
            TargetStrafe.mc.player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)TargetStrafe.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            TargetStrafe.mc.player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)TargetStrafe.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            if (elytra != -2) {
                TargetStrafe.mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, (PlayerEntity)TargetStrafe.mc.player);
                TargetStrafe.mc.interactionManager.clickSlot(0, elytra, 1, SlotActionType.PICKUP, (PlayerEntity)TargetStrafe.mc.player);
            }
            disableTime = System.currentTimeMillis();
        }
        disabled = true;
    }

    private double wrapDS(double x, double z) {
        double diffX = x - TargetStrafe.mc.player.getX();
        double diffZ = z - TargetStrafe.mc.player.getZ();
        return Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0;
    }

    @EventHandler
    public void onMove(EventMove event) {
        int elytraSlot = InventoryUtility.getElytra();
        if (this.boost.getValue() == Boost.Elytra && elytraSlot != -1 && MovementUtility.isMoving() && !TargetStrafe.mc.player.isOnGround() && TargetStrafe.mc.world.getBlockCollisions((Entity)TargetStrafe.mc.player, TargetStrafe.mc.player.getBoundingBox().offset(0.0, event.getY(), 0.0)).iterator().hasNext() && disabled) {
            oldSpeed = this.setSpeed.getValue().floatValue();
        }
        if (this.canStrafe()) {
            if (Aura.target != null && ModuleManager.aura.isEnabled()) {
                double z;
                double speed = this.calculateSpeed(event);
                double wrap = Math.atan2(TargetStrafe.mc.player.getZ() - Aura.target.getZ(), TargetStrafe.mc.player.getX() - Aura.target.getX());
                double x = Aura.target.getX() + (double)this.distance.getValue().floatValue() * Math.cos(wrap += switchDir ? speed / Math.sqrt(TargetStrafe.mc.player.squaredDistanceTo(Aura.target)) : -(speed / Math.sqrt(TargetStrafe.mc.player.squaredDistanceTo(Aura.target))));
                if (this.needToSwitch(x, z = Aura.target.getZ() + (double)this.distance.getValue().floatValue() * Math.sin(wrap))) {
                    switchDir = !switchDir;
                    x = Aura.target.getX() + (double)this.distance.getValue().floatValue() * Math.cos(wrap += 2.0 * (switchDir ? speed / Math.sqrt(TargetStrafe.mc.player.squaredDistanceTo(Aura.target)) : -(speed / Math.sqrt(TargetStrafe.mc.player.squaredDistanceTo(Aura.target)))));
                    z = Aura.target.getZ() + (double)this.distance.getValue().floatValue() * Math.sin(wrap);
                }
                event.setX(speed * -Math.sin(Math.toRadians(this.wrapDS(x, z))));
                event.setZ(speed * Math.cos(Math.toRadians(this.wrapDS(x, z))));
                event.cancel();
            }
        } else {
            oldSpeed = 0.0;
        }
    }

    @EventHandler
    public void updateValues(EventSync e) {
        oldSpeed = Math.hypot(TargetStrafe.mc.player.getX() - TargetStrafe.mc.player.prevX, TargetStrafe.mc.player.getZ() - TargetStrafe.mc.player.prevZ) * contextFriction;
        if (TargetStrafe.mc.player.isOnGround() && this.jump.getValue().booleanValue() && Aura.target != null) {
            TargetStrafe.mc.player.jump();
        }
        waterTicks = TargetStrafe.mc.player.isSubmergedInWater() ? 10 : (waterTicks = waterTicks - 1);
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (this.boost.getValue() == Boost.Elytra && InventoryUtility.getElytra() != -1 && !TargetStrafe.mc.player.isOnGround() && TargetStrafe.mc.player.fallDistance > 0.0f && !disabled) {
            TargetStrafe.disabler(InventoryUtility.getElytra());
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        EntityVelocityUpdateS2CPacket velocity;
        if (e.getPacket() instanceof PlayerPositionLookS2CPacket) {
            oldSpeed = 0.0;
        }
        if (e.getPacket() instanceof EntityVelocityUpdateS2CPacket && (velocity = (EntityVelocityUpdateS2CPacket)e.getPacket()).getEntityId() == TargetStrafe.mc.player.getId() && this.boost.getValue() == Boost.Damage) {
            if (TargetStrafe.mc.player.isOnGround()) {
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
            eventAction.setSprintState(!((IClientPlayerEntity)TargetStrafe.mc.player).getLastSprinting());
            needSwap = false;
        }
    }

    private static enum Boost {
        None,
        Elytra,
        Damage;

    }
}

