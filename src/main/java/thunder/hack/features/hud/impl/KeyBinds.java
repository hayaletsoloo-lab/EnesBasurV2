/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  org.jetbrains.annotations.NotNull
 */
package thunder.hack.features.hud.impl;

import java.awt.Color;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

public class KeyBinds
extends HudElement {
    public final Setting<ColorSetting> oncolor = new Setting<ColorSetting>("OnColor", new ColorSetting(-1));
    public final Setting<ColorSetting> offcolor = new Setting<ColorSetting>("OffColor", new ColorSetting(1));
    public final Setting<Boolean> onlyEnabled = new Setting<Boolean>("OnlyEnabled", false);
    private float vAnimation;
    private float hAnimation;

    public KeyBinds() {
        super("KeyBinds", 100, 100);
    }

    @Override
    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        int y_offset1 = 0;
        float max_width = 50.0f;
        float maxBindWidth = 0.0f;
        float pointerX = 0.0f;
        for (Module feature : Managers.MODULE.modules) {
            if (feature.isDisabled() && this.onlyEnabled.getValue().booleanValue() || Objects.equals(feature.getBind().getBind(), "None") || feature == ModuleManager.clickGui) continue;
            if (y_offset1 == 0) {
                y_offset1 += 4;
            }
            y_offset1 += 9;
            float nameWidth = FontRenderers.sf_bold_mini.getStringWidth(feature.getName());
            float bindWidth = FontRenderers.sf_bold_mini.getStringWidth(KeyBinds.getShortKeyName(feature));
            if (bindWidth > maxBindWidth) {
                maxBindWidth = bindWidth;
            }
            if (!(nameWidth > pointerX)) continue;
            pointerX = nameWidth;
        }
        float px = this.getPosX() + 10.0f + pointerX;
        max_width = Math.max(20.0f + pointerX + maxBindWidth, 50.0f);
        this.vAnimation = AnimationUtility.fast(this.vAnimation, 14 + y_offset1, 15.0f);
        this.hAnimation = AnimationUtility.fast(this.hAnimation, max_width, 15.0f);
        Render2DEngine.drawHudBase(context.getMatrices(), this.getPosX(), this.getPosY(), this.hAnimation, this.vAnimation, HudEditor.hudRound.getValue().floatValue());
        if (HudEditor.hudStyle.is(HudEditor.HudStyle.Glowing)) {
            FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), "KeyBinds", (double)(this.getPosX() + this.hAnimation / 2.0f), (double)(this.getPosY() + 4.0f), HudEditor.textColor.getValue().getColorObject());
        } else {
            FontRenderers.sf_bold.drawGradientCenteredString(context.getMatrices(), "KeyBinds", this.getPosX() + this.hAnimation / 2.0f, this.getPosY() + 4.0f, 10);
        }
        if (y_offset1 > 0) {
            if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
                Render2DEngine.drawRectDumbWay(context.getMatrices(), this.getPosX() + 4.0f, this.getPosY() + 13.0f, this.getPosX() + this.getWidth() - 4.0f, this.getPosY() + 13.5f, new Color(0x54FFFFFF, true));
            } else {
                Render2DEngine.horizontalGradient(context.getMatrices(), this.getPosX() + 2.0f, this.getPosY() + 13.7f, this.getPosX() + 2.0f + this.hAnimation / 2.0f - 2.0f, this.getPosY() + 14.0f, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
                Render2DEngine.horizontalGradient(context.getMatrices(), this.getPosX() + 2.0f + this.hAnimation / 2.0f - 2.0f, this.getPosY() + 13.7f, this.getPosX() + 2.0f + this.hAnimation - 4.0f, this.getPosY() + 14.0f, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));
            }
        }
        Render2DEngine.addWindow(context.getMatrices(), this.getPosX(), this.getPosY(), this.getPosX() + this.hAnimation, this.getPosY() + this.vAnimation, 1.0);
        int y_offset = 0;
        for (Module feature : Managers.MODULE.modules) {
            if (feature.isDisabled() && this.onlyEnabled.getValue().booleanValue() || Objects.equals(feature.getBind().getBind(), "None") || feature == ModuleManager.clickGui) continue;
            FontRenderers.sf_bold_mini.drawString(context.getMatrices(), feature.getName(), (double)(this.getPosX() + 5.0f), (double)(this.getPosY() + 19.0f + (float)y_offset), feature.isOn() ? this.oncolor.getValue().getColor() : this.offcolor.getValue().getColor());
            FontRenderers.sf_bold_mini.drawCenteredString(context.getMatrices(), KeyBinds.getShortKeyName(feature), (double)(px + (this.getPosX() + max_width - px) / 2.0f), (double)(this.getPosY() + 19.0f + (float)y_offset), feature.isOn() ? this.oncolor.getValue().getColor() : this.offcolor.getValue().getColor());
            Render2DEngine.drawRect(context.getMatrices(), px, this.getPosY() + 17.0f + (float)y_offset, 0.5f, 8.0f, new Color(0x44FFFFFF, true));
            y_offset += 9;
        }
        Render2DEngine.popWindow();
        this.setBounds(this.getPosX(), this.getPosY(), this.hAnimation, this.vAnimation);
    }

    @NotNull
    public static String getShortKeyName(Module feature) {
        String sbind = feature.getBind().getBind();
        return switch (feature.getBind().getBind()) {
            case "LEFT_CONTROL" -> "LCtrl";
            case "RIGHT_CONTROL" -> "RCtrl";
            case "LEFT_SHIFT" -> "LShift";
            case "RIGHT_SHIFT" -> "RShift";
            case "LEFT_ALT" -> "LAlt";
            case "RIGHT_ALT" -> "RAlt";
            default -> sbind.toUpperCase();
        };
    }
}

