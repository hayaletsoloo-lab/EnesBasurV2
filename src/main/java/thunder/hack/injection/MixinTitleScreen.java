/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.gui.screen.ConfirmScreen
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.TitleScreen
 *  net.minecraft.client.util.InputUtil
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 *  net.minecraft.util.Util
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package thunder.hack.injection;

import java.net.URI;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;

@Mixin(value={TitleScreen.class})
public class MixinTitleScreen
extends Screen {
    protected MixinTitleScreen(Text title) {
        super(title);
    }

    @Inject(method={"init"}, at={@At(value="RETURN")})
    public void postInitHook(CallbackInfo ci) {
        if (ModuleManager.clickGui.getBind().getKey() == -1) {
            ModuleManager.clickGui.setBind(InputUtil.fromTranslationKey((String)"key.keyboard.p").getCode(), false, false);
            ClientSettings.language.setValue(ClientSettings.Language.ENG);
        }
        if (ThunderHack.isOutdated && !FabricLoader.getInstance().isDevelopmentEnvironment()) {
            Module.mc.setScreen((Screen)new ConfirmScreen(confirm -> {
                if (confirm) {
                    Util.getOperatingSystem().open(URI.create("https://github.com/Pan4ur/ThunderHack-Recode/releases/download/latest/thunderhack-1.7.jar/"));
                } else {
                    Module.mc.stop();
                }
            }, Text.of((String)(String.valueOf(Formatting.RED) + "You are using an outdated version of ThunderHack Recode")), Text.of((String)"Please update to the latest release"), Text.of((String)"Download"), Text.of((String)"Quit Game")));
        }
    }
}

