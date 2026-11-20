/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  meteordevelopment.orbit.EventHandler
 *  net.minecraft.entity.projectile.FishingBobberEntity
 *  net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket
 *  net.minecraft.world.World
 */
package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.world.World;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class NoPush
extends Module {
    public Setting<Boolean> blocks = new Setting<Boolean>("Blocks", true);
    public Setting<Boolean> players = new Setting<Boolean>("Players", true);
    public Setting<Boolean> water = new Setting<Boolean>("Liquids", true);
    public Setting<Boolean> fishingHook = new Setting<Boolean>("FishingHook", true);

    public NoPush() {
        super("NoPush", Module.Category.MOVEMENT);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        EntityStatusS2CPacket pac;
        Object object = e.getPacket();
        if (object instanceof EntityStatusS2CPacket && (pac = (EntityStatusS2CPacket)object).getStatus() == 31 && (object = pac.getEntity((World)NoPush.mc.world)) instanceof FishingBobberEntity) {
            FishingBobberEntity hook = (FishingBobberEntity)object;
            if (this.fishingHook.getValue().booleanValue() && hook.getHookedEntity() == NoPush.mc.player) {
                e.cancel();
            }
        }
    }
}

