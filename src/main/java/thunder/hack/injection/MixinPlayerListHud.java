/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.hud.PlayerListHud
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.scoreboard.Team
 *  net.minecraft.util.Nullables
 *  net.minecraft.world.GameMode
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package thunder.hack.injection;

import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Nullables;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;

@Mixin(value={PlayerListHud.class})
public class MixinPlayerListHud {
    private static final Comparator<Object> ENTRY_ORDERING = Comparator.comparingInt(entry -> ((PlayerListEntry)entry).getGameMode() == GameMode.SPECTATOR ? 1 : 0).thenComparing(entry -> (String)Nullables.mapOrElse(((PlayerListEntry)entry).getScoreboardTeam(), Team::getName, "")).thenComparing(entry -> ((PlayerListEntry)entry).getProfile().getName(), String::compareToIgnoreCase);

    @Inject(method={"collectPlayerEntries"}, at={@At(value="HEAD")}, cancellable=true)
    private void collectPlayerEntriesHook(CallbackInfoReturnable<List<PlayerListEntry>> cir) {
        if (ClientSettings.futureCompatibility.getValue().booleanValue()) {
            return;
        }
        if (ThunderHack.isFuturePresent()) {
            return;
        }
        if (ModuleManager.extraTab.isEnabled()) {
            cir.setReturnValue(Module.mc.player.networkHandler.getListedPlayerListEntries().stream().sorted(ENTRY_ORDERING).limit(1000L).toList());
        } else {
            cir.setReturnValue(Module.mc.player.networkHandler.getListedPlayerListEntries().stream().sorted(ENTRY_ORDERING).limit(80L).toList());
        }
    }
}

