/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  meteordevelopment.orbit.EventHandler
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.screen.slot.Slot
 *  net.minecraft.screen.slot.SlotActionType
 */
package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import thunder.hack.events.impl.EventClickSlot;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class ItemScroller
extends Module {
    public Setting<Integer> delay = new Setting<Integer>("Delay", 80, 0, 500);
    private boolean pauseListening = false;

    public ItemScroller() {
        super("ItemScroller", Module.Category.MISC);
    }

    @EventHandler
    public void onClick(EventClickSlot e) {
        if ((this.isKeyPressed(340) || this.isKeyPressed(344)) && (this.isKeyPressed(341) || this.isKeyPressed(345)) && e.getSlotActionType() == SlotActionType.THROW && !this.pauseListening) {
            Item copy = ((Slot)ItemScroller.mc.player.currentScreenHandler.slots.get(e.getSlot())).getStack().getItem();
            this.pauseListening = true;
            for (int i2 = 0; i2 < ItemScroller.mc.player.currentScreenHandler.slots.size(); ++i2) {
                if (((Slot)ItemScroller.mc.player.currentScreenHandler.slots.get(i2)).getStack().getItem() != copy) continue;
                ItemScroller.mc.interactionManager.clickSlot(ItemScroller.mc.player.currentScreenHandler.syncId, i2, 1, SlotActionType.THROW, (PlayerEntity)ItemScroller.mc.player);
            }
            this.pauseListening = false;
        }
    }
}

