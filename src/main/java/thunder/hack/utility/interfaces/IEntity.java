/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 */
package thunder.hack.utility.interfaces;

import java.util.List;
import net.minecraft.util.math.BlockPos;
import thunder.hack.features.modules.render.Trails;

public interface IEntity {
    public List<Trails.Trail> getTrails();

    public BlockPos thunderHack_Recode$getVelocityBP();
}

