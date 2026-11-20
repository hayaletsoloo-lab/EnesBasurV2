/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.damage.DamageSource
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package thunder.hack.injection.accesors;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={LivingEntity.class})
public interface ILivingEntity {
    @Accessor(value="lastAttackedTicks")
    public int getLastAttackedTicks();

    @Accessor(value="jumpingCooldown")
    public int getLastJumpCooldown();

    @Accessor(value="jumpingCooldown")
    public void setLastJumpCooldown(int var1);

    @Invoker(value="tryUseTotem")
    public boolean invokeTryUseTotem(DamageSource var1);
}

