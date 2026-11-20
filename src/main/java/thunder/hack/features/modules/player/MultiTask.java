/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.EntityHitResult
 *  net.minecraft.util.hit.HitResult
 */
package thunder.hack.features.modules.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import thunder.hack.features.modules.Module;

public class MultiTask
extends Module {
    public MultiTask() {
        super("MultiTask", Module.Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        EntityHitResult ehr;
        BlockHitResult crossHair;
        HitResult ItemStackParticleEffect = MultiTask.mc.crosshairTarget;
        if (ItemStackParticleEffect instanceof BlockHitResult && (crossHair = (BlockHitResult)ItemStackParticleEffect).getBlockPos() != null && MultiTask.mc.options.attackKey.isPressed() && !MultiTask.mc.world.getBlockState(crossHair.getBlockPos()).isAir()) {
            MultiTask.mc.interactionManager.attackBlock(crossHair.getBlockPos(), crossHair.getSide());
            MultiTask.mc.player.swingHand(Hand.MAIN_HAND);
        }
        if ((ItemStackParticleEffect = MultiTask.mc.crosshairTarget) instanceof EntityHitResult && (ehr = (EntityHitResult)ItemStackParticleEffect).getEntity() != null && MultiTask.mc.options.attackKey.isPressed() && MultiTask.mc.player.getAttackCooldownProgress(0.5f) > 0.9f) {
            MultiTask.mc.interactionManager.attackEntity((PlayerEntity)MultiTask.mc.player, ehr.getEntity());
            MultiTask.mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}

