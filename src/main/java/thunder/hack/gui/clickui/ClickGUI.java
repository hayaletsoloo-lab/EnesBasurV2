/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.util.InputUtil
 *  net.minecraft.text.Text
 *  net.minecraft.util.Identifier
 *  org.lwjgl.glfw.GLFW
 */
package thunder.hack.gui.clickui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClickGui;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.clickui.AbstractCategory;
import thunder.hack.gui.clickui.Category;
import thunder.hack.gui.clickui.impl.SearchBar;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;
import thunder.hack.utility.render.animation.EaseOutBack;

public class ClickGUI
extends Screen {
    public static List<AbstractCategory> windows;
    public static boolean anyHovered;
    private boolean firstOpen;
    private float scrollY;
    private float closeAnimation;
    private float prevYaw;
    private float prevPitch;
    private float closeDirectionX;
    private float closeDirectionY;
    public static boolean close;
    public static boolean imageDirection;
    public static String currentDescription;
    public EaseOutBack imageAnimation = new EaseOutBack(6);
    private static ClickGUI INSTANCE;
    public SearchBar searchBar = new SearchBar();

    public ClickGUI() {
        super(Text.of((String)"NewClickGUI"));
        windows = Lists.newArrayList();
        this.firstOpen = true;
        this.setInstance();
    }

    public static ClickGUI getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGUI();
        }
        imageDirection = true;
        return INSTANCE;
    }

    public static ClickGUI getClickGui() {
        windows.forEach(AbstractCategory::init);
        return ClickGUI.getInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    protected void init() {
        if (this.firstOpen) {
            float offset = 0.0f;
            int windowHeight = 18;
            int halfWidth = Module.mc.getWindow().getScaledWidth() / 2;
            int halfWidthCats = (int)(((float)Module.Category.values().size() - 1.0f) / 2.0f * ((float)ModuleManager.clickGui.moduleWidth.getValue().intValue() + 4.0f));
            for (Module.Category category : Managers.MODULE.getCategories()) {
                if (category == Module.Category.HUD) continue;
                Category window = new Category(category, Managers.MODULE.getModulesByCategory(category), (float)(halfWidth - halfWidthCats) + offset, 20.0f, 100.0f, windowHeight);
                window.setOpen(true);
                windows.add(window);
                if (!((offset += (float)(ModuleManager.clickGui.moduleWidth.getValue() + 2)) > (float)Module.mc.getWindow().getScaledWidth())) continue;
                offset = 0.0f;
            }
            this.firstOpen = false;
        } else if (windows.getFirst().getX() < 0.0f || windows.getFirst().getY() < 0.0f) {
            float offset = 0.0f;
            int halfWidth = Module.mc.getWindow().getScaledWidth() / 2;
            int halfWidthCats = (int)(3.0f * ((float)ModuleManager.clickGui.moduleWidth.getValue().intValue() + 4.0f));
            for (AbstractCategory w : windows) {
                w.setX((float)(halfWidth - halfWidthCats) + offset);
                w.setY(20.0f);
                if (!((offset += (float)(ModuleManager.clickGui.moduleWidth.getValue() + 2)) > (float)Module.mc.getWindow().getScaledWidth())) continue;
                offset = 0.0f;
            }
        }
        windows.forEach(AbstractCategory::init);
    }

    public boolean shouldPause() {
        return false;
    }

    public void tick() {
        windows.forEach(AbstractCategory::tick);
        this.imageAnimation.update(imageDirection);
        if (close) {
            if (Module.mc.player != null) {
                if (Module.mc.player.getPitch() > this.prevPitch) {
                    this.closeDirectionY = (this.prevPitch - Module.mc.player.getPitch()) * 300.0f;
                }
                if (Module.mc.player.getPitch() < this.prevPitch) {
                    this.closeDirectionY = (this.prevPitch - Module.mc.player.getPitch()) * 300.0f;
                }
                if (Module.mc.player.getYaw() > this.prevYaw) {
                    this.closeDirectionX = (this.prevYaw - Module.mc.player.getYaw()) * 300.0f;
                }
                if (Module.mc.player.getYaw() < this.prevYaw) {
                    this.closeDirectionX = (this.prevYaw - Module.mc.player.getYaw()) * 300.0f;
                }
            }
            if (this.closeDirectionX < 1.0f && this.closeDirectionY < 1.0f && this.closeAnimation > 2.0f) {
                this.closeDirectionY = -3000.0f;
            }
            this.closeAnimation += 1.0f;
            if (this.closeAnimation > 6.0f) {
                close = false;
                windows.forEach(AbstractCategory::restorePos);
                this.close();
            }
        }
        if (this.searchBar != null) {
            this.searchBar.tick();
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (ModuleManager.clickGui.blur.getValue().booleanValue()) {
            this.applyBlur(delta);
        }
        anyHovered = false;
        ClickGui.Image image = ModuleManager.clickGui.image.getValue();
        if (image != ClickGui.Image.None) {
            RenderSystem.setShaderTexture((int)0, (Identifier)(Object)image.file);
            Render2DEngine.renderTexture(context.getMatrices(), (double)Module.mc.getWindow().getScaledWidth() - (double)image.fileWidth * this.imageAnimation.getAnimationd(), Module.mc.getWindow().getScaledHeight() - image.fileHeight, image.fileWidth, image.fileHeight, 0.0f, 0.0f, image.fileWidth, image.fileHeight, image.fileWidth, image.fileHeight);
        }
        if (this.closeAnimation <= 6.0f) {
            windows.forEach(w -> {
                w.setX(w.getX() + this.closeDirectionX * AnimationUtility.deltaTime());
                w.setY(w.getY() + this.closeDirectionY * AnimationUtility.deltaTime());
            });
        }
        if (Module.fullNullCheck()) {
            this.renderBackground(context, mouseX, mouseY, delta);
        }
        if (ModuleManager.clickGui.scrollMode.getValue() == ClickGui.scrollModeEn.Old) {
            for (AbstractCategory window : windows) {
                if (InputUtil.isKeyPressed((long)Module.mc.getWindow().getHandle(), (int)264)) {
                    window.setY(window.getY() + 2.0f);
                }
                if (InputUtil.isKeyPressed((long)Module.mc.getWindow().getHandle(), (int)265)) {
                    window.setY(window.getY() - 2.0f);
                }
                if (InputUtil.isKeyPressed((long)Module.mc.getWindow().getHandle(), (int)262)) {
                    window.setX(window.getX() + 2.0f);
                }
                if (InputUtil.isKeyPressed((long)Module.mc.getWindow().getHandle(), (int)263)) {
                    window.setX(window.getX() - 2.0f);
                }
                if (this.scrollY == 0.0f) continue;
                window.setY(window.getY() + this.scrollY);
            }
        } else {
            for (AbstractCategory window : windows) {
                if (this.scrollY == 0.0f) continue;
                window.setModuleOffset(this.scrollY, mouseX, mouseY);
            }
        }
        this.scrollY = 0.0f;
        windows.forEach(w -> w.render(context, mouseX, mouseY, delta));
        if (!Objects.equals(currentDescription, "") && ModuleManager.clickGui.descriptions.getValue().booleanValue()) {
            Render2DEngine.drawHudBase(context.getMatrices(), (float)(mouseX + 7), (float)(mouseY + 5), FontRenderers.sf_medium.getStringWidth(currentDescription) + 6.0f, 11.0f, 1.0f, false);
            FontRenderers.sf_medium.drawString(context.getMatrices(), currentDescription, (double)(mouseX + 10), (double)(mouseY + 8), HudEditor.getColor(0).getRGB());
            currentDescription = "";
        }
        if (this.searchBar != null) {
            this.searchBar.setX((float)Module.mc.getWindow().getScaledWidth() / 2.0f - 75.0f);
            this.searchBar.setY(Module.mc.getWindow().getScaledHeight() - 22);
            this.searchBar.setWidth(150.0f);
            this.searchBar.setHeight(14.0f);
            this.searchBar.render(context, mouseX, mouseY, delta);
            FontRenderers.sf_medium_mini.drawString(context.getMatrices(), "Left Click: Toggle | Right Click: Settings | Middle Click: Bind | Ctrl+F: Search", (double)((float)Module.mc.getWindow().getScaledWidth() / 2.0f - 350.0f), (double)(Module.mc.getWindow().getScaledHeight() - 12), new Color(150, 150, 150).getRGB());
        }
        if (!HudElement.anyHovered && !anyHovered && GLFW.glfwGetPlatform() != 393219) {
            GLFW.glfwSetCursor((long)Module.mc.getWindow().getHandle(), (long)GLFW.glfwCreateStandardCursor((int)221185));
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scrollY += (float)((int)(verticalAmount * 5.0));
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        windows.forEach(w -> {
            w.mouseClicked((int)mouseX, (int)mouseY, button);
            windows.forEach(w1 -> {
                if (w.dragging && w != w1) {
                    w1.dragging = false;
                }
            });
        });
        if (this.searchBar != null) {
            this.searchBar.mouseClicked((int)mouseX, (int)mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        windows.forEach(w -> w.mouseReleased((int)mouseX, (int)mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean charTyped(char key, int modifier) {
        if (this.searchBar != null) {
            this.searchBar.charTyped(key, modifier);
        }
        windows.forEach(w -> w.charTyped(key, modifier));
        return true;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        windows.forEach(w -> w.keyTyped(keyCode));
        if (this.searchBar != null) {
            this.searchBar.keyTyped(keyCode);
        }
        if (keyCode == 256 && SearchBar.listening) {
            SearchBar.listening = false;
            return true;
        }
        if (keyCode == 256) {
            if (Module.mc.player == null || !ModuleManager.clickGui.closeAnimation.getValue().booleanValue()) {
                imageDirection = false;
                this.imageAnimation.reset();
                super.keyPressed(keyCode, scanCode, modifiers);
                return true;
            }
            if (close) {
                return true;
            }
            imageDirection = false;
            windows.forEach(AbstractCategory::savePos);
            this.closeDirectionX = 0.0f;
            this.closeDirectionY = 0.0f;
            close = true;
            Module.mc.mouse.lockCursor();
            this.closeAnimation = 0.0f;
            if (Module.mc.player != null) {
                this.prevYaw = Module.mc.player.getYaw();
                this.prevPitch = Module.mc.player.getPitch();
            }
            return true;
        }
        return false;
    }

    static {
        close = false;
        currentDescription = "";
        INSTANCE = new ClickGUI();
    }
}

