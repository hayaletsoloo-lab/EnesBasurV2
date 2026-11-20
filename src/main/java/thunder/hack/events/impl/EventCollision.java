/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.util.math.BlockPos
 */
package thunder.hack.events.impl;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import thunder.hack.events.Event;

public class EventCollision
extends Event {
    private BlockState bs;
    private BlockPos bp;

    public EventCollision(BlockState bs, BlockPos bp) {
        this.bs = bs;
        this.bp = bp;
    }

    public BlockState getState() {
        return this.bs;
    }

    public BlockPos getPos() {
        return this.bp;
    }

    public void setState(BlockState bs) {
        this.bs = bs;
    }
}

