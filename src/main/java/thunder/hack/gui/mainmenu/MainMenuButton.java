/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  org.jetbrains.annotations.NotNull
 */
package thunder.hack.gui.mainmenu;

import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.modules.Module;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;

public class MainMenuButton {
    private final float posX;
    private final float posY;
    private final float width;
    private final float height;
    private final String name;
    private final Runnable action;

    public MainMenuButton(float posX, float posY, @NotNull String name, Runnable action, boolean isExit) {
        this.name = name;
        this.posX = posX;
        this.posY = posY;
        this.action = action;
        this.width = isExit ? 222.0f : 107.0f;
        this.height = 38.0f;
    }

    public MainMenuButton(float posX, float posY, @NotNull String name, Runnable action) {
        this(posX, posY, name, action, false);
    }

    public void onRender(DrawContext context, float mouseX, float mouseY) {
        float halfOfWidth = (float)Module.mc.getWindow().getScaledWidth() / 2.0f;
        float halfOfHeight = (float)Module.mc.getWindow().getScaledHeight() / 2.0f;
        Render2DEngine.drawHudBase(context.getMatrices(), halfOfWidth + this.posX, halfOfHeight + this.posY, this.width, this.height, 10.0f);
        boolean hovered = Render2DEngine.isHovered(mouseX, mouseY, halfOfWidth + this.posX, halfOfHeight + this.posY, this.width, this.height);
        FontRenderers.monsterrat.drawCenteredString(context.getMatrices(), this.name, (double)(halfOfWidth + this.posX + this.width / 2.0f), (double)(halfOfHeight + this.posY + this.height / 2.0f - 3.0f), hovered ? -1 : Render2DEngine.applyOpacity(-1, 0.7f));
    }

    public void onClick(int mouseX, int mouseY) {
        float halfOfHeight;
        float halfOfWidth = (float)Module.mc.getWindow().getScaledWidth() / 2.0f;
        boolean hovered = Render2DEngine.isHovered(mouseX, mouseY, halfOfWidth + this.posX, (halfOfHeight = (float)Module.mc.getWindow().getScaledHeight() / 2.0f) + this.posY, this.width, this.height);
        if (hovered) {
            this.action.run();
        }
    }
}

