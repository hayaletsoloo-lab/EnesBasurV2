/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  meteordevelopment.orbit.EventHandler
 *  net.minecraft.client.network.PendingUpdateManager
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.World
 */
package thunder.hack.features.modules.combat;

import java.awt.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.EventTick;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.player.ElytraHelper;
import thunder.hack.injection.accesors.IClientWorldMixin;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render3DEngine;

public class ElytraTarget
extends Module {
    public final Setting<Boolean> drawBox = new Setting<Boolean>("Draw Box", true);
    public final Setting<ColorSetting> boxColor = new Setting<ColorSetting>("Box Color", new ColorSetting(new Color(0, 0, 255, 120).getRGB()));
    public final Setting<Float> distanceInFront = new Setting<Float>("DistanceInFront", Float.valueOf(2.5f), Float.valueOf(0.0f), Float.valueOf(20.0f));
    public final Setting<Float> distance = new Setting<Float>("Distance", Float.valueOf(100.0f), Float.valueOf(10.0f), Float.valueOf(400.0f));
    public final Setting<Boolean> autoFirework = new Setting<Boolean>("Auto Firework", true);
    public final Setting<Integer> fireworkDelay = new Setting<Integer>("Firework Delay", 1000, 200, 5000, v -> this.autoFirework.getValue());
    public static PlayerEntity target = null;
    public static Vec3d predictedPos = null;
    private World lastWorld;
    private long lastFireworkTime = 0L;

    public ElytraTarget() {
        super("ElytraTarget", Module.Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (ElytraTarget.mc.player == null || ElytraTarget.mc.world == null) {
            target = null;
            predictedPos = null;
            return;
        }
        if (ElytraTarget.mc.world != this.lastWorld) {
            target = null;
            this.lastWorld = ElytraTarget.mc.world;
        }
        if ((target = this.findTarget()) != null) {
            predictedPos = this.getFrontPosition((Entity)target, this.distanceInFront.getValue().floatValue());
            this.useFireworkIfNeeded();
        } else {
            predictedPos = null;
        }
    }

    private PlayerEntity findTarget() {
        PlayerEntity bestTarget = null;
        float closestDistance = this.distance.getValue().floatValue();
        for (PlayerEntity player : ElytraTarget.mc.world.getPlayers()) {
            float d;
            boolean isFlying;
            if (player == ElytraTarget.mc.player || !player.isAlive() || player.isRemoved() || player.getHealth() <= 0.0f || Managers.FRIEND.isFriend(player) || !(isFlying = player.isFallFlying() || player.getInventory().getArmorStack(2).isOf(Items.ELYTRA) && !player.isOnGround()) || !((d = ElytraTarget.mc.player.distanceTo((Entity)player)) < closestDistance)) continue;
            bestTarget = player;
            closestDistance = d;
        }
        return bestTarget;
    }

    private boolean skipEntity(Entity entity) {
        if (!(entity instanceof PlayerEntity)) {
            return true;
        }
        PlayerEntity player = (PlayerEntity)entity;
        return player.isRemoved() || !player.isAlive() || player.getHealth() <= 0.0f || !player.isFallFlying() || Managers.FRIEND.isFriend(player) || ElytraTarget.mc.player.distanceTo((Entity)player) > this.distance.getValue().floatValue();
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        if (target != null && predictedPos != null && this.drawBox.getValue().booleanValue()) {
            Box targetBox = target.getBoundingBox();
            double w = (targetBox.maxX - targetBox.minX) / 2.0;
            double h = targetBox.maxY - targetBox.minY;
            Box box = new Box(ElytraTarget.predictedPos.x - w, ElytraTarget.predictedPos.y, ElytraTarget.predictedPos.z - w, ElytraTarget.predictedPos.x + w, ElytraTarget.predictedPos.y + h, ElytraTarget.predictedPos.z + w);
            Color color = this.boxColor.getValue().getColorObject();
            Render3DEngine.drawFilledBox(stack, box, color);
            Render3DEngine.drawBoxOutline(box, color, 2.0f);
        }
    }

    private Vec3d getFrontPosition(Entity target, double distanceInFront) {
        Vec3d targetPos = target.getPos();
        Vec3d lookVec = target.getRotationVector();
        Vec3d horizontalLookVec = new Vec3d(lookVec.x, 0.0, lookVec.z).normalize();
        Vec3d frontPos = targetPos.add(horizontalLookVec.multiply(distanceInFront));
        return new Vec3d(frontPos.x, targetPos.y + 1.2, frontPos.z);
    }

    private void useFireworkIfNeeded() {
        if (ElytraTarget.mc.player == null || ElytraTarget.mc.world == null || !this.autoFirework.getValue().booleanValue()) {
            return;
        }
        if (!ElytraTarget.mc.player.isFallFlying()) {
            return;
        }
        if (System.currentTimeMillis() - this.lastFireworkTime < (long)this.fireworkDelay.getValue().intValue()) {
            return;
        }
        int fireworkSlot = -1;
        for (int i = 0; i < 9; ++i) {
            if (ElytraTarget.mc.player.getInventory().getStack(i).getItem() != Items.FIREWORK_ROCKET) continue;
            fireworkSlot = i;
            break;
        }
        if (fireworkSlot == -1) {
            return;
        }
        String fireworkMode = ElytraHelper.getInstance().getFireWorkMode();
        boolean shouldSwing = ElytraHelper.getInstance().isSwingEnabled();
        int oldSlot = ElytraTarget.mc.player.getInventory().selectedSlot;
        if (fireworkMode.equalsIgnoreCase("Legit")) {
            ElytraTarget.mc.player.getInventory().selectedSlot = fireworkSlot;
            ElytraTarget.mc.interactionManager.interactItem((PlayerEntity)ElytraTarget.mc.player, Hand.MAIN_HAND);
            if (shouldSwing) {
                ElytraTarget.mc.player.swingHand(Hand.MAIN_HAND);
            }
        } else if (fireworkMode.equalsIgnoreCase("Silent")) {
            ElytraTarget.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(fireworkSlot));
            try (PendingUpdateManager pendingUpdateManager = ((IClientWorldMixin)ElytraTarget.mc.world).getPendingUpdateManager().incrementSequence();){
                int sequence = pendingUpdateManager.getSequence();
                ElytraTarget.mc.player.networkHandler.sendPacket((Packet)new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, ElytraTarget.mc.player.getYaw(), ElytraTarget.mc.player.getPitch()));
            }
            if (shouldSwing) {
                ElytraTarget.mc.player.networkHandler.sendPacket((Packet)new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
            ElytraTarget.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(oldSlot));
        }
        this.lastFireworkTime = System.currentTimeMillis();
    }
}

