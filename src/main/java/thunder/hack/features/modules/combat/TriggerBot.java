/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  meteordevelopment.orbit.EventHandler
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Position
 */
package thunder.hack.features.modules.combat;

import java.util.Random;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;

public final class TriggerBot
extends Module {
    public final Setting<Float> attackRange = new Setting<Float>("Range", Float.valueOf(3.0f), Float.valueOf(1.0f), Float.valueOf(7.0f));
    public final Setting<BooleanSettingGroup> smartCrit = new Setting<BooleanSettingGroup>("SmartCrit", new BooleanSettingGroup(true));
    public final Setting<Boolean> onlySpace = new Setting<Boolean>("OnlyCrit", false).addToGroup(this.smartCrit);
    public final Setting<Boolean> autoJump = new Setting<Boolean>("AutoJump", false).addToGroup(this.smartCrit);
    public final Setting<Boolean> ignoreWalls = new Setting<Boolean>("IgnoreWalls", false);
    public final Setting<Boolean> pauseEating = new Setting<Boolean>("PauseWhileEating", false);
    public final Setting<Integer> minDelay = new Setting<Integer>("RandomDelayMin", 2, 0, 20);
    public final Setting<Integer> maxDelay = new Setting<Integer>("RandomDelayMax", 13, 0, 20);
    private int delay;
    private final Random random = new Random();

    public TriggerBot() {
        super("TriggerBot", Module.Category.COMBAT);
    }

    @EventHandler
    public void onAttack(PlayerUpdateEvent e) {
        if (TriggerBot.mc.player.isUsingItem() && this.pauseEating.getValue().booleanValue()) {
            return;
        }
        if (!TriggerBot.mc.options.jumpKey.isPressed() && TriggerBot.mc.player.isOnGround() && this.autoJump.getValue().booleanValue()) {
            TriggerBot.mc.player.jump();
        }
        if (!this.autoCrit() && this.delay > 0) {
            --this.delay;
            return;
        }
        Entity ent = Managers.PLAYER.getRtxTarget(TriggerBot.mc.player.getYaw(), TriggerBot.mc.player.getPitch(), this.attackRange.getValue().floatValue(), this.ignoreWalls.getValue());
        if (ent != null && !Managers.FRIEND.isFriend(ent.getName().getString())) {
            TriggerBot.mc.interactionManager.attackEntity((PlayerEntity)TriggerBot.mc.player, ent);
            TriggerBot.mc.player.swingHand(Hand.MAIN_HAND);
            this.delay = this.random.nextInt(this.minDelay.getValue(), this.maxDelay.getValue() + 1);
        }
    }

    private boolean autoCrit() {
        float f2;
        boolean reasonForSkipCrit = !this.smartCrit.getValue().isEnabled() || TriggerBot.mc.player.getAbilities().flying || TriggerBot.mc.player.isFallFlying() || TriggerBot.mc.player.hasStatusEffect(StatusEffects.BLINDNESS) || TriggerBot.mc.player.isHoldingOntoLadder() || TriggerBot.mc.world.getBlockState(BlockPos.ofFloored((Position)TriggerBot.mc.player.getPos())).getBlock() == Blocks.COBWEB;
        boolean bl = reasonForSkipCrit;
        if (TriggerBot.mc.player.fallDistance > 1.0f && (double)TriggerBot.mc.player.fallDistance < 1.14) {
            return false;
        }
        float f = ModuleManager.aura.getAttackCooldown();
        float f3 = f2 = TriggerBot.mc.player.isOnGround() ? 1.0f : 0.9f;
        if (f < f2) {
            return false;
        }
        boolean mergeWithTargetStrafe = !ModuleManager.targetStrafe.isEnabled() || ModuleManager.targetStrafe.jump.getValue() == false;
        boolean mergeWithSpeed = !ModuleManager.speed.isEnabled() || TriggerBot.mc.player.isOnGround();
        boolean bl2 = mergeWithSpeed;
        if (!TriggerBot.mc.options.jumpKey.isPressed() && mergeWithTargetStrafe && mergeWithSpeed && !this.onlySpace.getValue().booleanValue() && !this.autoJump.getValue().booleanValue()) {
            return true;
        }
        if (TriggerBot.mc.player.isInLava()) {
            return true;
        }
        if (!TriggerBot.mc.options.jumpKey.isPressed() && ModuleManager.aura.isAboveWater()) {
            return true;
        }
        if (!reasonForSkipCrit) {
            return !TriggerBot.mc.player.isOnGround() && TriggerBot.mc.player.fallDistance > 0.0f;
        }
        return true;
    }
}

