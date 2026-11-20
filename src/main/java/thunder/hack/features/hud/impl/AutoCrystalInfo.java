/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.client.render.Tessellator
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.util.Formatting
 */
package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Collections;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Formatting;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;

public class AutoCrystalInfo
extends HudElement {
    private final ArrayDeque<Integer> speeds = new ArrayDeque(20);
    private int max;
    private int min;
    private long time;

    public AutoCrystalInfo() {
        super("AutoCrystalInfo", 175, 80);
    }

    @Override
    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        Render2DEngine.drawHudBase(context.getMatrices(), this.getPosX(), this.getPosY(), this.getWidth(), this.getHeight(), HudEditor.hudRound.getValue().floatValue());
        Color c1 = HudEditor.getColor(0).darker().darker().darker();
        Color c2 = HudEditor.getColor(0);
        Render2DEngine.drawRect(context.getMatrices(), this.getPosX() + 2.0f, this.getPosY() + 14.0f, 96.0f, 64.0f, HudEditor.hudRound.getValue().floatValue(), 0.4f, c1, c1, c1, c1);
        FontRenderers.sf_bold.drawGradientString(context.getMatrices(), "AutoCrystal Info", this.getPosX() + 2.0f, this.getPosY() + 4.0f, 10);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        float offset = 0.0f;
        for (Integer n : this.speeds) {
            bufferBuilder.vertex(this.getPosX() + 2.0f + offset, this.getPosY() + 80.0f - 55.0f * ((float)n.intValue() / (float)this.max), 0.0f).color(c2.getRGB());
            offset += 4.8f;
        }
        Render2DEngine.endBuilding(bufferBuilder);
        BufferBuilder bufferBuilder2 = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        offset = 0.0f;
        for (Integer speed : this.speeds) {
            bufferBuilder2.vertex(this.getPosX() + 2.0f + offset, this.getPosY() + 80.0f - 55.0f * ((float)speed.intValue() / (float)this.max), 0.0f).color(Render2DEngine.applyOpacity(c2.getRGB(), 0.3f));
            bufferBuilder2.vertex(this.getPosX() + 2.0f + offset, this.getPosY() + 80.0f, 0.0f).color(Render2DEngine.applyOpacity(c2.darker().darker().getRGB(), 0.0f));
            offset += 4.8f;
        }
        Render2DEngine.endBuilding(bufferBuilder2);
        FontRenderers.sf_bold_mini.drawString(context.getMatrices(), "" + this.max, (double)(this.getPosX() + 100.0f), (double)(this.getPosY() + 16.0f), HudEditor.textColor.getValue().getRawColor());
        if (!this.speeds.isEmpty()) {
            FontRenderers.sf_bold_mini.drawString(context.getMatrices(), String.valueOf(this.speeds.getLast()), (double)(this.getPosX() + 100.0f), (double)(this.getPosY() + 80.0f - 55.0f * ((float)this.speeds.getLast().intValue() / (float)this.max)), HudEditor.textColor.getValue().getRawColor());
        }
        FontRenderers.sf_bold_mini.drawString(context.getMatrices(), "" + this.min, (double)(this.getPosX() + 100.0f), (double)(this.getPosY() + 72.0f), HudEditor.textColor.getValue().getRawColor());
        boolean bl = true;
        FontRenderers.sf_bold_mini.drawString(context.getMatrices(), "Target: " + String.valueOf(Formatting.GRAY) + "null", (double)(this.getPosX() + 113.0f), (double)(this.getPosY() + 16.0f), HudEditor.textColor.getValue().getRawColor());
        FontRenderers.sf_bold_mini.drawString(context.getMatrices(), "Calc delay: 0ms", (double)(this.getPosX() + 113.0f), (double)(this.getPosY() + 24.0f), HudEditor.textColor.getValue().getRawColor());
        FontRenderers.sf_bold_mini.drawString(context.getMatrices(), "Side: null", (double)(this.getPosX() + 113.0f), (double)(this.getPosY() + 32.0f), HudEditor.textColor.getValue().getRawColor());
        FontRenderers.sf_bold_mini.drawString(context.getMatrices(), "Damage: 0", (double)(this.getPosX() + 113.0f), (double)(this.getPosY() + 40.0f), HudEditor.textColor.getValue().getRawColor());
        FontRenderers.sf_bold_mini.drawString(context.getMatrices(), "Self: 0", (double)(this.getPosX() + 113.0f), (double)(this.getPosY() + 48.0f), HudEditor.textColor.getValue().getRawColor());
        FontRenderers.sf_bold_mini.drawString(context.getMatrices(), "Overr. dmg: 0", (double)(this.getPosX() + 113.0f), (double)(this.getPosY() + 56.0f), HudEditor.textColor.getValue().getRawColor());
        FontRenderers.sf_bold_mini.drawString(context.getMatrices(), "Efficiency: 0", (double)(this.getPosX() + 113.0f), (double)(this.getPosY() + 64.0f), HudEditor.textColor.getValue().getRawColor());
        FontRenderers.sf_bold_mini.drawString(context.getMatrices(), "Pause: null", (double)(this.getPosX() + 113.0f), (double)(this.getPosY() + 72.0f), HudEditor.textColor.getValue().getRawColor());
        Render2DEngine.drawRect(context.getMatrices(), this.getPosX() + 110.5f, this.getPosY() + 12.0f, 0.5f, 65.0f, new Color(0x44FFFFFF, true));
        this.setBounds(this.getPosX(), this.getPosY(), this.getWidth(), this.getHeight());
    }

    public Formatting getCalcColor(float val) {
        if (val > 20.0f) {
            return Formatting.RED;
        }
        if (val > 10.0f) {
            return Formatting.YELLOW;
        }
        return Formatting.GREEN;
    }

    public Formatting getEfficiencyColor(float val) {
        if (val > 6.0f) {
            return Formatting.GREEN;
        }
        if (val < 1.0f) {
            return Formatting.RED;
        }
        return Formatting.YELLOW;
    }

    public void onSpawn() {
        if (this.time != 0L) {
            if (this.speeds.size() > 20) {
                this.speeds.poll();
            }
            this.speeds.add((int)(1000.0f / (float)(System.currentTimeMillis() - this.time)));
            this.max = Collections.max(this.speeds);
            this.min = Collections.min(this.speeds);
        }
        this.time = System.currentTimeMillis();
    }
}

