/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.item.BlockItem
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.math.Box
 */
package thunder.hack.features.modules.player;

import java.awt.Color;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.render.Render3DEngine;

public class AirPlace
extends Module {
    private final Setting<Float> range = new Setting<Float>("Range", Float.valueOf(5.0f), Float.valueOf(0.0f), Float.valueOf(6.0f));
    private final Setting<SettingGroup> renderGroup = new Setting<SettingGroup>("Render", new SettingGroup(false, 0));
    private final Setting<Boolean> swing = new Setting<Boolean>("Swing", true).addToGroup(this.renderGroup);
    private final Setting<ColorSetting> fillColor = new Setting<ColorSetting>("Fill Color", new ColorSetting(new Color(100, 50, 255, 50))).addToGroup(this.renderGroup);
    private final Setting<ColorSetting> lineColor = new Setting<ColorSetting>("Line Color", new ColorSetting(new Color(100, 50, 255, 150))).addToGroup(this.renderGroup);
    private final Setting<Integer> lineWidth = new Setting<Integer>("Line Width", 2, 1, 5).addToGroup(this.renderGroup);
    private BlockHitResult hit;
    private int cooldown;

    public AirPlace() {
        super("AirPlace", Module.Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        BlockHitResult bhr;
        HitResult hitResult;
        if (this.cooldown > 0) {
            --this.cooldown;
        }
        if (!((hitResult = mc.getCameraEntity().raycast((double)this.range.getValue().floatValue(), 0.0f, false)) instanceof BlockHitResult)) {
            return;
        }
        this.hit = bhr = (BlockHitResult)hitResult;
        boolean main = AirPlace.mc.player.getMainHandStack().getItem() instanceof BlockItem;
        boolean off = AirPlace.mc.player.getOffHandStack().getItem() instanceof BlockItem;
        if (AirPlace.mc.options.useKey.isPressed() && (main || off) && this.cooldown <= 0) {
            AirPlace.mc.interactionManager.interactBlock(AirPlace.mc.player, main ? Hand.MAIN_HAND : Hand.OFF_HAND, this.hit);
            if (this.swing.getValue().booleanValue()) {
                AirPlace.mc.player.swingHand(main ? Hand.MAIN_HAND : Hand.OFF_HAND);
            } else {
                this.sendPacket((Packet<?>)new HandSwingC2SPacket(main ? Hand.MAIN_HAND : Hand.OFF_HAND));
            }
            this.cooldown = ModuleManager.fastUse.isEnabled() && (ModuleManager.fastUse.blocks.getValue() != false || ModuleManager.fastUse.all.getValue() != false) ? 0 : 4;
        }
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        if (this.hit == null || !AirPlace.mc.world.getBlockState(this.hit.getBlockPos()).getBlock().equals(Blocks.AIR) || !(AirPlace.mc.player.getMainHandStack().getItem() instanceof BlockItem) && !(AirPlace.mc.player.getOffHandStack().getItem() instanceof BlockItem)) {
            return;
        }
        Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(new Box(this.hit.getBlockPos()), this.fillColor.getValue().getColorObject()));
        Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(new Box(this.hit.getBlockPos()), this.lineColor.getValue().getColorObject(), this.lineWidth.getValue().intValue()));
    }
}

