/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.TitleScreen
 *  net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen
 *  net.minecraft.client.gui.screen.option.OptionsScreen
 *  net.minecraft.client.gui.screen.world.SelectWorldScreen
 *  net.minecraft.client.resource.language.I18n
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 *  net.minecraft.util.Util
 *  org.jetbrains.annotations.NotNull
 */
package thunder.hack.gui.mainmenu;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import thunder.hack.api.IAddon;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.mainmenu.CreditsScreen;
import thunder.hack.gui.mainmenu.MainMenuButton;
import thunder.hack.utility.ThunderUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

public class MainMenuScreen
extends Screen {
    private final List<MainMenuButton> buttons = new ArrayList<MainMenuButton>();
    public boolean confirm = false;
    public static int ticksActive;
    private static MainMenuScreen INSTANCE;

    protected MainMenuScreen() {
        super(Text.of((String)"THMainMenuScreen"));
        INSTANCE = this;
        this.buttons.add(new MainMenuButton(-110.0f, -70.0f, I18n.translate((String)"menu.singleplayer", (Object[])new Object[0]).toUpperCase(Locale.ROOT), () -> Module.mc.setScreen((Screen)new SelectWorldScreen((Screen)(Object)this))));
        this.buttons.add(new MainMenuButton(4.0f, -70.0f, I18n.translate((String)"menu.multiplayer", (Object[])new Object[0]).toUpperCase(Locale.ROOT), () -> Module.mc.setScreen((Screen)new MultiplayerScreen((Screen)(Object)this))));
        this.buttons.add(new MainMenuButton(-110.0f, -29.0f, I18n.translate((String)"menu.options", (Object[])new Object[0]).toUpperCase(Locale.ROOT).replace(".", ""), () -> Module.mc.setScreen((Screen)new OptionsScreen((Screen)(Object)this, Module.mc.options))));
        this.buttons.add(new MainMenuButton(4.0f, -29.0f, "CLICKGUI", () -> ModuleManager.clickGui.setGui()));
        this.buttons.add(new MainMenuButton(-110.0f, 12.0f, I18n.translate((String)"menu.quit", (Object[])new Object[0]).toUpperCase(Locale.ROOT), () -> Module.mc.scheduleStop(), true));
    }

    public static MainMenuScreen getInstance() {
        ticksActive = 0;
        if (INSTANCE == null) {
            INSTANCE = new MainMenuScreen();
        }
        return INSTANCE;
    }

    public void tick() {
        if (++ticksActive > 400) {
            ticksActive = 0;
        }
    }

    public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
        float halfOfWidth = (float)Module.mc.getWindow().getScaledWidth() / 2.0f;
        float halfOfHeight = (float)Module.mc.getWindow().getScaledHeight() / 2.0f;
        float mainX = halfOfWidth - 120.0f;
        float mainY = halfOfHeight - 80.0f;
        float mainWidth = 240.0f;
        float mainHeight = 140.0f;
        this.renderBackground(context, mouseX, mouseY, delta);
        Render2DEngine.drawHudBase(context.getMatrices(), mainX, mainY, mainWidth, mainHeight, 20.0f);
        this.buttons.forEach(b -> b.onRender(context, mouseX, mouseY));
        boolean hoveredLogo = Render2DEngine.isHovered(mouseX, mouseY, (int)(halfOfWidth - 120.0f), (int)(halfOfHeight - 130.0f), 210.0, 50.0);
        FontRenderers.thglitchBig.drawCenteredString(context.getMatrices(), "THUNDERHACK", (double)((int)halfOfWidth), (double)((int)(halfOfHeight - 120.0f)), new Color(255, 255, 255, hoveredLogo ? 230 : 180).getRGB());
        boolean hovered = Render2DEngine.isHovered(mouseX, mouseY, halfOfWidth - 50.0f, halfOfHeight + 70.0f, 100.0, 10.0);
        FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "<-- Back to default menu", (double)halfOfWidth, (double)(halfOfHeight + 70.0f), hovered ? -1 : Render2DEngine.applyOpacity(-1, 0.6f));
        String onlineUsers = String.format("online: %s%s", Formatting.DARK_GREEN, Managers.TELEMETRY.getOnlinePlayers().size());
        FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), onlineUsers, (double)halfOfWidth, (double)(halfOfHeight * 2.0f - 15.0f), Color.GREEN);
        context.getMatrices().push();
        context.getMatrices().translate(halfOfWidth - 10.0f - FontRenderers.sf_medium.getStringWidth(onlineUsers) / 2.0f, halfOfHeight * 2.0f - 17.0f, 0.0f);
        Render2DEngine.drawBloom(context.getMatrices(), Render2DEngine.applyOpacity(Color.GREEN, 0.6f), 9.0f);
        context.getMatrices().pop();
        context.getMatrices().push();
        context.getMatrices().translate(halfOfWidth - 10.0f - FontRenderers.sf_medium.getStringWidth(onlineUsers) / 2.0f, halfOfHeight * 2.0f - 17.0f, 0.0f);
        Render2DEngine.drawBloom(context.getMatrices(), Render2DEngine.applyOpacity(Color.GREEN, (float)(0.5 + Math.sin((double)System.currentTimeMillis() / 500.0) / 2.0)), 9.0f);
        context.getMatrices().pop();
        Render2DEngine.drawHudBase(context.getMatrices(), (float)(Module.mc.getWindow().getScaledWidth() - 40), (float)(Module.mc.getWindow().getScaledHeight() - 40), 30.0f, 30.0f, 5.0f, Render2DEngine.isHovered(mouseX, mouseY, Module.mc.getWindow().getScaledWidth() - 40, Module.mc.getWindow().getScaledHeight() - 40, 30.0, 30.0) ? 0.7f : 1.0f);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)(Render2DEngine.isHovered(mouseX, mouseY, Module.mc.getWindow().getScaledWidth() - 40, Module.mc.getWindow().getScaledHeight() - 40, 30.0, 30.0) ? 0.7f : 1.0f));
        context.drawTexture(TextureStorage.thTeam, Module.mc.getWindow().getScaledWidth() - 40, Module.mc.getWindow().getScaledHeight() - 40, 30, 30, 0.0f, 0.0f, 30, 30, 30, 30);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        Render2DEngine.drawHudBase(context.getMatrices(), (float)(Module.mc.getWindow().getScaledWidth() - 80), (float)(Module.mc.getWindow().getScaledHeight() - 40), 30.0f, 30.0f, 5.0f, Render2DEngine.isHovered(mouseX, mouseY, Module.mc.getWindow().getScaledWidth() - 80, Module.mc.getWindow().getScaledHeight() - 40, 30.0, 30.0) ? 0.7f : 1.0f);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)(Render2DEngine.isHovered(mouseX, mouseY, Module.mc.getWindow().getScaledWidth() - 80, Module.mc.getWindow().getScaledHeight() - 40, 30.0, 30.0) ? 0.7f : 1.0f));
        context.drawTexture(TextureStorage.donation, Module.mc.getWindow().getScaledWidth() - 79, Module.mc.getWindow().getScaledHeight() - 39, 28, 28, 0.0f, 0.0f, 30, 30, 30, 30);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        int offsetY = 10;
        for (String change : ThunderUtility.changeLog) {
            String prefix = MainMenuScreen.getPrefix(change);
            FontRenderers.sf_medium.drawString(context.getMatrices(), prefix, 10.0, (double)offsetY, Render2DEngine.applyOpacity(-1, 0.4f));
            offsetY += 10;
        }
        int totalAddonsLoaded = Managers.ADDON.getTotalAddons();
        String addonsText = "Addons Loaded: " + totalAddonsLoaded;
        int screenWidth = Module.mc.getWindow().getScaledWidth();
        int textWidth = (int)FontRenderers.sf_bold.getStringWidth(addonsText);
        int textX = screenWidth - textWidth - 5;
        FontRenderers.sf_bold.drawString(context.getMatrices(), addonsText, (double)textX, 5.0, Color.WHITE.getRGB());
        int offset = 0;
        for (IAddon addon : Managers.ADDON.getAddons()) {
            textWidth = (int)FontRenderers.sf_bold.getStringWidth(addon.getName() + " |");
            textX = screenWidth - textWidth - 5;
            FontRenderers.sf_bold.drawString(context.getMatrices(), addon.getName() + String.valueOf(Formatting.WHITE) + " |", (double)textX, (double)(13 + offset), Color.GRAY.getRGB());
            offset += 9;
        }
    }

    @NotNull
    private static String getPrefix(@NotNull String change) {
        Object prefix = "";
        if (change.contains("[+]")) {
            change = change.replace("[+] ", "");
            prefix = String.valueOf(Formatting.GREEN) + "[+] " + String.valueOf(Formatting.RESET);
        } else if (change.contains("[-]")) {
            change = change.replace("[-] ", "");
            prefix = String.valueOf(Formatting.RED) + "[-] " + String.valueOf(Formatting.RESET);
        } else if (change.contains("[/]")) {
            change = change.replace("[/] ", "");
            prefix = String.valueOf(Formatting.LIGHT_PURPLE) + "[/] " + String.valueOf(Formatting.RESET);
        } else if (change.contains("[*]")) {
            change = change.replace("[*] ", "");
            prefix = String.valueOf(Formatting.GOLD) + "[*] " + String.valueOf(Formatting.RESET);
        }
        return (String)prefix + change;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float halfOfWidth = (float)Module.mc.getWindow().getScaledWidth() / 2.0f;
        float halfOfHeight = (float)Module.mc.getWindow().getScaledHeight() / 2.0f;
        this.buttons.forEach(b -> b.onClick((int)mouseX, (int)mouseY));
        if (Render2DEngine.isHovered(mouseX, mouseY, halfOfWidth - 50.0f, halfOfHeight + 70.0f, 100.0, 10.0)) {
            this.confirm = true;
            Module.mc.setScreen((Screen)new TitleScreen());
            this.confirm = false;
        }
        if (Render2DEngine.isHovered(mouseX, mouseY, Module.mc.getWindow().getScaledWidth() - 40, Module.mc.getWindow().getScaledHeight() - 40, 40.0, 40.0)) {
            Module.mc.setScreen((Screen)CreditsScreen.getInstance());
        }
        if (Render2DEngine.isHovered(mouseX, mouseY, Module.mc.getWindow().getScaledWidth() - 90, Module.mc.getWindow().getScaledHeight() - 40, 40.0, 40.0)) {
            Util.getOperatingSystem().open(URI.create("https://www.donationalerts.com/r/06ed/"));
        }
        if (Render2DEngine.isHovered(mouseX, mouseY, (int)(halfOfWidth - 157.0f), (int)(halfOfHeight - 140.0f), 300.0, 70.0)) {
            Util.getOperatingSystem().open(URI.create("https://thunderhack-site.vercel.app/"));
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    static {
        INSTANCE = new MainMenuScreen();
    }
}

