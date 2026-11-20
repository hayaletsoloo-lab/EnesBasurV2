/*
 * Decompiled with CFR 0.152.
 */
package thunder.hack.utility;

import java.lang.reflect.Method;
import thunder.hack.ThunderHack;

public class BaritoneUtility {
    public static void executeInternal(String command) {
        if (!ThunderHack.baritone) {
            return;
        }
        try {
            Class<?> apiClass = Class.forName("baritone.api.BaritoneAPI");
            Method getProvider = apiClass.getMethod("getProvider", new Class[0]);
            Object provider = getProvider.invoke(null, new Object[0]);
            Method getPrimaryBaritone = provider.getClass().getMethod("getPrimaryBaritone", new Class[0]);
            Object baritone = getPrimaryBaritone.invoke(provider, new Object[0]);
            Method getCommandManager = baritone.getClass().getMethod("getCommandManager", new Class[0]);
            Object commandManager = getCommandManager.invoke(baritone, new Object[0]);
            Method execute = commandManager.getClass().getMethod("execute", String.class);
            execute.invoke(commandManager, command);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

