/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  net.minecraft.client.render.WorldRenderer
 *  net.minecraft.entity.player.BlockBreakingInfo
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package thunder.hack.injection.accesors;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.player.BlockBreakingInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={WorldRenderer.class})
public interface IWorldRenderer {
    @Accessor(value="blockBreakingInfos")
    public Int2ObjectMap<BlockBreakingInfo> getBlockBreakingInfos();
}

