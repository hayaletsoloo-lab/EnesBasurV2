/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.AirBlock
 *  net.minecraft.block.EnderChestBlock
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 */
package thunder.hack.features.modules.player;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.AirBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class AutoTool
extends Module {
    public static Setting<Boolean> swapBack = new Setting<Boolean>("SwapBack", true);
    public static Setting<Boolean> saveItem = new Setting<Boolean>("SaveItem", true);
    public static Setting<Boolean> silent = new Setting<Boolean>("Silent", false);
    public static Setting<Boolean> echestSilk = new Setting<Boolean>("EchestSilk", true);
    public static int itemIndex;
    private boolean swap;
    private long swapDelay;
    private final List<Integer> lastItem = new ArrayList<Integer>();

    public AutoTool() {
        super("AutoTool", Module.Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (!(AutoTool.mc.crosshairTarget instanceof BlockHitResult)) {
            return;
        }
        BlockHitResult result = (BlockHitResult)AutoTool.mc.crosshairTarget;
        BlockPos pos = result.getBlockPos();
        if (AutoTool.mc.world.getBlockState(pos).isAir()) {
            return;
        }
        if (AutoTool.getTool(pos) != -1 && AutoTool.mc.options.attackKey.isPressed()) {
            this.lastItem.add(AutoTool.mc.player.getInventory().selectedSlot);
            if (silent.getValue().booleanValue()) {
                AutoTool.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(AutoTool.getTool(pos)));
            } else {
                AutoTool.mc.player.getInventory().selectedSlot = AutoTool.getTool(pos);
            }
            itemIndex = AutoTool.getTool(pos);
            this.swap = true;
            this.swapDelay = System.currentTimeMillis();
        } else if (this.swap && !this.lastItem.isEmpty() && System.currentTimeMillis() >= this.swapDelay + 300L && swapBack.getValue().booleanValue()) {
            if (silent.getValue().booleanValue()) {
                AutoTool.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(this.lastItem.get(0).intValue()));
            } else {
                AutoTool.mc.player.getInventory().selectedSlot = this.lastItem.get(0);
            }
            itemIndex = this.lastItem.get(0);
            this.lastItem.clear();
            this.swap = false;
        }
    }

    public static int getTool(BlockPos pos) {
        int index = -1;
        float CurrentFastest = 1.0f;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = AutoTool.mc.player.getInventory().getStack(i);
            if (stack == ItemStack.EMPTY || AutoTool.mc.player.getInventory().getStack(i).getMaxDamage() - AutoTool.mc.player.getInventory().getStack(i).getDamage() <= 10 && saveItem.getValue().booleanValue()) continue;
            float digSpeed = EnchantmentHelper.getLevel((RegistryEntry)((RegistryEntry)AutoTool.mc.world.getRegistryManager().get(Enchantments.EFFICIENCY.getRegistryRef()).getEntry(Enchantments.EFFICIENCY).get()), (ItemStack)stack);
            float destroySpeed = stack.getMiningSpeedMultiplier(AutoTool.mc.world.getBlockState(pos));
            if (AutoTool.mc.world.getBlockState(pos).getBlock() instanceof AirBlock) {
                return -1;
            }
            if (AutoTool.mc.world.getBlockState(pos).getBlock() instanceof EnderChestBlock && echestSilk.getValue().booleanValue()) {
                if (EnchantmentHelper.getLevel((RegistryEntry)((RegistryEntry)AutoTool.mc.world.getRegistryManager().get(Enchantments.SILK_TOUCH.getRegistryRef()).getEntry(Enchantments.SILK_TOUCH).get()), (ItemStack)stack) <= 0 || !(digSpeed + destroySpeed > CurrentFastest)) continue;
                CurrentFastest = digSpeed + destroySpeed;
                index = i;
                continue;
            }
            if (!(digSpeed + destroySpeed > CurrentFastest)) continue;
            CurrentFastest = digSpeed + destroySpeed;
            index = i;
        }
        return index;
    }
}

