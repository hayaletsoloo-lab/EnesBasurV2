/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  meteordevelopment.orbit.EventHandler
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.decoration.ArmorStandEntity
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.entity.vehicle.BoatEntity
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.shape.VoxelShape
 *  net.minecraft.util.shape.VoxelShapes
 */
package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.EventMove;
import thunder.hack.events.impl.EventPlayerTravel;
import thunder.hack.events.impl.EventPostTick;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.EventTick;
import thunder.hack.events.impl.PostPlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.features.modules.movement.Strafe;
import thunder.hack.injection.accesors.IInteractionManager;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.interfaces.IEntity;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.MovementUtility;
import thunder.hack.utility.player.SearchInvResult;

public class Speed
extends Module {
    public final Setting<Mode> mode = new Setting<Mode>("Mode", Mode.NCP);
    public Setting<Boolean> useTimer = new Setting<Boolean>("Use Timer", false);
    public Setting<Boolean> pauseInLiquids = new Setting<Boolean>("PauseInLiquids", false);
    public Setting<Boolean> pauseWhileSneaking = new Setting<Boolean>("PauseWhileSneaking", false);
    public final Setting<Integer> hurttime = new Setting<Integer>("HurtTime", 0, 0, 10, v -> this.mode.is(Mode.MatrixDamage));
    public final Setting<Float> boostFactor = new Setting<Float>("BoostFactor", Float.valueOf(2.0f), Float.valueOf(0.0f), Float.valueOf(10.0f), v -> this.mode.is(Mode.MatrixDamage) || this.mode.is(Mode.Vanilla));
    public final Setting<Boolean> allowOffGround = new Setting<Boolean>("AllowOffGround", true, v -> this.mode.is(Mode.MatrixDamage));
    public final Setting<Integer> shiftTicks = new Setting<Integer>("ShiftTicks", 0, 0, 10, v -> this.mode.is(Mode.MatrixDamage));
    public final Setting<Integer> fireWorkSlot = new Setting<Integer>("FireSlot", 1, 1, 9, v -> this.mode.getValue() == Mode.FireWork);
    public final Setting<Integer> delay = new Setting<Integer>("Delay", 8, 1, 20, v -> this.mode.getValue() == Mode.FireWork);
    public final Setting<Boolean> strict = new Setting<Boolean>("Strict", false, v -> this.mode.is(Mode.GrimIce));
    public final Setting<Float> matrixJBSpeed = new Setting<Float>("TimerSpeed", Float.valueOf(1.088f), Float.valueOf(1.0f), Float.valueOf(2.0f), v -> this.mode.is(Mode.MatrixJB));
    public final Setting<Boolean> armorStands = new Setting<Boolean>("ArmorStands", false, v -> this.mode.is(Mode.GrimCombo) || this.mode.is(Mode.GrimEntity2));
    public double baseSpeed;
    private int stage;
    private int ticks;
    private int prevSlot;
    private float prevForward = 0.0f;
    private Timer elytraDelay = new Timer();
    private Timer startDelay = new Timer();

    public Speed() {
        super("Speed", Module.Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        ThunderHack.TICK_TIMER = 1.0f;
    }

    @Override
    public void onEnable() {
        this.stage = 1;
        this.ticks = 0;
        this.baseSpeed = 0.2873;
        this.startDelay.reset();
        this.prevSlot = -1;
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (Speed.mc.player.isInFluid() && this.pauseInLiquids.getValue().booleanValue() || Speed.mc.player.isSneaking() && this.pauseWhileSneaking.getValue().booleanValue()) {
            return;
        }
        if (this.mode.getValue() == Mode.MatrixJB) {
            boolean closeToGround = false;
            for (VoxelShape a : Speed.mc.world.getBlockCollisions((Entity)Speed.mc.player, Speed.mc.player.getBoundingBox().expand(0.5, 0.0, 0.5).offset(0.0, -1.0, 0.0))) {
                if (a == VoxelShapes.empty()) continue;
                closeToGround = true;
                break;
            }
            if (MovementUtility.isMoving() && closeToGround && Speed.mc.player.fallDistance <= 0.0f) {
                ThunderHack.TICK_TIMER = 1.0f;
                Speed.mc.player.setOnGround(true);
                Speed.mc.player.jump();
            } else if (Speed.mc.player.fallDistance > 0.0f && this.useTimer.getValue().booleanValue()) {
                ThunderHack.TICK_TIMER = this.matrixJBSpeed.getValue().floatValue();
                Speed.mc.player.addVelocity(0.0, (double)-0.003f, 0.0);
            }
        }
    }

    @EventHandler
    public void modifyVelocity(EventPlayerTravel e) {
        if (this.mode.getValue() == Mode.GrimEntity && !e.isPre() && ThunderHack.core.getSetBackTime() > 1000L) {
            for (PlayerEntity playerEntity : Managers.ASYNC.getAsyncPlayers()) {
                if (playerEntity == Speed.mc.player || !(Speed.mc.player.squaredDistanceTo((Entity)playerEntity) <= 2.25)) continue;
                float p = Speed.mc.world.getBlockState(((IEntity)Speed.mc.player).thunderHack_Recode$getVelocityBP()).getBlock().getSlipperiness();
                float f = Speed.mc.player.isOnGround() ? p * 0.91f : 0.91f;
                float f2 = Speed.mc.player.isOnGround() ? p : 0.99f;
                Speed.mc.player.setVelocity(Speed.mc.player.getVelocity().getX() / (double)f * (double)f2, Speed.mc.player.getVelocity().getY(), Speed.mc.player.getVelocity().getZ() / (double)f * (double)f2);
                break;
            }
        }
        if ((this.mode.is(Mode.GrimEntity2) || this.mode.is(Mode.GrimCombo)) && !e.isPre() && ThunderHack.core.getSetBackTime() > 1000L && MovementUtility.isMoving()) {
            int collisions = 0;
            for (Entity ent : Speed.mc.world.getEntities()) {
                if (ent == Speed.mc.player || ent instanceof ArmorStandEntity && !this.armorStands.getValue().booleanValue() || !(ent instanceof LivingEntity) && !(ent instanceof BoatEntity) || !Speed.mc.player.getBoundingBox().expand(1.0).intersects(ent.getBoundingBox())) continue;
                ++collisions;
            }
            double[] dArray = MovementUtility.forward(0.08 * (double)collisions);
            Speed.mc.player.addVelocity(dArray[0], 0.0, dArray[1]);
        }
    }

    @EventHandler
    public void onTick(EventTick e) {
        if ((this.mode.is(Mode.GrimIce) || this.mode.is(Mode.GrimCombo)) && Speed.mc.player.isOnGround()) {
            BlockPos pos = ((IEntity)Speed.mc.player).thunderHack_Recode$getVelocityBP();
            SearchInvResult result = InventoryUtility.findBlockInHotBar(Blocks.ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE);
            if (Speed.mc.world.isAir(pos) || !result.found() || !Speed.mc.options.jumpKey.isPressed()) {
                return;
            }
            this.prevSlot = Speed.mc.player.getInventory().selectedSlot;
            result.switchTo();
            this.sendPacket((Packet<?>)new PlayerMoveC2SPacket.Full(Speed.mc.player.getX(), Speed.mc.player.getY(), Speed.mc.player.getZ(), Speed.mc.player.getYaw(), 90.0f, Speed.mc.player.isOnGround()));
            if (this.strict.getValue().booleanValue()) {
                this.sendPacket((Packet<?>)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP));
                this.sendPacket((Packet<?>)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, Direction.UP));
            }
            this.sendPacket((Packet<?>)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP));
            this.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(pos.down().toCenterPos().add(0.0, 0.5, 0.0), Direction.UP, pos.down(), false), id));
            Speed.mc.world.setBlockState(pos, Blocks.ICE.getDefaultState());
        }
    }

    @EventHandler
    public void onPostTick(EventPostTick e) {
        if ((this.mode.is(Mode.GrimIce) || this.mode.is(Mode.GrimCombo)) && this.prevSlot != -1) {
            Speed.mc.player.getInventory().selectedSlot = this.prevSlot;
            ((IInteractionManager)Speed.mc.interactionManager).syncSlot();
            this.prevSlot = -1;
        }
    }

    @Override
    public void onUpdate() {
        if (Speed.mc.player.isInFluid() && this.pauseInLiquids.getValue().booleanValue() || Speed.mc.player.isSneaking() && this.pauseWhileSneaking.getValue().booleanValue()) {
            return;
        }
        if (this.mode.getValue() == Mode.FireWork) {
            int fireInInv;
            --this.ticks;
            int ellySlot = InventoryUtility.getElytra();
            int fireSlot = InventoryUtility.findItemInHotBar(Items.FIREWORK_ROCKET).slot();
            boolean inOffHand = Speed.mc.player.getOffHandStack().getItem() == Items.FIREWORK_ROCKET;
            boolean bl = inOffHand;
            if (fireSlot == -1 && (fireInInv = InventoryUtility.findItemInInventory(Items.FIREWORK_ROCKET).slot()) != -1) {
                Speed.mc.interactionManager.clickSlot(Speed.mc.player.currentScreenHandler.syncId, fireInInv, this.fireWorkSlot.getValue() - 1, SlotActionType.SWAP, (PlayerEntity)Speed.mc.player);
            }
            if (ellySlot != -1 && (fireSlot != -1 || inOffHand) && !Speed.mc.player.isOnGround() && Speed.mc.player.fallDistance > 0.0f && this.ticks <= 0) {
                if (ellySlot != -2) {
                    Speed.mc.interactionManager.clickSlot(0, ellySlot, 1, SlotActionType.PICKUP, (PlayerEntity)Speed.mc.player);
                    Speed.mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, (PlayerEntity)Speed.mc.player);
                }
                Speed.mc.player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)Speed.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                int prevSlot = Speed.mc.player.getInventory().selectedSlot;
                if (prevSlot != fireSlot && !inOffHand) {
                    this.sendPacket((Packet<?>)new UpdateSelectedSlotC2SPacket(fireSlot));
                }
                Speed.mc.interactionManager.interactItem((PlayerEntity)Speed.mc.player, inOffHand ? Hand.OFF_HAND : Hand.MAIN_HAND);
                if (prevSlot != fireSlot && !inOffHand) {
                    this.sendPacket((Packet<?>)new UpdateSelectedSlotC2SPacket(prevSlot));
                }
                if (ellySlot != -2) {
                    Speed.mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, (PlayerEntity)Speed.mc.player);
                    Speed.mc.interactionManager.clickSlot(0, ellySlot, 1, SlotActionType.PICKUP, (PlayerEntity)Speed.mc.player);
                }
                Speed.mc.player.networkHandler.sendPacket((Packet)new CloseHandledScreenC2SPacket(Speed.mc.player.currentScreenHandler.syncId));
                this.ticks = this.delay.getValue();
            }
        }
        if (this.mode.getValue() == Mode.ElytraLowHop) {
            if (Speed.mc.player.isOnGround()) {
                Speed.mc.player.jump();
                return;
            }
            if (Speed.mc.world.getBlockCollisions((Entity)Speed.mc.player, Speed.mc.player.getBoundingBox().expand(-0.29, 0.0, -0.29).offset(0.0, -3.0, 0.0)).iterator().hasNext() && this.elytraDelay.passedMs(150L) && this.startDelay.passedMs(500L)) {
                int elytra = InventoryUtility.getElytra();
                if (elytra == -1) {
                    this.disable(ClientSettings.isRu() ? "\u0414\u043b\u044f \u044d\u0442\u043e\u0433\u043e \u0440\u0435\u0436\u0438\u043c\u0430 \u043d\u0443\u0436\u043d\u0430 \u044d\u043b\u0438\u0442\u0440\u0430!" : "You need elytra for this mode!");
                } else {
                    Strafe.disabler(elytra);
                }
                Speed.mc.player.setVelocity(Speed.mc.player.getVelocity().getX(), 0.0, Speed.mc.player.getVelocity().getZ());
                if (MovementUtility.isMoving()) {
                    MovementUtility.setMotion(0.85);
                }
                this.elytraDelay.reset();
            }
        }
    }

    @EventHandler
    public void onPostPlayerUpdate(PostPlayerUpdateEvent event) {
        if (this.mode.getValue() == Mode.MatrixDamage && MovementUtility.isMoving() && Speed.mc.player.hurtTime > this.hurttime.getValue()) {
            if (Speed.mc.player.isOnGround()) {
                MovementUtility.setMotion(0.387f * this.boostFactor.getValue().floatValue());
            } else if (Speed.mc.player.isTouchingWater()) {
                MovementUtility.setMotion(0.346f * this.boostFactor.getValue().floatValue());
            } else if (!Speed.mc.player.isOnGround() && this.allowOffGround.getValue().booleanValue()) {
                MovementUtility.setMotion(0.448f * this.boostFactor.getValue().floatValue());
            }
            if (this.shiftTicks.getValue() > 0) {
                event.cancel();
                event.setIterations(this.shiftTicks.getValue());
            }
        }
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (Speed.mc.player.isInFluid() && this.pauseInLiquids.getValue().booleanValue() || Speed.mc.player.isSneaking() && this.pauseWhileSneaking.getValue().booleanValue()) {
            return;
        }
        if (this.mode.getValue() != Mode.NCP && this.mode.getValue() != Mode.StrictStrafe) {
            return;
        }
        if (Speed.mc.player.getAbilities().flying) {
            return;
        }
        if (Speed.mc.player.isFallFlying()) {
            return;
        }
        if (Speed.mc.player.getHungerManager().getFoodLevel() <= 6) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        event.cancel();
        if (MovementUtility.isMoving()) {
            double amplifier;
            ThunderHack.TICK_TIMER = this.useTimer.getValue() != false ? 1.088f : 1.0f;
            float currentSpeed = this.mode.getValue() == Mode.NCP && Speed.mc.player.input.movementForward <= 0.0f && this.prevForward > 0.0f ? Managers.PLAYER.currentPlayerSpeed * 0.66f : Managers.PLAYER.currentPlayerSpeed;
            boolean canJump = !Speed.mc.player.horizontalCollision;
            boolean bl = canJump;
            if (this.stage == 1 && Speed.mc.player.isOnGround() && canJump) {
                Speed.mc.player.setVelocity(Speed.mc.player.getVelocity().x, MovementUtility.getJumpSpeed(), Speed.mc.player.getVelocity().z);
                event.setY(MovementUtility.getJumpSpeed());
                this.baseSpeed *= 2.149;
                this.stage = 2;
            } else if (this.stage == 2) {
                this.baseSpeed = (double)currentSpeed - 0.66 * ((double)currentSpeed - MovementUtility.getBaseMoveSpeed());
                this.stage = 3;
            } else {
                if (Speed.mc.world.getBlockCollisions((Entity)Speed.mc.player, Speed.mc.player.getBoundingBox().offset(0.0, Speed.mc.player.getVelocity().getY(), 0.0)).iterator().hasNext() || Speed.mc.player.verticalCollision) {
                    this.stage = 1;
                }
                this.baseSpeed = (double)currentSpeed - (double)currentSpeed / 159.0;
            }
            this.baseSpeed = Math.max(this.baseSpeed, MovementUtility.getBaseMoveSpeed());
            double ncpSpeed = this.mode.getValue() == Mode.StrictStrafe || Speed.mc.player.input.movementForward < 1.0f ? 0.465 : 0.576;
            double ncpBypassSpeed = this.mode.getValue() == Mode.StrictStrafe || Speed.mc.player.input.movementForward < 1.0f ? 0.44 : 0.57;
            double d = ncpBypassSpeed;
            if (Speed.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                amplifier = Speed.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
                ncpSpeed *= 1.0 + 0.2 * (amplifier + 1.0);
                ncpBypassSpeed *= 1.0 + 0.2 * (amplifier + 1.0);
            }
            if (Speed.mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
                amplifier = Speed.mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
                ncpSpeed /= 1.0 + 0.2 * (amplifier + 1.0);
                ncpBypassSpeed /= 1.0 + 0.2 * (amplifier + 1.0);
            }
            this.baseSpeed = Math.min(this.baseSpeed, this.ticks > 25 ? ncpSpeed : ncpBypassSpeed);
            if (this.ticks++ > 50) {
                this.ticks = 0;
            }
            MovementUtility.modifyEventSpeed(event, this.baseSpeed);
            this.prevForward = Speed.mc.player.input.movementForward;
        } else {
            ThunderHack.TICK_TIMER = 1.0f;
            event.setX(0.0);
            event.setZ(0.0);
        }
    }

    public static enum Mode {
        StrictStrafe,
        MatrixJB,
        NCP,
        ElytraLowHop,
        MatrixDamage,
        GrimEntity,
        GrimEntity2,
        FireWork,
        Vanilla,
        GrimIce,
        GrimCombo;

    }
}

