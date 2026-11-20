/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.math.Vec3d
 */
package thunder.hack.features.modules.render;

import java.awt.Color;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render3DEngine;

public class Tracers
extends Module {
    private final Setting<Float> height = new Setting<Float>("Height", Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(2.0f));
    private final Setting<ColorSetting> color = new Setting<ColorSetting>("Color", new ColorSetting(new Color(-1812004864, true)));
    private final Setting<ColorSetting> friendColor = new Setting<ColorSetting>("Friends", new ColorSetting(new Color(-1827152291, true)));

    public Tracers() {
        super("Tracers", Module.Category.RENDER);
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        for (PlayerEntity playerEntity : Managers.ASYNC.getAsyncPlayers()) {
            if (playerEntity == Tracers.mc.player) continue;
            Color color1 = this.color.getValue().getColorObject();
            if (Managers.FRIEND.isFriend(playerEntity)) {
                color1 = this.friendColor.getValue().getColorObject();
            }
            double x1 = Tracers.mc.player.prevX + (Tracers.mc.player.getX() - Tracers.mc.player.prevX) * (double)Render3DEngine.getTickDelta();
            double y1 = (double)Tracers.mc.player.getEyeHeight(Tracers.mc.player.getPose()) + Tracers.mc.player.prevY + (Tracers.mc.player.getY() - Tracers.mc.player.prevY) * (double)Render3DEngine.getTickDelta();
            double z1 = Tracers.mc.player.prevZ + (Tracers.mc.player.getZ() - Tracers.mc.player.prevZ) * (double)Render3DEngine.getTickDelta();
            Vec3d vec2 = new Vec3d(0.0, 0.0, 75.0).rotateX(-((float)Math.toRadians(Tracers.mc.gameRenderer.getCamera().getPitch()))).rotateY(-((float)Math.toRadians(Tracers.mc.gameRenderer.getCamera().getYaw()))).add(x1, y1, z1);
            double x = playerEntity.prevX + (playerEntity.getX() - playerEntity.prevX) * (double)Render3DEngine.getTickDelta();
            double y = playerEntity.prevY + (playerEntity.getY() - playerEntity.prevY) * (double)Render3DEngine.getTickDelta();
            double z = playerEntity.prevZ + (playerEntity.getZ() - playerEntity.prevZ) * (double)Render3DEngine.getTickDelta();
            Render3DEngine.drawLineDebug(vec2, new Vec3d(x, y + (double)this.height.getValue().floatValue(), z), color1);
        }
    }
}

