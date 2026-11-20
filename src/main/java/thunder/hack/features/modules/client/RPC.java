/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.TitleScreen
 *  net.minecraft.client.gui.screen.multiplayer.AddServerScreen
 *  net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen
 */
package thunder.hack.features.modules.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Objects;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.discord.DiscordEventHandlers;
import thunder.hack.utility.discord.DiscordRPC;
import thunder.hack.utility.discord.DiscordRichPresence;

public final class RPC
extends Module {
    private static final DiscordRPC rpc = DiscordRPC.Holder.INSTANCE;
    public static Setting<Mode> mode = new Setting<Mode>("Picture", Mode.Recode);
    public static Setting<Boolean> showIP = new Setting<Boolean>("ShowIP", true);
    public static Setting<sMode> smode = new Setting<sMode>("StateMode", sMode.Stats);
    public static Setting<String> state = new Setting<String>("State", "Beta? Recode? NextGen?");
    public static Setting<Boolean> nickname = new Setting<Boolean>("Nickname", true);
    public static DiscordRichPresence presence = new DiscordRichPresence();
    public static boolean started;
    static String String1;
    private final Timer timer_delay = new Timer();
    private static Thread thread;
    String slov;
    String[] rpc_perebor_en = new String[]{"Parkour", "Reporting cheaters", "Touching grass", "Asks how to bind", "Reporting bugs", "Watching Kilab"};
    String[] rpc_perebor_ru = new String[]{"\u041f\u0430\u0440\u043a\u0443\u0440\u0438\u0442", "\u0420\u0435\u043f\u043e\u0440\u0442\u0438\u0442 \u0447\u0438\u0442\u0435\u0440\u043e\u0432", "\u0422\u0440\u043e\u0433\u0430\u0435\u0442 \u0442\u0440\u0430\u0432\u0443", "\u0421\u043f\u0440\u0430\u0448\u0438\u0432\u0430\u0435\u0442 \u043a\u0430\u043a \u0437\u0430\u0431\u0438\u043d\u0434\u0438\u0442\u044c", "\u0420\u0435\u043f\u043e\u0440\u0442\u0438\u0442 \u0431\u0430\u0433\u0438", "\u0421\u043c\u043e\u0442\u0440\u0438\u0442 \u0424\u043b\u044e\u0433\u0435\u0440\u0430"};
    int randomInt;

    public RPC() {
        super("DiscordRPC", Module.Category.CLIENT);
    }

    public static void readFile() {
        block8: {
            try {
                File file = new File("ThunderHackRecode/misc/RPC.txt");
                if (!file.exists()) break block8;
                try (BufferedReader reader = new BufferedReader(new FileReader(file));){
                    while (reader.ready()) {
                        String1 = reader.readLine();
                    }
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public static void WriteFile(String url1, String url2) {
        File file = new File("ThunderHackRecode/misc/RPC.txt");
        try {
            file.createNewFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));){
                writer.write(url1 + "SEPARATOR" + url2 + "\n");
            }
            catch (Exception exception) {}
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Override
    public void onDisable() {
        started = false;
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
        if (rpc != null) {
            rpc.Discord_Shutdown();
        }
    }

    @Override
    public void onUpdate() {
        this.startRpc();
    }

    public void startRpc() {
        if (this.isDisabled()) {
            return;
        }
        if (!started) {
            started = true;
            if (rpc == null) {
                return;
            }
            DiscordEventHandlers handlers = new DiscordEventHandlers();
            rpc.Discord_Initialize("1093053626198523935", handlers, true, "");
            RPC.presence.startTimestamp = System.currentTimeMillis() / 1000L;
            RPC.presence.largeImageText = "v1.7b2407 [" + ThunderHack.GITHUB_HASH + "]";
            rpc.Discord_UpdatePresence(presence);
            thread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    rpc.Discord_RunCallbacks();
                    RPC.presence.details = this.getDetails();
                    switch (smode.getValue().ordinal()) {
                        case 1: {
                            RPC.presence.state = "Hacks: " + Managers.MODULE.getEnabledModules().size() + " / " + Managers.MODULE.modules.size();
                            break;
                        }
                        case 0: {
                            RPC.presence.state = state.getValue();
                            break;
                        }
                        case 2: {
                            RPC.presence.state = "v1.7b2407 for mc 1.21";
                        }
                    }
                    if (nickname.getValue().booleanValue()) {
                        RPC.presence.smallImageText = "logged as - " + mc.getSession().getUsername();
                        RPC.presence.smallImageKey = "https://minotar.net/helm/" + mc.getSession().getUsername() + "/100.png";
                    } else {
                        RPC.presence.smallImageText = "";
                        RPC.presence.smallImageKey = "";
                    }
                    RPC.presence.button_label_1 = "Download";
                    RPC.presence.button_url_1 = "https://github.com/Pan4ur/ThunderHack-Recode/";
                    switch (mode.getValue().ordinal()) {
                        case 2: {
                            RPC.presence.largeImageKey = "https://i.imgur.com/yY0z2Uq.gif";
                            break;
                        }
                        case 1: {
                            RPC.presence.largeImageKey = "https://media1.tenor.com/images/6bcbfcc0be97d029613b54f97845bc59/tenor.gif?itemid=26823781";
                            break;
                        }
                        case 0: {
                            RPC.readFile();
                            RPC.presence.largeImageKey = String1.split("SEPARATOR")[0];
                            if (Objects.equals(String1.split("SEPARATOR")[1], "none")) break;
                            RPC.presence.smallImageKey = String1.split("SEPARATOR")[1];
                        }
                    }
                    rpc.Discord_UpdatePresence(presence);
                    try {
                        Thread.sleep(2000L);
                    }
                    catch (InterruptedException interruptedException) {}
                }
            }, "TH-RPC-Handler");
            thread.start();
        }
    }

    private String getDetails() {
        String result = "";
        if (RPC.mc.currentScreen instanceof MultiplayerScreen || RPC.mc.currentScreen instanceof AddServerScreen || RPC.mc.currentScreen instanceof TitleScreen) {
            if (this.timer_delay.passedMs(60000L)) {
                this.randomInt = (int)(Math.random() * 6.0 + 0.0);
                this.slov = ClientSettings.isRu() ? this.rpc_perebor_ru[this.randomInt] : this.rpc_perebor_en[this.randomInt];
                this.timer_delay.reset();
            }
            result = this.slov;
        } else if (mc.getCurrentServerEntry() != null) {
            result = ClientSettings.isRu() ? (showIP.getValue().booleanValue() ? "\u0418\u0433\u0440\u0430\u0435\u0442 \u043d\u0430 " + RPC.mc.getCurrentServerEntry().address : "\u0418\u0433\u0440\u0430\u0435\u0442 \u043d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435") : (showIP.getValue() != false ? "Playing on " + RPC.mc.getCurrentServerEntry().address : "Playing on server");
        } else if (mc.isInSingleplayer()) {
            result = ClientSettings.isRu() ? "\u0427\u0438\u0442\u0435\u0440\u0438\u0442 \u0432 \u043e\u0434\u0438\u043d\u043e\u0447\u043a\u0435" : "SinglePlayer hacker";
        }
        return result;
    }

    static {
        String1 = "none";
    }

    public static enum sMode {
        Custom,
        Stats,
        Version;

    }

    public static enum Mode {
        Custom,
        MegaCute,
        Recode;

    }
}

