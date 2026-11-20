/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.AbstractBlock
 *  net.minecraft.block.BlockState
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.registry.RegistryKeys
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.BlockView
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package thunder.hack.injection;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.player.AutoTool;

@Mixin(value={AbstractBlock.class})
public abstract class MixinAbstractBlock {
    @Inject(method={"calcBlockBreakingDelta"}, at={@At(value="HEAD")}, cancellable=true)
    public void calcBlockBreakingDeltaHook(BlockState state, PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> ci) {
        if (ModuleManager.autoTool.isEnabled() && AutoTool.silent.getValue().booleanValue()) {
            float f = state.getHardness(world, pos);
            if (f < 0.0f) {
                ci.setReturnValue(Float.valueOf(0.0f));
            } else {
                float dig_speed = this.getDigSpeed(state, player, player.getInventory().getStack(AutoTool.itemIndex)) / f;
                ci.setReturnValue(Float.valueOf(player.getInventory().getStack(AutoTool.itemIndex).isSuitableFor(state) ? dig_speed / 30.0f : dig_speed / 100.0f));
            }
        }
    }

    public float getDigSpeed(BlockState state, PlayerEntity player, ItemStack stack) {
        int effect;
        float str = stack.getMiningSpeedMultiplier(state);
        if (str > 1.0f && (effect = EnchantmentHelper.getLevel((RegistryEntry)((RegistryEntry)player.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.EFFICIENCY).get()), (ItemStack)stack)) > 0) {
            str += (float)(effect * effect + 1);
        }
        return Math.max(str, 0.0f);
    }
}

