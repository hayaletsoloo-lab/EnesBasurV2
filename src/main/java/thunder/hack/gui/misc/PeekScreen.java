/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen
 *  net.minecraft.entity.player.PlayerInventory
 *  net.minecraft.item.ItemStack
 *  net.minecraft.screen.ShulkerBoxScreenHandler
 *  net.minecraft.text.Text
 */
package thunder.hack.gui.misc;

import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.text.Text;

public class PeekScreen
extends ShulkerBoxScreen {
    private static final ItemStack[] ITEMS = new ItemStack[27];

    public PeekScreen(ShulkerBoxScreenHandler handler, PlayerInventory inventory, Text title, Block block) {
        super(handler, inventory, title);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 2 && this.focusedSlot != null && !this.focusedSlot.getStack().isEmpty() && this.client.player.playerScreenHandler.getCursorStack().isEmpty()) {
            ItemStack itemStack = this.focusedSlot.getStack();
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }
}

