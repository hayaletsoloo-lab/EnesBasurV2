/*
 * Decompiled with CFR 0.152.
 */
package thunder.hack.features.modules.player;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class NoInteract
extends Module {
    public static Setting<Boolean> onlyAura = new Setting<Boolean>("OnlyAura", false);

    public NoInteract() {
        super("NoInteract", Module.Category.PLAYER);
    }
}

