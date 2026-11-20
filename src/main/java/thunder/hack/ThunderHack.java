/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  meteordevelopment.orbit.EventBus
 *  meteordevelopment.orbit.IEventBus
 *  net.fabricmc.api.ModInitializer
 *  net.fabricmc.loader.api.FabricLoader
 *  net.fabricmc.loader.api.ModContainer
 *  net.fabricmc.loader.api.metadata.ModMetadata
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.util.math.BlockPos
 *  org.slf4j.Logger
 */
package thunder.hack;

import com.mojang.logging.LogUtils;
import java.awt.Color;
import java.lang.invoke.MethodHandles;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import thunder.hack.core.Core;
import thunder.hack.core.Managers;
import thunder.hack.core.hooks.ManagerShutdownHook;
import thunder.hack.core.hooks.ModuleShutdownHook;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.utility.ThunderUtility;
import thunder.hack.utility.render.Render2DEngine;

public class ThunderHack
implements ModInitializer {
    public static final ModMetadata MOD_META;
    public static final String MOD_ID = "thunderhack";
    public static final String VERSION = "1.7b2407";
    public static String GITHUB_HASH;
    public static String BUILD_DATE;
    public static final Logger LOGGER;
    public static final Runtime RUNTIME;
    public static final boolean baritone;
    public static final IEventBus EVENT_BUS;
    public static String[] contributors;
    public static Color copy_color;
    public static KeyListening currentKeyListener;
    public static boolean isOutdated;
    public static BlockPos gps_position;
    public static float TICK_TIMER;
    public static MinecraftClient mc;
    public static long initTime;
    public static Core core;

    public void onInitialize() {
        mc = MinecraftClient.getInstance();
        initTime = System.currentTimeMillis();
        BUILD_DATE = ThunderUtility.readManifestField("Build-Timestamp");
        GITHUB_HASH = ThunderUtility.readManifestField("Git-Commit");
        ThunderUtility.syncVersion();
        EVENT_BUS.registerLambdaFactory("thunder.hack", (lookupInMethod, klass) -> (MethodHandles.Lookup)lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        EVENT_BUS.subscribe(core);
        Managers.init();
        Managers.subscribe();
        Render2DEngine.initShaders();
        ModuleManager.rpc.startRpc();
        LOGGER.info("[ThunderHack] Init time: {} ms.", (System.currentTimeMillis() - initTime));
        initTime = System.currentTimeMillis();
        RUNTIME.addShutdownHook(new ManagerShutdownHook());
        RUNTIME.addShutdownHook(new ModuleShutdownHook());
    }

    public static boolean isFuturePresent() {
        return FabricLoader.getInstance().getModContainer("future").isPresent();
    }

    static {
        GITHUB_HASH = "0";
        BUILD_DATE = "1 Jan 1970";
        LOGGER = LogUtils.getLogger();
        RUNTIME = Runtime.getRuntime();
        baritone = FabricLoader.getInstance().isModLoaded("baritone") || FabricLoader.getInstance().isModLoaded("baritone-meteor");
        EVENT_BUS = new EventBus();
        contributors = new String[32];
        copy_color = new Color(-1);
        isOutdated = false;
        TICK_TIMER = 1.0f;
        core = new Core();
        MOD_META = ((ModContainer)FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow()).getMetadata();
    }

    public static enum KeyListening {
        ThunderGui,
        ClickGui,
        Search,
        Sliders,
        Strings;

    }
}

