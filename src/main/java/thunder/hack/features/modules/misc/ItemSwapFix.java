/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 */
package thunder.hack.features.modules.misc;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import thunder.hack.features.modules.Module;

public class ItemSwapFix
extends Module {
    public ItemSwapFix() {
        super("ItemSwapFix", Module.Category.MISC);
    }

    public void forceSync() {
        if (!this.isEnabled() || ItemSwapFix.mc.player == null || mc.getNetworkHandler() == null) {
            return;
        }
        mc.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(ItemSwapFix.mc.player.getInventory().selectedSlot));
    }
}

