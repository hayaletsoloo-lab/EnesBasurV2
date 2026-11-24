/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  meteordevelopment.orbit.EventHandler
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.network.OtherClientPlayerEntity
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.attribute.EntityAttributes
 *  net.minecraft.entity.decoration.ArmorStandEntity
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.mob.HostileEntity
 *  net.minecraft.entity.mob.MobEntity
 *  net.minecraft.entity.mob.SlimeEntity
 *  net.minecraft.entity.passive.AnimalEntity
 *  net.minecraft.entity.passive.CatEntity
 *  net.minecraft.entity.passive.VillagerEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.entity.projectile.FireballEntity
 *  net.minecraft.entity.projectile.ProjectileEntity
 *  net.minecraft.entity.projectile.ShulkerBulletEntity
 *  net.minecraft.item.AxeItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.SwordItem
 *  net.minecraft.item.TridentItem
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.Hand
 *  net.minecraft.util.UseAction
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.World
 *  org.jetbrains.annotations.NotNull
 */
package thunder.hack.features.modules.combat;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.core.Core;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.core.manager.player.PlayerManager;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.features.modules.combat.AntiBot;
import thunder.hack.features.modules.combat.Criticals;
import thunder.hack.features.modules.combat.ElytraTarget;
import thunder.hack.gui.notification.Notification;
import thunder.hack.injection.accesors.ILivingEntity;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.BaritoneUtility;
import thunder.hack.utility.Timer;
import thunder.hack.utility.interfaces.IOtherClientPlayerEntity;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.animation.CaptureMark;

public class Aura
extends Module {
    public final Setting<Float> attackRange = new Setting<Float>("Range", Float.valueOf(3.1f), Float.valueOf(1.0f), Float.valueOf(6.0f));
    public final Setting<Float> wallRange = new Setting<Float>("ThroughWallsRange", Float.valueOf(3.1f), Float.valueOf(0.0f), Float.valueOf(6.0f));
    public final Setting<Boolean> elytra = new Setting<Boolean>("ElytraOverride", false);
    public final Setting<Float> elytraAttackRange = new Setting<Float>("ElytraRange", Float.valueOf(30.0f), Float.valueOf(1.0f), Float.valueOf(100.0f), v -> this.elytra.getValue());
    public final Setting<Float> elytraWallRange = new Setting<Float>("ElytraThroughWallsRange", Float.valueOf(30.0f), Float.valueOf(0.0f), Float.valueOf(100.0f), v -> this.elytra.getValue());
    public final Setting<WallsBypass> wallsBypass = new Setting<WallsBypass>("WallsBypass", WallsBypass.Off, v -> this.getWallRange() > 0.0f);
    public final Setting<Integer> fov = new Setting<Integer>("FOV", 180, 1, 180);
    public final Setting<Mode> rotationMode = new Setting<Mode>("RotationMode", Mode.Track);
    public final Setting<Integer> interactTicks = new Setting<Integer>("InteractTicks", 3, 1, 10, v -> this.rotationMode.getValue() == Mode.Interact);
    public final Setting<Switch> switchMode = new Setting<Switch>("AutoWeapon", Switch.None);
    public final Setting<Boolean> onlyWeapon = new Setting<Boolean>("OnlyWeapon", false, v -> this.switchMode.getValue() != Switch.Silent);
    public final Setting<BooleanSettingGroup> smartCrit = new Setting<BooleanSettingGroup>("SmartCrit", new BooleanSettingGroup(true));
    public final Setting<Boolean> onlySpace = new Setting<Boolean>("OnlyCrit", false).addToGroup(this.smartCrit);
    public final Setting<Boolean> autoJump = new Setting<Boolean>("AutoJump", false).addToGroup(this.smartCrit);
    public final Setting<Boolean> shieldBreaker = new Setting<Boolean>("ShieldBreaker", true);
    public final Setting<Boolean> pauseWhileEating = new Setting<Boolean>("PauseWhileEating", false);
    public final Setting<Boolean> tpsSync = new Setting<Boolean>("TPSSync", false);
    public final Setting<Boolean> clientLook = new Setting<Boolean>("ClientLook", false);
    public final Setting<Boolean> pauseBaritone = new Setting<Boolean>("PauseBaritone", false);
    public final Setting<BooleanSettingGroup> oldDelay = new Setting<BooleanSettingGroup>("OldDelay", new BooleanSettingGroup(false));
    public final Setting<Integer> minCPS = new Setting<Integer>("MinCPS", 7, 1, 20).addToGroup(this.oldDelay);
    public final Setting<Integer> maxCPS = new Setting<Integer>("MaxCPS", 12, 1, 20).addToGroup(this.oldDelay);
    public final Setting<ESP> esp = new Setting<ESP>("ESP", ESP.ThunderHack);
    public final Setting<SettingGroup> espGroup = new Setting<SettingGroup>("ESPSettings", new SettingGroup(false, 0), v -> this.esp.is(ESP.ThunderHackV2));
    public final Setting<Integer> espLength = new Setting<Integer>("ESPLength", 14, 1, 40, v -> this.esp.is(ESP.ThunderHackV2)).addToGroup(this.espGroup);
    public final Setting<Integer> espFactor = new Setting<Integer>("ESPFactor", 8, 1, 20, v -> this.esp.is(ESP.ThunderHackV2)).addToGroup(this.espGroup);
    public final Setting<Float> espShaking = new Setting<Float>("ESPShaking", Float.valueOf(1.8f), Float.valueOf(1.5f), Float.valueOf(10.0f), v -> this.esp.is(ESP.ThunderHackV2)).addToGroup(this.espGroup);
    public final Setting<Float> espAmplitude = new Setting<Float>("ESPAmplitude", Float.valueOf(3.0f), Float.valueOf(0.1f), Float.valueOf(8.0f), v -> this.esp.is(ESP.ThunderHackV2)).addToGroup(this.espGroup);
    public final Setting<Sort> sort = new Setting<Sort>("Sort", Sort.LowestDistance);
    public final Setting<Boolean> lockTarget = new Setting<Boolean>("LockTarget", true);
    public final Setting<Boolean> elytraTarget = new Setting<Boolean>("ElytraTarget", true);
    public final Setting<SettingGroup> advanced = new Setting<SettingGroup>("Advanced", new SettingGroup(false, 0));
    public final Setting<Float> aimRange = new Setting<Float>("AimRange", Float.valueOf(3.1f), Float.valueOf(0.0f), Float.valueOf(6.0f)).addToGroup(this.advanced);
    public final Setting<SprintMode> sprintMode = new Setting<SprintMode>("SprintMode", SprintMode.HvH).addToGroup(this.advanced);
    public final Setting<Boolean> randomHitDelay = new Setting<Boolean>("RandomHitDelay", false).addToGroup(this.advanced);
    public final Setting<Boolean> pauseInInventory = new Setting<Boolean>("PauseInInventory", true).addToGroup(this.advanced);
    public final Setting<Boolean> dropSprint = new Setting<Boolean>("DropSprint", true).addToGroup(this.advanced);
    public final Setting<Boolean> returnSprint = new Setting<Boolean>("ReturnSprint", true, v -> this.dropSprint.getValue()).addToGroup(this.advanced);
    public final Setting<RayTrace> rayTrace = new Setting<RayTrace>("RayTrace", RayTrace.OnlyTarget).addToGroup(this.advanced);
    public final Setting<Boolean> grimRayTrace = new Setting<Boolean>("GrimRayTrace", true).addToGroup(this.advanced);
    public final Setting<Boolean> unpressShield = new Setting<Boolean>("UnpressShield", true).addToGroup(this.advanced);
    public final Setting<Boolean> deathDisable = new Setting<Boolean>("DisableOnDeath", true).addToGroup(this.advanced);
    public final Setting<Boolean> tpDisable = new Setting<Boolean>("TPDisable", false).addToGroup(this.advanced);
    public final Setting<Boolean> pullDown = new Setting<Boolean>("FastFall", false).addToGroup(this.advanced);
    public final Setting<Boolean> onlyJumpBoost = new Setting<Boolean>("OnlyJumpBoost", false, v -> this.pullDown.getValue()).addToGroup(this.advanced);
    public final Setting<Float> pullValue = new Setting<Float>("PullValue", Float.valueOf(3.0f), Float.valueOf(0.0f), Float.valueOf(20.0f), v -> this.pullDown.getValue()).addToGroup(this.advanced);
    public final Setting<AttackHand> attackHand = new Setting<AttackHand>("AttackHand", AttackHand.MainHand).addToGroup(this.advanced);
    public final Setting<Resolver> resolver = new Setting<Resolver>("Resolver", Resolver.Advantage).addToGroup(this.advanced);
    public final Setting<Integer> backTicks = new Setting<Integer>("BackTicks", 4, 1, 20, v -> this.resolver.is(Resolver.BackTrack)).addToGroup(this.advanced);
    public final Setting<Boolean> resolverVisualisation = new Setting<Boolean>("ResolverVisualisation", false, v -> !this.resolver.is(Resolver.Off)).addToGroup(this.advanced);
    public final Setting<AccelerateOnHit> accelerateOnHit = new Setting<AccelerateOnHit>("AccelerateOnHit", AccelerateOnHit.Off).addToGroup(this.advanced);
    public final Setting<Integer> minYawStep = new Setting<Integer>("MinYawStep", 65, 1, 180).addToGroup(this.advanced);
    public final Setting<Integer> maxYawStep = new Setting<Integer>("MaxYawStep", 75, 1, 180).addToGroup(this.advanced);
    public final Setting<Float> aimedPitchStep = new Setting<Float>("AimedPitchStep", Float.valueOf(1.0f), Float.valueOf(0.0f), Float.valueOf(90.0f)).addToGroup(this.advanced);
    public final Setting<Float> maxPitchStep = new Setting<Float>("MaxPitchStep", Float.valueOf(8.0f), Float.valueOf(1.0f), Float.valueOf(90.0f)).addToGroup(this.advanced);
    public final Setting<Float> pitchAccelerate = new Setting<Float>("PitchAccelerate", Float.valueOf(1.65f), Float.valueOf(1.0f), Float.valueOf(10.0f)).addToGroup(this.advanced);
    public final Setting<Float> attackCooldown = new Setting<Float>("AttackCooldown", Float.valueOf(0.9f), Float.valueOf(0.5f), Float.valueOf(1.0f)).addToGroup(this.advanced);
    public final Setting<Float> attackBaseTime = new Setting<Float>("AttackBaseTime", Float.valueOf(0.5f), Float.valueOf(0.0f), Float.valueOf(2.0f)).addToGroup(this.advanced);
    public final Setting<Integer> attackTickLimit = new Setting<Integer>("AttackTickLimit", 11, 0, 20).addToGroup(this.advanced);
    public final Setting<Float> critFallDistance = new Setting<Float>("CritFallDistance", Float.valueOf(0.08f), Float.valueOf(0.0f), Float.valueOf(1.0f)).addToGroup(this.advanced);
    public final Setting<Boolean> predict = new Setting<Boolean>("Predict", false).addToGroup(this.advanced);
    public final Setting<Float> predictMultiplier = new Setting<Float>("PredictMultiplier", Float.valueOf(3.0f), Float.valueOf(0.0f), Float.valueOf(10.0f), v -> this.predict.getValue()).addToGroup(this.advanced);
    public final Setting<Boolean> visualTurn = new Setting<Boolean>("VisualTurn", false).addToGroup(this.advanced);
    public final Setting<SettingGroup> targets = new Setting<SettingGroup>("Targets", new SettingGroup(false, 0));
    public final Setting<Boolean> Players = new Setting<Boolean>("Players", true).addToGroup(this.targets);
    public final Setting<Boolean> Mobs = new Setting<Boolean>("Mobs", true).addToGroup(this.targets);
    public final Setting<Boolean> Animals = new Setting<Boolean>("Animals", true).addToGroup(this.targets);
    public final Setting<Boolean> Villagers = new Setting<Boolean>("Villagers", true).addToGroup(this.targets);
    public final Setting<Boolean> Slimes = new Setting<Boolean>("Slimes", true).addToGroup(this.targets);
    public final Setting<Boolean> hostiles = new Setting<Boolean>("Hostiles", true).addToGroup(this.targets);
    public final Setting<Boolean> onlyAngry = new Setting<Boolean>("OnlyAngryHostiles", true, v -> this.hostiles.getValue()).addToGroup(this.targets);
    public final Setting<Boolean> Projectiles = new Setting<Boolean>("Projectiles", true).addToGroup(this.targets);
    public final Setting<Boolean> ignoreInvisible = new Setting<Boolean>("IgnoreInvisibleEntities", false).addToGroup(this.targets);
    public final Setting<Boolean> ignoreNamed = new Setting<Boolean>("IgnoreNamed", false).addToGroup(this.targets);
    public final Setting<Boolean> ignoreTeam = new Setting<Boolean>("IgnoreTeam", false).addToGroup(this.targets);
    public final Setting<Boolean> ignoreCreative = new Setting<Boolean>("IgnoreCreative", true).addToGroup(this.targets);
    public final Setting<Boolean> ignoreNaked = new Setting<Boolean>("IgnoreNaked", false).addToGroup(this.targets);
    public final Setting<Boolean> ignoreShield = new Setting<Boolean>("AttackShieldingEntities", true).addToGroup(this.targets);
    public static Entity target;
    public float rotationYaw;
    public float rotationPitch;
    public float pitchAcceleration = 1.0f;
    private Vec3d rotationPoint = Vec3d.ZERO;
    private Vec3d rotationMotion = Vec3d.ZERO;
    private int hitTicks;
    private int trackticks;
    private boolean lookingAtHitbox;
    private final Timer delayTimer = new Timer();
    private final Timer pauseTimer = new Timer();
    private World lastWorld;
    public Box resolvedBox;
    static boolean wasTargeted;

    public Aura() {
        super("Aura", Module.Category.COMBAT);
    }

    private float getRange() {
        return (this.elytra.getValue() != false && Aura.mc.player.isFallFlying() ? this.elytraAttackRange.getValue() : this.attackRange.getValue()).floatValue();
    }

    private float getWallRange() {
        return (this.elytra.getValue() != false && Aura.mc.player != null && Aura.mc.player.isFallFlying() ? this.elytraWallRange.getValue() : this.wallRange.getValue()).floatValue();
    }

    public void auraLogic() {
    boolean readyForAttack;
    if (!this.haveWeapon()) {
        target = null;
        return;
    }
    this.handleKill();
    this.updateTarget();
    if (target == null) {
        return;
    }
    if (!Aura.mc.options.jumpKey.isPressed() && Aura.mc.player.isOnGround() && this.autoJump.getValue().booleanValue() && this.smartCrit.getValue().isEnabled()) {
        if (target != null && this.isInRange(target) && this.getAttackCooldown() > 0.85f) {
            Aura.mc.player.jump();
        }
    }
    boolean isCritting = this.autoCrit();
    if (this.grimRayTrace.getValue().booleanValue()) {
        readyForAttack = isCritting && (this.lookingAtHitbox || this.skipRayTraceCheck());
        this.calcRotations(isCritting);
    } else {
        this.calcRotations(isCritting);
        readyForAttack = isCritting && (this.lookingAtHitbox || this.skipRayTraceCheck());
    }
    if (readyForAttack) {
        PlayerEntity pl;
        if (this.shieldBreaker(false)) {
            return;
        }
        boolean[] playerState = this.preAttack(isCritting);
        Entity class_12972 = target;
        if (!(class_12972 instanceof PlayerEntity) || !(pl = (PlayerEntity)class_12972).isUsingItem() || pl.getOffHandStack().getItem() != Items.SHIELD || this.ignoreShield.getValue().booleanValue()) {
            this.attack();
        }
        this.postAttack(playerState[0], playerState[1], isCritting);
    }
    }

    private boolean haveWeapon() {
        Item handItem = Aura.mc.player.getMainHandStack().getItem();
        if (this.onlyWeapon.getValue().booleanValue()) {
            if (this.switchMode.getValue() == Switch.None) {
                return handItem instanceof SwordItem || handItem instanceof AxeItem || handItem instanceof TridentItem;
            }
            return InventoryUtility.getSwordHotBar().found() || InventoryUtility.getAxeHotBar().found();
        }
        return true;
    }

    private boolean skipRayTraceCheck() {
        return this.rotationMode.getValue() == Mode.None || this.rayTrace.getValue() == RayTrace.OFF || this.rotationMode.is(Mode.Grim) || this.rotationMode.is(Mode.Interact) && (this.interactTicks.getValue() <= 1 || Aura.mc.world.getBlockCollisions((Entity)Aura.mc.player, Aura.mc.player.getBoundingBox().expand(-0.25, 0.0, -0.25).offset(0.0, 1.0, 0.0)).iterator().hasNext());
    }

    public void attack() {
        int prevSlot = this.switchMethod();
        Aura.mc.interactionManager.attackEntity((PlayerEntity)Aura.mc.player, target);
        this.swingHand();
        this.hitTicks = this.getHitTicks();
        if (prevSlot != -1) {
            if (this.switchMode.getValue() == Switch.Silent) {
                InventoryUtility.switchToSilent(prevSlot);
            } else {
                InventoryUtility.switchTo(prevSlot);
            }
            ModuleManager.itemSwapFix.forceSync();
        }
    }

    private boolean @NotNull [] preAttack(boolean isCritting) {
    boolean blocking = Aura.mc.player.isUsingItem() && Aura.mc.player.getActiveItem().getItem().getUseAction(Aura.mc.player.getActiveItem()) == UseAction.BLOCK;
    boolean sprint = Core.serverSprint;
    
    if (blocking && this.unpressShield.getValue().booleanValue()) {
        this.sendPacket((Packet<?>)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
    }
    
    if (this.rotationMode.is(Mode.Grim)) {
        this.sendPacket((Packet<?>)new PlayerMoveC2SPacket.Full(Aura.mc.player.getX(), Aura.mc.player.getY(), Aura.mc.player.getZ(), this.rotationYaw, this.rotationPitch, Aura.mc.player.isOnGround()));
    }
        return new boolean[]{blocking, sprint};
    }

    public void postAttack(boolean block, boolean sprint) {
    public void postAttack(boolean block, boolean sprint, boolean isCritting) {
    if (block && this.unpressShield.getValue().booleanValue()) {
        this.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id, this.rotationYaw, this.rotationPitch));
    }
    
    if (this.rotationMode.is(Mode.Grim)) {
        this.sendPacket((Packet<?>)new PlayerMoveC2SPacket.Full(Aura.mc.player.getX(), Aura.mc.player.getY(), Aura.mc.player.getZ(), Aura.mc.player.getYaw(), Aura.mc.player.getPitch(), Aura.mc.player.isOnGround()));
    }
    }

    
    private void disableSprint() {
    Aura.mc.player.setSprinting(false);
    this.sendPacket((Packet<?>)new ClientCommandC2SPacket((Entity)Aura.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
}

private void enableSprint() {
    Aura.mc.player.setSprinting(true);
    this.sendPacket((Packet<?>)new ClientCommandC2SPacket((Entity)Aura.mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
}

    public void resolvePlayers() {
        if (this.resolver.not(Resolver.Off)) {
            for (PlayerEntity player : Aura.mc.world.getPlayers()) {
                if (!(player instanceof OtherClientPlayerEntity)) continue;
                ((IOtherClientPlayerEntity)player).resolve(this.resolver.getValue());
            }
        }
    }

    public void restorePlayers() {
        if (this.resolver.not(Resolver.Off)) {
            for (PlayerEntity player : Aura.mc.world.getPlayers()) {
                if (!(player instanceof OtherClientPlayerEntity)) continue;
                ((IOtherClientPlayerEntity)player).releaseResolver();
            }
        }
    }

    public void handleKill() {
        if (target instanceof LivingEntity && (((LivingEntity)target).getHealth() <= 0.0f || ((LivingEntity)target).isDead())) {
            Managers.NOTIFICATION.publicity("Aura", ClientSettings.isRu() ? "\u0426\u0435\u043b\u044c \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043d\u0435\u0439\u0442\u0440\u0430\u043b\u0438\u0437\u043e\u0432\u0430\u043d\u0430!" : "Target successfully neutralized!", 3, Notification.Type.SUCCESS);
        }
    }

    private int switchMethod() {
        int prevSlot = -1;
        SearchInvResult swordResult = InventoryUtility.getSwordHotBar();
        if (swordResult.found() && this.switchMode.getValue() != Switch.None) {
            if (this.switchMode.getValue() == Switch.Silent) {
                prevSlot = Aura.mc.player.getInventory().selectedSlot;
                swordResult.switchToSilent();
            } else {
                swordResult.switchTo();
            }
        }
        return prevSlot;
    }

    private int getHitTicks() {
        return this.oldDelay.getValue().isEnabled() ? 1 + (int)(20.0f / MathUtility.random(this.minCPS.getValue().intValue(), this.maxCPS.getValue().intValue())) : (this.shouldRandomizeDelay() ? (int)MathUtility.random(11.0f, 13.0f) : this.attackTickLimit.getValue());
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent e) {
        if (Aura.mc.world != this.lastWorld) {
            target = null;
            this.lastWorld = Aura.mc.world;
        }
        if (!this.pauseTimer.passedMs(1000L)) {
            return;
        }
        if (Aura.mc.player.isUsingItem() && this.pauseWhileEating.getValue().booleanValue()) {
            return;
        }
        if (this.pauseBaritone.getValue().booleanValue() && ThunderHack.baritone) {
            boolean isTargeted = target != null;
            boolean bl = isTargeted;
            if (isTargeted && !wasTargeted) {
                BaritoneUtility.executeInternal("pause");
                wasTargeted = true;
            } else if (!isTargeted && wasTargeted) {
                BaritoneUtility.executeInternal("resume");
                wasTargeted = false;
            }
        }
        this.resolvePlayers();
        this.auraLogic();
        this.restorePlayers();
        --this.hitTicks;
    }

     @EventHandler
public void onSync(EventSync e) {
    if (!this.pauseTimer.passedMs(1000L)) {
        return;
    }
    if (Aura.mc.player.isUsingItem() && this.pauseWhileEating.getValue().booleanValue()) {
        return;
    }
    if (!this.haveWeapon()) {
        return;
    }
    if (target != null && this.rotationMode.getValue() != Mode.None && this.rotationMode.getValue() != Mode.Grim) {
        Aura.mc.player.setYaw(this.rotationYaw);
        Aura.mc.player.setPitch(this.rotationPitch);
    } else {
        this.rotationYaw = Aura.mc.player.getYaw();
        this.rotationPitch = Aura.mc.player.getPitch();
    }
    if (this.oldDelay.getValue().isEnabled() && this.minCPS.getValue() > this.maxCPS.getValue()) {
        this.minCPS.setValue(this.maxCPS.getValue());
    }
    if (target != null && this.pullDown.getValue().booleanValue() && (Aura.mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) || !this.onlyJumpBoost.getValue().booleanValue())) {
        if (Aura.mc.player.getVelocity().y < 0) {
            Aura.mc.player.setVelocity(Aura.mc.player.getVelocity().x, Aura.mc.player.getVelocity().y - 0.002, Aura.mc.player.getVelocity().z);
        }
    }
    if (this.sprintMode.getValue() == SprintMode.Legit && target != null && Aura.mc.player.isSprinting() && this.isInRange(target)) {
        Vec3d vel = Aura.mc.player.getVelocity();
        Aura.mc.player.setVelocity(vel.x * 0.6, vel.y, vel.z * 0.6);
    }
}

    @EventHandler
    public void onPacketSend(@NotNull PacketEvent.Send e) {
        PlayerInteractEntityC2SPacket pie;
        Object t = e.getPacket();
        if (t instanceof PlayerInteractEntityC2SPacket && Criticals.getInteractType(pie = (PlayerInteractEntityC2SPacket)t) != Criticals.InteractType.ATTACK && target != null) {
            e.cancel();
        }
    }

    @EventHandler
    public void onPacketReceive(@NotNull PacketEvent.Receive e) {
        EntityStatusS2CPacket pac;
        EntityStatusS2CPacket status;
        Object t = e.getPacket();
        if (t instanceof EntityStatusS2CPacket && (status = (EntityStatusS2CPacket)t).getStatus() == 30 && status.getEntity((World)Aura.mc.world) != null && target != null && status.getEntity((World)Aura.mc.world) == target) {
            Managers.NOTIFICATION.publicity("Aura", ClientSettings.isRu() ? "\u0423\u0441\u043f\u0435\u0448\u043d\u043e \u0441\u043b\u043e\u043c\u0430\u043b\u0438 \u0449\u0438\u0442 \u0438\u0433\u0440\u043e\u043a\u0443 " + target.getName().getString() : "Succesfully destroyed " + target.getName().getString() + "'s shield", 2, Notification.Type.SUCCESS);
        }
        if (e.getPacket() instanceof PlayerPositionLookS2CPacket && this.tpDisable.getValue().booleanValue()) {
            this.disable(ClientSettings.isRu() ? "\u041e\u0442\u043a\u043b\u044e\u0447\u0430\u044e \u0438\u0437-\u0437\u0430 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u0438!" : "Disabling due to tp!");
        }
        if ((t = e.getPacket()) instanceof EntityStatusS2CPacket && (pac = (EntityStatusS2CPacket)t).getStatus() == 3 && pac.getEntity((World)Aura.mc.world) == Aura.mc.player && this.deathDisable.getValue().booleanValue()) {
            this.disable(ClientSettings.isRu() ? "\u041e\u0442\u043a\u043b\u044e\u0447\u0430\u044e \u0438\u0437-\u0437\u0430 \u0441\u043c\u0435\u0440\u0442\u0438!" : "Disabling due to death!");
        }
    }

    @Override
    public void onEnable() {
        target = null;
        this.lookingAtHitbox = false;
        this.rotationPoint = Vec3d.ZERO;
        this.rotationMotion = Vec3d.ZERO;
        this.rotationYaw = Aura.mc.player.getYaw();
        this.rotationPitch = Aura.mc.player.getPitch();
        this.delayTimer.reset();
    }

    private boolean autoCrit() {
    if (this.sprintMode.getValue() == SprintMode.HvH) {
        return true;
    }
    
    boolean reasonForSkipCrit = !this.smartCrit.getValue().isEnabled() || Aura.mc.player.getAbilities().flying || Aura.mc.player.isFallFlying() || Aura.mc.player.hasStatusEffect(StatusEffects.BLINDNESS) || Aura.mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING) || Managers.PLAYER.isInWeb();
    if (this.hitTicks > 0) {
        return false;
    }
    if (this.pauseInInventory.getValue().booleanValue() && Managers.PLAYER.inInventory) {
        return false;
    }
    if (this.getAttackCooldown() < this.attackCooldown.getValue().floatValue() && !this.oldDelay.getValue().isEnabled()) {
        return false;
    }
    boolean mergeWithTargetStrafe = !ModuleManager.targetStrafe.isEnabled() || ModuleManager.targetStrafe.jump.getValue() == false;
    boolean mergeWithSpeed = !ModuleManager.speed.isEnabled() || Aura.mc.player.isOnGround();
    if (!Aura.mc.options.jumpKey.isPressed() && mergeWithTargetStrafe && mergeWithSpeed && !this.onlySpace.getValue().booleanValue() && !this.autoJump.getValue().booleanValue()) {
        return true;
    }
    if (Aura.mc.player.isInLava()) {
        return true;
    }
    if (Aura.mc.player.isSubmergedInWater()) {
        return true;
    }
    if (!Aura.mc.options.jumpKey.isPressed() && this.isAboveWater()) {
        return true;
    }
    if (reasonForSkipCrit) {
        return true;
    }
    if (Aura.mc.player.isOnGround()) {
        return false;
    }
    float fallDist = Aura.mc.player.fallDistance;
    float minFallDist = this.shouldRandomizeFallDistance() ? MathUtility.random(0.08f, 0.14f) : this.critFallDistance.getValue().floatValue();
    return Aura.mc.player.getVelocity().y < 0.0 && fallDist > minFallDist;
    }
    

    private boolean shieldBreaker(boolean instant) {
        int axeSlot = InventoryUtility.getAxe().slot();
        if (axeSlot == -1) {
            return false;
        }
        if (!this.shieldBreaker.getValue().booleanValue()) {
            return false;
        }
        if (!(target instanceof PlayerEntity)) {
            return false;
        }
        if (!((PlayerEntity)target).isUsingItem() && !instant) {
            return false;
        }
        if (((PlayerEntity)target).getOffHandStack().getItem() != Items.SHIELD && ((PlayerEntity)target).getMainHandStack().getItem() != Items.SHIELD) {
            return false;
        }
        if (axeSlot >= 9) {
            Aura.mc.interactionManager.clickSlot(Aura.mc.player.currentScreenHandler.syncId, axeSlot, Aura.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, (PlayerEntity)Aura.mc.player);
            this.sendPacket((Packet<?>)new CloseHandledScreenC2SPacket(Aura.mc.player.currentScreenHandler.syncId));
            Aura.mc.interactionManager.attackEntity((PlayerEntity)Aura.mc.player, target);
            this.swingHand();
            Aura.mc.interactionManager.clickSlot(Aura.mc.player.currentScreenHandler.syncId, axeSlot, Aura.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, (PlayerEntity)Aura.mc.player);
            this.sendPacket((Packet<?>)new CloseHandledScreenC2SPacket(Aura.mc.player.currentScreenHandler.syncId));
            ModuleManager.itemSwapFix.forceSync();
        } else {
            this.sendPacket((Packet<?>)new UpdateSelectedSlotC2SPacket(axeSlot));
            Aura.mc.interactionManager.attackEntity((PlayerEntity)Aura.mc.player, target);
            this.swingHand();
            this.sendPacket((Packet<?>)new UpdateSelectedSlotC2SPacket(Aura.mc.player.getInventory().selectedSlot));
            ModuleManager.itemSwapFix.forceSync();
        }
        this.hitTicks = 10;
        return true;
    }

    private void swingHand() {
        switch (this.attackHand.getValue().ordinal()) {
            case 1: {
                Aura.mc.player.swingHand(Hand.OFF_HAND);
                break;
            }
            case 0: {
                Aura.mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    public boolean isAboveWater() {
        Vec3d pos = Aura.mc.player.getPos().add(0.0, -0.4, 0.0);
        return Aura.mc.player.isSubmergedInWater() || Aura.mc.world.getBlockState(BlockPos.ofFloored((double)pos.x, (double)pos.y, (double)pos.z)).getBlock() == Blocks.WATER;
    }

    public float getAttackCooldownProgressPerTick() {
        return (float)(1.0 / Aura.mc.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED) * (20.0 * (double)ThunderHack.TICK_TIMER * (double)(this.tpsSync.getValue() != false ? Managers.SERVER.getTPSFactor() : 1.0f)));
    }

    public float getAttackCooldown() {
        return MathHelper.clamp((float)(((float)((ILivingEntity)Aura.mc.player).getLastAttackedTicks() + this.attackBaseTime.getValue().floatValue()) / this.getAttackCooldownProgressPerTick()), (float)0.0f, (float)1.0f);
    }

    private void updateTarget() {
        if (this.elytraTarget.getValue().booleanValue() && ElytraTarget.target != null) {
            if (this.lockTarget.getValue().booleanValue() && target != null && !this.skipEntity(target)) {
                return;
            }
            if (!this.skipEntity((Entity)ElytraTarget.target)) {
                target = ElytraTarget.target;
                return;
            }
        }
        if (this.lockTarget.getValue().booleanValue() && target != null && !this.skipEntity(target) && this.sort.getValue() != Sort.FOV) {
            return;
        }
        Entity candidat = this.findTarget();
        if (target == null || !this.lockTarget.getValue().booleanValue() || this.sort.getValue() == Sort.FOV) {
            target = candidat;
        }
        if (candidat instanceof ProjectileEntity) {
            target = candidat;
        }
        if (target != null && this.skipEntity(target)) {
            target = null;
        }
    }

    private void calcRotations(boolean ready) {
        float f2;
        float pitchStep = 0.0f;
        float yawStep = 0.0f;
        if (ready) {
            this.trackticks = Aura.mc.world.getBlockCollisions((Entity)Aura.mc.player, Aura.mc.player.getBoundingBox().expand(-0.25, 0.0, -0.25).offset(0.0, 1.0, 0.0)).iterator().hasNext() ? 1 : this.interactTicks.getValue();
        } else if (this.trackticks > 0) {
            --this.trackticks;
        }
        if (target == null) {
            return;
        }
        Vec3d targetVec = null;
        if (this.predict.getValue().booleanValue()) {
            double motionX = target.getX() - Aura.target.prevX;
            double motionY = target.getY() - Aura.target.prevY;
            double motionZ = target.getZ() - Aura.target.prevZ;
            targetVec = new Vec3d(target.getX() + motionX * (double)this.predictMultiplier.getValue().floatValue(), target.getY() + motionY * (double)this.predictMultiplier.getValue().floatValue() + (double)(target.getEyeHeight(target.getPose()) / 2.0f), target.getZ() + motionZ * (double)this.predictMultiplier.getValue().floatValue());
        } else {
            Vec3d vec3d = targetVec = Aura.mc.player.isFallFlying() ? target.getEyePos() : this.getLegitLook(target);
        }
        if (targetVec == null) {
            return;
        }
        this.pitchAcceleration = Managers.PLAYER.checkRtx(this.rotationYaw, this.rotationPitch, this.getRange() + this.aimRange.getValue().floatValue(), this.getRange() + this.aimRange.getValue().floatValue(), this.rayTrace.getValue()) ? this.aimedPitchStep.getValue().floatValue() : (this.pitchAcceleration < this.maxPitchStep.getValue().floatValue() ? this.pitchAcceleration * this.pitchAccelerate.getValue().floatValue() : this.maxPitchStep.getValue().floatValue());
        float delta_yaw = MathHelper.wrapDegrees((float)((float)MathHelper.wrapDegrees((double)(Math.toDegrees(Math.atan2(targetVec.z - Aura.mc.player.getZ(), targetVec.x - Aura.mc.player.getX())) - 90.0)) - this.rotationYaw)) + (float)(this.wallsBypass.is(WallsBypass.V2) && !ready && !Aura.mc.player.canSee(target) ? 20 : 0);
        float delta_pitch = (float)(-Math.toDegrees(Math.atan2(targetVec.y - (Aura.mc.player.getPos().y + (double)Aura.mc.player.getEyeHeight(Aura.mc.player.getPose())), Math.sqrt(Math.pow(targetVec.x - Aura.mc.player.getX(), 2.0) + Math.pow(targetVec.z - Aura.mc.player.getZ(), 2.0))))) - this.rotationPitch;
        yawStep = this.rotationMode.getValue() != Mode.Track ? 360.0f : MathUtility.random(this.minYawStep.getValue().intValue(), this.maxYawStep.getValue().intValue());
        float f = yawStep;
        float f3 = this.rotationMode.getValue() != Mode.Track ? 180.0f : (f2 = (pitchStep = Managers.PLAYER.ticksElytraFlying > 5 ? 180.0f : this.pitchAcceleration + MathUtility.random(-1.0f, 1.0f)));
        if (ready) {
            switch (this.accelerateOnHit.getValue().ordinal()) {
                case 1: {
                    yawStep = 180.0f;
                    break;
                }
                case 2: {
                    pitchStep = 90.0f;
                    break;
                }
                case 3: {
                    yawStep = 180.0f;
                    pitchStep = 90.0f;
                }
            }
        }
        if (delta_yaw > 180.0f) {
            delta_yaw -= 180.0f;
        }
        float deltaYaw = MathHelper.clamp((float)MathHelper.abs((float)delta_yaw), (float)(-yawStep), (float)yawStep);
        float deltaPitch = MathHelper.clamp((float)delta_pitch, (float)(-pitchStep), (float)pitchStep);
        float newYaw = this.rotationYaw + (delta_yaw > 0.0f ? deltaYaw : -deltaYaw);
        float newPitch = MathHelper.clamp((float)(this.rotationPitch + deltaPitch), (float)-90.0f, (float)90.0f);
        double gcdFix = Math.pow((Double)Aura.mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0) * 1.2;
        if (this.trackticks > 0 || this.rotationMode.getValue() == Mode.Track) {
            this.rotationYaw = (float)((double)newYaw - (double)(newYaw - this.rotationYaw) % gcdFix);
            this.rotationPitch = (float)((double)newPitch - (double)(newPitch - this.rotationPitch) % gcdFix);
        } else {
            this.rotationYaw = Aura.mc.player.getYaw();
            this.rotationPitch = Aura.mc.player.getPitch();
        }
        if (!this.rotationMode.is(Mode.Grim)) {
            ModuleManager.rotations.fixRotation = this.rotationYaw;
        }
        this.lookingAtHitbox = Managers.PLAYER.checkRtx(this.rotationYaw, this.rotationPitch, this.getRange(), this.getWallRange(), this.rayTrace.getValue());
        if (this.visualTurn.getValue().booleanValue()) {
            Aura.mc.player.headYaw = this.rotationYaw;
            Aura.mc.player.bodyYaw = this.rotationYaw;
        }
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        if (!this.haveWeapon() || target == null) {
            return;
        }
        if ((this.resolver.is(Resolver.BackTrack) || this.resolverVisualisation.getValue().booleanValue()) && this.resolvedBox != null) {
            Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(this.resolvedBox, HudEditor.getColor(0), 1.0f));
        }
        switch (this.esp.getValue().ordinal()) {
            case 3: {
                Render3DEngine.drawOldTargetEsp(stack, target);
                break;
            }
            case 2: {
                CaptureMark.render(target);
                break;
            }
            case 4: {
                Render3DEngine.renderGhosts(this.espLength.getValue(), this.espFactor.getValue(), this.espShaking.getValue().floatValue(), this.espAmplitude.getValue().floatValue(), target);
                break;
            }
            case 1: {
                Render3DEngine.drawTargetEsp(stack, target);
            }
        }
        if (this.clientLook.getValue().booleanValue() && this.rotationMode.getValue() != Mode.None) {
            Aura.mc.player.setYaw((float)Render2DEngine.interpolate(Aura.mc.player.prevYaw, this.rotationYaw, Render3DEngine.getTickDelta()));
            Aura.mc.player.setPitch((float)Render2DEngine.interpolate(Aura.mc.player.prevPitch, this.rotationPitch, Render3DEngine.getTickDelta()));
        }
    }

    @Override
    public void onDisable() {
        target = null;
    }

    public float getSquaredRotateDistance() {
        float dst = this.getRange();
        dst += this.aimRange.getValue().floatValue();
        if (Aura.mc.player.isFallFlying() || target instanceof PlayerEntity && ((PlayerEntity)target).isFallFlying()) {
            dst += 45.0f;
        }
        if (ModuleManager.strafe.isEnabled()) {
            dst += 10.0f;
        }
        if (this.rotationMode.getValue() != Mode.Track || this.rayTrace.getValue() == RayTrace.OFF) {
            dst = this.getRange();
        }
        return dst * dst;
    }

    public Vec3d getLegitLook(Entity target) {
        float minMotionXZ = 0.003f;
        float maxMotionXZ = 0.03f;
        float minMotionY = 0.001f;
        float maxMotionY = 0.03f;
        double lenghtX = target.getBoundingBox().getLengthX();
        double lenghtY = target.getBoundingBox().getLengthY();
        double lenghtZ = target.getBoundingBox().getLengthZ();
        if (this.rotationMotion.equals(Vec3d.ZERO)) {
            this.rotationMotion = new Vec3d((double)MathUtility.random(-0.05f, 0.05f), (double)MathUtility.random(-0.05f, 0.05f), (double)MathUtility.random(-0.05f, 0.05f));
        }
        this.rotationPoint = this.rotationPoint.add(this.rotationMotion);
        if (this.rotationPoint.x >= (lenghtX - 0.05) / 2.0) {
            this.rotationMotion = new Vec3d((double)(-MathUtility.random(minMotionXZ, maxMotionXZ)), this.rotationMotion.getY(), this.rotationMotion.getZ());
        }
        if (this.rotationPoint.y >= lenghtY) {
            this.rotationMotion = new Vec3d(this.rotationMotion.getX(), (double)(-MathUtility.random(minMotionY, maxMotionY)), this.rotationMotion.getZ());
        }
        if (this.rotationPoint.z >= (lenghtZ - 0.05) / 2.0) {
            this.rotationMotion = new Vec3d(this.rotationMotion.getX(), this.rotationMotion.getY(), (double)(-MathUtility.random(minMotionXZ, maxMotionXZ)));
        }
        if (this.rotationPoint.x <= -(lenghtX - 0.05) / 2.0) {
            this.rotationMotion = new Vec3d((double)MathUtility.random(minMotionXZ, 0.03f), this.rotationMotion.getY(), this.rotationMotion.getZ());
        }
        if (this.rotationPoint.y <= 0.05) {
            this.rotationMotion = new Vec3d(this.rotationMotion.getX(), (double)MathUtility.random(minMotionY, maxMotionY), this.rotationMotion.getZ());
        }
        if (this.rotationPoint.z <= -(lenghtZ - 0.05) / 2.0) {
            this.rotationMotion = new Vec3d(this.rotationMotion.getX(), this.rotationMotion.getY(), (double)MathUtility.random(minMotionXZ, maxMotionXZ));
        }
        this.rotationPoint.add((double)MathUtility.random(-0.03f, 0.03f), 0.0, (double)MathUtility.random(-0.03f, 0.03f));
        if (!Aura.mc.player.canSee(target) && Objects.requireNonNull(this.wallsBypass.getValue()) == WallsBypass.V1) {
            return target.getPos().add(MathUtility.random(-0.15, 0.15), lenghtY, MathUtility.random(-0.15, 0.15));
        }
        if (!Managers.PLAYER.checkRtx(this.rotationYaw, this.rotationPitch, this.getRange(), this.getWallRange(), this.rayTrace.getValue())) {
            float[] rotation1 = PlayerManager.calcAngle(target.getPos().add(0.0, (double)(target.getEyeHeight(target.getPose()) / 2.0f), 0.0));
            if (PlayerUtility.squaredDistanceFromEyes(target.getPos().add(0.0, (double)(target.getEyeHeight(target.getPose()) / 2.0f), 0.0)) <= this.attackRange.getPow2Value() && Managers.PLAYER.checkRtx(rotation1[0], rotation1[1], this.getRange(), 0.0f, this.rayTrace.getValue())) {
                this.rotationPoint = new Vec3d((double)MathUtility.random(-0.1f, 0.1f), (double)(target.getEyeHeight(target.getPose()) / MathUtility.random(1.8f, 2.5f)), (double)MathUtility.random(-0.1f, 0.1f));
            } else {
                float halfBox = (float)(lenghtX / 2.0);
                for (float x1 = -halfBox; x1 <= halfBox; x1 += 0.05f) {
                    block1: for (float z1 = -halfBox; z1 <= halfBox; z1 += 0.05f) {
                        float y1 = 0.05f;
                        while ((double)y1 <= target.getBoundingBox().getLengthY()) {
                            float[] rotation;
                            Vec3d v1 = new Vec3d(target.getX() + (double)x1, target.getY() + (double)y1, target.getZ() + (double)z1);
                            if (!(PlayerUtility.squaredDistanceFromEyes(v1) > this.attackRange.getPow2Value()) && Managers.PLAYER.checkRtx((rotation = PlayerManager.calcAngle(v1))[0], rotation[1], this.getRange(), 0.0f, this.rayTrace.getValue())) {
                                this.rotationPoint = new Vec3d((double)x1, (double)y1, (double)z1);
                                continue block1;
                            }
                            y1 += 0.15f;
                        }
                    }
                }
            }
        }
        return target.getPos().add(this.rotationPoint);
    }

    public boolean isInRange(Entity target) {
        if (PlayerUtility.squaredDistanceFromEyes(target.getPos().add(0.0, (double)target.getEyeHeight(target.getPose()), 0.0)) > this.getSquaredRotateDistance() + 4.0f) {
            return false;
        }
        float halfBox = (float)(target.getBoundingBox().getLengthX() / 2.0);
        for (float x1 = -halfBox; x1 <= halfBox; x1 += 0.15f) {
            for (float z1 = -halfBox; z1 <= halfBox; z1 += 0.15f) {
                float y1 = 0.05f;
                while ((double)y1 <= target.getBoundingBox().getLengthY()) {
                    float[] rotation;
                    if (!(PlayerUtility.squaredDistanceFromEyes(new Vec3d(target.getX() + (double)x1, target.getY() + (double)y1, target.getZ() + (double)z1)) > this.getSquaredRotateDistance()) && Managers.PLAYER.checkRtx((rotation = PlayerManager.calcAngle(new Vec3d(target.getX() + (double)x1, target.getY() + (double)y1, target.getZ() + (double)z1)))[0], rotation[1], (float)Math.sqrt(this.getSquaredRotateDistance()), this.getWallRange(), this.rayTrace.getValue())) {
                        return true;
                    }
                    y1 += 0.25f;
                }
            }
        }
        return false;
    }

    public Entity findTarget() {
        CopyOnWriteArrayList<LivingEntity> first_stage = new CopyOnWriteArrayList<LivingEntity>();
        for (Entity ent : Aura.mc.world.getEntities()) {
            if ((ent instanceof ShulkerBulletEntity || ent instanceof FireballEntity) && ent.isAlive() && this.isInRange(ent) && this.Projectiles.getValue().booleanValue()) {
                return ent;
            }
            if (this.skipEntity(ent) || !(ent instanceof LivingEntity)) continue;
            first_stage.add((LivingEntity)ent);
        }
        return switch (this.sort.getValue().ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> first_stage.stream().min(Comparator.comparing(e -> Aura.mc.player.squaredDistanceTo(e.getPos()))).orElse(null);
            case 1 -> first_stage.stream().max(Comparator.comparing(e -> Aura.mc.player.squaredDistanceTo(e.getPos()))).orElse(null);
            case 6 -> first_stage.stream().min(Comparator.comparing(this::getFOVAngle)).orElse(null);
            case 2 -> first_stage.stream().min(Comparator.comparing(e -> Float.valueOf(e.getHealth() + e.getAbsorptionAmount()))).orElse(null);
            case 3 -> first_stage.stream().max(Comparator.comparing(e -> Float.valueOf(e.getHealth() + e.getAbsorptionAmount()))).orElse(null);
            case 4 -> first_stage.stream().min(Comparator.comparing(e -> {
                float v = 0.0f;
                for (ItemStack armor : e.getArmorItems()) {
                    if (armor == null || armor.getItem().equals(Items.AIR)) continue;
                    v += (float)(armor.getMaxDamage() - armor.getDamage()) / (float)armor.getMaxDamage();
                }
                return Float.valueOf(v);
            })).orElse(null);
            case 5 -> first_stage.stream().max(Comparator.comparing(e -> {
                float v = 0.0f;
                for (ItemStack armor : e.getArmorItems()) {
                    if (armor == null || armor.getItem().equals(Items.AIR)) continue;
                    v += (float)(armor.getMaxDamage() - armor.getDamage()) / (float)armor.getMaxDamage();
                }
                return Float.valueOf(v);
            })).orElse(null);
        };
    }

    private boolean skipEntity(Entity entity) {
        if (this.isBullet(entity)) {
            return false;
        }
        if (!(entity instanceof LivingEntity)) {
            return true;
        }
        LivingEntity ent = (LivingEntity)entity;
        if (ent.isDead() || !entity.isAlive() || entity.isRemoved() || entity.getWorld() != Aura.mc.world) {
            return true;
        }
        if (entity instanceof ArmorStandEntity) {
            return true;
        }
        if (entity instanceof CatEntity) {
            return true;
        }
        if (this.skipNotSelected(entity)) {
            return true;
        }
        if (!InteractionUtility.isVecInFOV(ent.getPos(), this.fov.getValue())) {
            return true;
        }
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            if (ModuleManager.antiBot.isEnabled() && AntiBot.bots.contains(entity)) {
                return true;
            }
            if (player == Aura.mc.player || Managers.FRIEND.isFriend(player)) {
                return true;
            }
            if (player.isCreative() && this.ignoreCreative.getValue().booleanValue()) {
                return true;
            }
            if (player.getArmor() == 0 && this.ignoreNaked.getValue().booleanValue()) {
                return true;
            }
            if (player.isInvisible() && this.ignoreInvisible.getValue().booleanValue()) {
                return true;
            }
            if (player.getTeamColorValue() == Aura.mc.player.getTeamColorValue() && this.ignoreTeam.getValue().booleanValue() && Aura.mc.player.getTeamColorValue() != 0xFFFFFF) {
                return true;
            }
        }
        return !this.isInRange(entity) || entity.hasCustomName() && this.ignoreNamed.getValue() != false;
    }

    private boolean isBullet(Entity entity) {
        return (entity instanceof ShulkerBulletEntity || entity instanceof FireballEntity) && entity.isAlive() && PlayerUtility.squaredDistanceFromEyes(entity.getPos()) < this.getSquaredRotateDistance() && this.Projectiles.getValue() != false;
    }

    private boolean skipNotSelected(Entity entity) {
        if (entity instanceof SlimeEntity && !this.Slimes.getValue().booleanValue()) {
            return true;
        }
        if (entity instanceof HostileEntity) {
            HostileEntity he = (HostileEntity)entity;
            if (!this.hostiles.getValue().booleanValue()) {
                return true;
            }
            if (this.onlyAngry.getValue().booleanValue()) {
                return !he.isAngryAt((PlayerEntity)Aura.mc.player);
            }
        }
        if (entity instanceof PlayerEntity && !this.Players.getValue().booleanValue()) {
            return true;
        }
        if (entity instanceof VillagerEntity && !this.Villagers.getValue().booleanValue()) {
            return true;
        }
        if (entity instanceof MobEntity && !this.Mobs.getValue().booleanValue()) {
            return true;
        }
        return entity instanceof AnimalEntity && this.Animals.getValue() == false;
    }

    private float getFOVAngle(@NotNull LivingEntity e) {
        double difX = e.getX() - Aura.mc.player.getX();
        double difZ = e.getZ() - Aura.mc.player.getZ();
        float yaw = (float)MathHelper.wrapDegrees((double)(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0));
        return Math.abs(yaw - MathHelper.wrapDegrees((float)Aura.mc.player.getYaw()));
    }

    public void pause() {
        this.pauseTimer.reset();
    }

    private boolean shouldRandomizeDelay() {
        return this.randomHitDelay.getValue() != false && (Aura.mc.player.isOnGround() || Aura.mc.player.fallDistance < 0.12f || Aura.mc.player.isSwimming() || Aura.mc.player.isFallFlying());
    }

    private boolean shouldRandomizeFallDistance() {
        return this.randomHitDelay.getValue() != false && !this.shouldRandomizeDelay();
    }

    static {
        wasTargeted = false;
    }

    public static enum WallsBypass {
        Off,
        V1,
        V2;

    }

    public static enum Mode {
        Interact,
        Track,
        Grim,
        None;

    }

    public static enum Switch {
        Normal,
        None,
        Silent;

    }

    public static enum ESP {
        Off,
        ThunderHack,
        NurikZapen,
        CelkaPasta,
        ThunderHackV2;

    }

    public static enum Sort {
        LowestDistance,
        HighestDistance,
        LowestHealth,
        HighestHealth,
        LowestDurability,
        HighestDurability,
        FOV;

    }

    public static enum RayTrace {
        OFF,
        OnlyTarget,
        AllEntities;

    }

    public static enum AttackHand {
        MainHand,
        OffHand,
        None;

    }

    public static enum Resolver {
        Off,
        Advantage,
        Predictive,
        BackTrack;

    }

    public static enum AccelerateOnHit {
        Off,
        Yaw,
        Pitch,
        Both;

    }

    public static class Position {
        private double x;
        private double y;
        private double z;
        private int ticks;

        public Position(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public boolean shouldRemove() {
            return this.ticks++ > ModuleManager.aura.backTicks.getValue();
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }

        public double getZ() {
            return this.z;
        }
    }
}

public static enum SprintMode {
    HvH,
    Legit;
}
