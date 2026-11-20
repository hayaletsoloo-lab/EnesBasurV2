/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  meteordevelopment.orbit.EventHandler
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.gui.screen.ingame.GenericContainerScreen
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.TntEntity
 *  net.minecraft.entity.decoration.EndCrystalEntity
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.mob.CreeperEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.entity.vehicle.TntMinecartEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.ShieldItem
 *  net.minecraft.item.SwordItem
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket
 *  net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
 *  net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 *  org.jetbrains.annotations.NotNull
 */
package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.injection.accesors.IMinecraftClient;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.PredictUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.world.ExplosionUtility;

public final class AutoTotem
extends Module {
    private final Setting<Mode> mode = new Setting<Mode>("Mode", Mode.Matrix);
    private final Setting<OffHand> offhand = new Setting<OffHand>("Item", OffHand.Totem);
    private final Setting<BooleanSettingGroup> bindSwap = new Setting<BooleanSettingGroup>("BindSwap", new BooleanSettingGroup(false), v -> this.offhand.is(OffHand.Totem));
    private final Setting<Bind> swapButton = new Setting<Bind>("SwapButton", new Bind(280, false, false)).addToGroup(this.bindSwap);
    private final Setting<Swap> swapMode = new Setting<Swap>("Swap", Swap.GappleShield).addToGroup(this.bindSwap);
    private final Setting<Boolean> ncpStrict = new Setting<Boolean>("NCPStrict", false);
    private final Setting<Float> healthF = new Setting<Float>("HP", Float.valueOf(16.0f), Float.valueOf(0.0f), Float.valueOf(36.0f));
    private final Setting<Float> healthS = new Setting<Float>("ShieldGappleHp", Float.valueOf(16.0f), Float.valueOf(0.0f), Float.valueOf(20.0f), v -> this.offhand.getValue() == OffHand.Shield);
    private final Setting<Boolean> calcAbsorption = new Setting<Boolean>("CalcAbsorption", true);
    private final Setting<Boolean> stopMotion = new Setting<Boolean>("StopMotion", false);
    private final Setting<Boolean> resetAttackCooldown = new Setting<Boolean>("ResetAttackCooldown", false);
    private final Setting<SettingGroup> safety = new Setting<SettingGroup>("Safety", new SettingGroup(false, 0));
    private final Setting<Boolean> hotbarFallBack = new Setting<Boolean>("HotbarFallback", false).addToGroup(this.safety);
    private final Setting<Boolean> fallBackCalc = new Setting<Boolean>("FallBackCalc", true, v -> this.hotbarFallBack.getValue()).addToGroup(this.safety);
    private final Setting<Boolean> onElytra = new Setting<Boolean>("OnElytra", true).addToGroup(this.safety);
    private final Setting<Boolean> onFall = new Setting<Boolean>("OnFall", true).addToGroup(this.safety);
    private final Setting<Boolean> onCrystal = new Setting<Boolean>("OnCrystal", true).addToGroup(this.safety);
    private final Setting<Boolean> onObsidianPlace = new Setting<Boolean>("OnObsidianPlace", false).addToGroup(this.safety);
    private final Setting<Boolean> onCrystalInHand = new Setting<Boolean>("OnCrystalInHand", false).addToGroup(this.safety);
    private final Setting<Boolean> onMinecartTnt = new Setting<Boolean>("OnMinecartTNT", true).addToGroup(this.safety);
    private final Setting<Boolean> onCreeper = new Setting<Boolean>("OnCreeper", true).addToGroup(this.safety);
    private final Setting<Boolean> onAnchor = new Setting<Boolean>("OnAnchor", true).addToGroup(this.safety);
    private final Setting<Boolean> onTnt = new Setting<Boolean>("OnTNT", true).addToGroup(this.safety);
    public final Setting<RCGap> rcGap = new Setting<RCGap>("RightClickGapple", RCGap.Off);
    private final Setting<Boolean> crappleSpoof = new Setting<Boolean>("CrappleSpoof", true, v -> this.offhand.getValue() == OffHand.GApple);
    private int delay;
    private Timer bindDelay = new Timer();
    private Item prevItem;

    public AutoTotem() {
        super("AutoTotem", Module.Category.COMBAT);
    }

    @EventHandler
    public void onSync(EventSync e) {
        this.swapTo(this.getItemSlot());
        if (this.rcGap.not(RCGap.Off) && AutoTotem.mc.player.getMainHandStack().getItem() instanceof SwordItem && AutoTotem.mc.options.useKey.isPressed() && !AutoTotem.mc.player.isUsingItem()) {
            ((IMinecraftClient)mc).idoItemUse();
        }
        --this.delay;
    }

    @EventHandler
    public void onPacketReceive(@NotNull PacketEvent.Receive e) {
        BlockUpdateS2CPacket blockUpdate;
        EntitySpawnS2CPacket spawn;
        Object t = e.getPacket();
        if (t instanceof EntitySpawnS2CPacket && (spawn = (EntitySpawnS2CPacket)t).getEntityType() == EntityType.END_CRYSTAL && this.getPlayerPos().squaredDistanceTo(spawn.getX(), spawn.getY(), spawn.getZ()) < 36.0) {
            if (this.hotbarFallBack.getValue().booleanValue()) {
                Vec3d VanillaChestLootTableGenerator;
                if (this.fallBackCalc.getValue().booleanValue() && ExplosionUtility.getExplosionDamageWPredict(VanillaChestLootTableGenerator = new Vec3d(spawn.getX(), spawn.getY(), spawn.getZ()), (PlayerEntity)AutoTotem.mc.player, PredictUtility.createBox(this.getPlayerPos(), (Entity)AutoTotem.mc.player), false) < this.getTriggerHealth() + 4.0f) {
                    return;
                }
                this.runInstant();
            }
            if (this.onCrystal.getValue().booleanValue()) {
                Vec3d WallPlayerSkullBlock = new Vec3d(spawn.getX(), spawn.getY(), spawn.getZ());
                if ((double)(this.getTriggerHealth() - ExplosionUtility.getExplosionDamageWPredict(WallPlayerSkullBlock, (PlayerEntity)AutoTotem.mc.player, PredictUtility.createBox(this.getPlayerPos(), (Entity)AutoTotem.mc.player), false)) < 0.5) {
                    int slot = -1;
                    for (int i = 9; i < 45; ++i) {
                        if (!AutoTotem.mc.player.getInventory().getStack(i >= 36 ? i - 36 : i).getItem().equals(Items.TOTEM_OF_UNDYING)) continue;
                        slot = i >= 36 ? i - 36 : i;
                        break;
                    }
                    this.swapTo(slot);
                    this.debug("spawn switch");
                }
            }
        }
        if ((t = e.getPacket()) instanceof BlockUpdateS2CPacket && (blockUpdate = (BlockUpdateS2CPacket)t).getState().getBlock() == Blocks.OBSIDIAN && this.onObsidianPlace.getValue().booleanValue() && this.getPlayerPos().squaredDistanceTo(blockUpdate.getPos().toCenterPos()) < 36.0 && this.delay <= 0) {
            this.runInstant();
        }
    }

    private float getTriggerHealth() {
        return AutoTotem.mc.player.getHealth() + (this.calcAbsorption.getValue() != false ? AutoTotem.mc.player.getAbsorptionAmount() : 0.0f);
    }

    private void runInstant() {
        SearchInvResult hotbarResult = InventoryUtility.findItemInHotBar(Items.TOTEM_OF_UNDYING);
        SearchInvResult invResult = InventoryUtility.findItemInInventory(Items.TOTEM_OF_UNDYING);
        if (hotbarResult.found()) {
            hotbarResult.switchTo();
            this.delay = 20;
        } else if (invResult.found()) {
            int slot = invResult.slot() >= 36 ? invResult.slot() - 36 : invResult.slot();
            int n = slot;
            if (!this.hotbarFallBack.getValue().booleanValue()) {
                this.swapTo(slot);
            } else {
                AutoTotem.mc.interactionManager.pickFromInventory(slot);
            }
            this.delay = 20;
        }
    }

    public void swapTo(int slot) {
        block14: {
            block16: {
                int prevCurrentItem;
                block15: {
                    if (slot == -1 || this.delay > 0) break block14;
                    if (AutoTotem.mc.currentScreen instanceof GenericContainerScreen) {
                        return;
                    }
                    if (this.stopMotion.getValue().booleanValue()) {
                        AutoTotem.mc.player.setVelocity(0.0, AutoTotem.mc.player.getVelocity().getY(), 0.0);
                    }
                    int nearestSlot = AutoTotem.findNearestCurrentItem();
                    prevCurrentItem = AutoTotem.mc.player.getInventory().selectedSlot;
                    if (slot < 9) break block15;
                    switch (this.mode.getValue().ordinal()) {
                        case 0: {
                            if (this.ncpStrict.getValue().booleanValue()) {
                                this.sendPacket((Packet<?>)new ClientCommandC2SPacket((Entity)AutoTotem.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                            }
                            AutoTotem.clickSlot(slot);
                            AutoTotem.clickSlot(45);
                            AutoTotem.clickSlot(slot);
                            this.sendPacket((Packet<?>)new CloseHandledScreenC2SPacket(AutoTotem.mc.player.currentScreenHandler.syncId));
                            break;
                        }
                        case 1: {
                            if (this.ncpStrict.getValue().booleanValue()) {
                                this.sendPacket((Packet<?>)new ClientCommandC2SPacket((Entity)AutoTotem.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                            }
                            AutoTotem.clickSlot(slot, nearestSlot, SlotActionType.SWAP);
                            AutoTotem.clickSlot(45, nearestSlot, SlotActionType.SWAP);
                            AutoTotem.clickSlot(slot, nearestSlot, SlotActionType.SWAP);
                            this.sendPacket((Packet<?>)new CloseHandledScreenC2SPacket(AutoTotem.mc.player.currentScreenHandler.syncId));
                            break;
                        }
                        case 2: {
                            if (this.ncpStrict.getValue().booleanValue()) {
                                this.sendPacket((Packet<?>)new ClientCommandC2SPacket((Entity)AutoTotem.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                            }
                            AutoTotem.mc.interactionManager.clickSlot(AutoTotem.mc.player.currentScreenHandler.syncId, slot, nearestSlot, SlotActionType.SWAP, (PlayerEntity)AutoTotem.mc.player);
                            this.debug(slot + " " + nearestSlot);
                            this.sendPacket((Packet<?>)new UpdateSelectedSlotC2SPacket(nearestSlot));
                            AutoTotem.mc.player.getInventory().selectedSlot = nearestSlot;
                            ItemStack itemstack = AutoTotem.mc.player.getOffHandStack();
                            AutoTotem.mc.player.setStackInHand(Hand.OFF_HAND, AutoTotem.mc.player.getMainHandStack());
                            AutoTotem.mc.player.setStackInHand(Hand.MAIN_HAND, itemstack);
                            this.sendPacket((Packet<?>)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                            this.sendPacket((Packet<?>)new UpdateSelectedSlotC2SPacket(prevCurrentItem));
                            AutoTotem.mc.player.getInventory().selectedSlot = prevCurrentItem;
                            AutoTotem.mc.interactionManager.clickSlot(AutoTotem.mc.player.currentScreenHandler.syncId, slot, nearestSlot, SlotActionType.SWAP, (PlayerEntity)AutoTotem.mc.player);
                            this.sendPacket((Packet<?>)new CloseHandledScreenC2SPacket(AutoTotem.mc.player.currentScreenHandler.syncId));
                            if (this.resetAttackCooldown.getValue().booleanValue()) {
                                AutoTotem.mc.player.resetLastAttackedTicks();
                                break;
                            }
                            break block16;
                        }
                        case 3: {
                            this.debug(slot + " pick");
                            this.sendPacket((Packet<?>)new PickFromInventoryC2SPacket(slot));
                            this.sendPacket((Packet<?>)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                            int prevSlot = AutoTotem.mc.player.getInventory().selectedSlot;
                            Managers.ASYNC.run(() -> {
                                AutoTotem.mc.player.getInventory().selectedSlot = prevSlot;
                            }, 300L);
                            break;
                        }
                        case 4: {
                            this.debug(slot + " swap");
                            AutoTotem.mc.interactionManager.clickSlot(AutoTotem.mc.player.currentScreenHandler.syncId, slot, 40, SlotActionType.SWAP, (PlayerEntity)AutoTotem.mc.player);
                            this.sendPacket((Packet<?>)new CloseHandledScreenC2SPacket(AutoTotem.mc.player.currentScreenHandler.syncId));
                        }
                    }
                    break block16;
                }
                this.sendPacket((Packet<?>)new UpdateSelectedSlotC2SPacket(slot));
                AutoTotem.mc.player.getInventory().selectedSlot = slot;
                this.debug(slot + " select");
                this.sendPacket((Packet<?>)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                this.sendPacket((Packet<?>)new UpdateSelectedSlotC2SPacket(prevCurrentItem));
                AutoTotem.mc.player.getInventory().selectedSlot = prevCurrentItem;
                if (this.resetAttackCooldown.getValue().booleanValue()) {
                    AutoTotem.mc.player.resetLastAttackedTicks();
                }
            }
            this.delay = (int)(2.0f + (float)Managers.SERVER.getPing() / 25.0f);
        }
    }

    public static int findNearestCurrentItem() {
        int i = AutoTotem.mc.player.getInventory().selectedSlot;
        if (i == 8) {
            return 7;
        }
        if (i == 0) {
            return 1;
        }
        return i - 1;
    }

    /*
     * WARNING - void declaration
     */
    public int getItemSlot() {
        if (AutoTotem.mc.player == null || AutoTotem.mc.world == null) {
            return -1;
        }
        SearchInvResult gapple = InventoryUtility.findItemInInventory(Items.ENCHANTED_GOLDEN_APPLE);
        SearchInvResult crapple = InventoryUtility.findItemInInventory(Items.GOLDEN_APPLE);
        SearchInvResult shield = InventoryUtility.findItemInInventory(Items.SHIELD);
        Item offHandItem = AutoTotem.mc.player.getOffHandStack().getItem();
        int itemSlot = -1;
        Item item = null;
        switch (this.offhand.getValue().ordinal()) {
            case 0: {
                if (offHandItem != Items.TOTEM_OF_UNDYING && !AutoTotem.mc.player.getOffHandStack().isEmpty()) {
                    this.prevItem = offHandItem;
                }
                item = this.prevItem;
                if (!this.bindSwap.getValue().isEnabled() || !this.isKeyPressed(this.swapButton) || !this.bindDelay.every(250L)) break;
                switch (this.swapMode.getValue().ordinal()) {
                    case 1: {
                        if (AutoTotem.mc.player.getOffHandStack().isEmpty() || offHandItem == Items.SHIELD) {
                            item = Items.PLAYER_HEAD;
                            break;
                        }
                        item = Items.SHIELD;
                        break;
                    }
                    case 2: {
                        if (AutoTotem.mc.player.getOffHandStack().isEmpty() || offHandItem == Items.GOLDEN_APPLE) {
                            item = Items.PLAYER_HEAD;
                            break;
                        }
                        item = Items.GOLDEN_APPLE;
                        break;
                    }
                    case 0: {
                        if (AutoTotem.mc.player.getOffHandStack().isEmpty() || offHandItem == Items.SHIELD) {
                            item = Items.GOLDEN_APPLE;
                            break;
                        }
                        item = Items.SHIELD;
                        break;
                    }
                    case 3: {
                        item = AutoTotem.mc.player.getOffHandStack().isEmpty() || offHandItem == Items.TOTEM_OF_UNDYING ? Items.PLAYER_HEAD : Items.TOTEM_OF_UNDYING;
                    }
                }
                this.prevItem = item;
                break;
            }
            case 1: {
                item = Items.END_CRYSTAL;
                break;
            }
            case 2: {
                if (this.crappleSpoof.getValue().booleanValue()) {
                    if (AutoTotem.mc.player.hasStatusEffect(StatusEffects.ABSORPTION) && AutoTotem.mc.player.getStatusEffect(StatusEffects.ABSORPTION).getAmplifier() > 2) {
                        if (crapple.found() || offHandItem == Items.GOLDEN_APPLE) {
                            item = Items.GOLDEN_APPLE;
                            break;
                        }
                        if (!gapple.found() && offHandItem != Items.ENCHANTED_GOLDEN_APPLE) break;
                        item = Items.ENCHANTED_GOLDEN_APPLE;
                        break;
                    }
                    if (gapple.found() || offHandItem == Items.ENCHANTED_GOLDEN_APPLE) {
                        item = Items.ENCHANTED_GOLDEN_APPLE;
                        break;
                    }
                    if (!crapple.found() && offHandItem != Items.GOLDEN_APPLE) break;
                    item = Items.GOLDEN_APPLE;
                    break;
                }
                if (crapple.found() || offHandItem == Items.GOLDEN_APPLE) {
                    item = Items.GOLDEN_APPLE;
                    break;
                }
                if (!gapple.found() && offHandItem != Items.ENCHANTED_GOLDEN_APPLE) break;
                item = Items.ENCHANTED_GOLDEN_APPLE;
                break;
            }
            case 3: {
                if (shield.found() || offHandItem == Items.SHIELD) {
                    if (this.getTriggerHealth() <= this.healthS.getValue().floatValue()) {
                        if (crapple.found() || offHandItem == Items.GOLDEN_APPLE) {
                            item = Items.GOLDEN_APPLE;
                            break;
                        }
                        if (!gapple.found() && offHandItem != Items.ENCHANTED_GOLDEN_APPLE) break;
                        item = Items.ENCHANTED_GOLDEN_APPLE;
                        break;
                    }
                    if (!AutoTotem.mc.player.getItemCooldownManager().isCoolingDown(Items.SHIELD)) {
                        item = Items.SHIELD;
                        break;
                    }
                    if (crapple.found() || offHandItem == Items.GOLDEN_APPLE) {
                        item = Items.GOLDEN_APPLE;
                        break;
                    }
                    if (!gapple.found() && offHandItem != Items.ENCHANTED_GOLDEN_APPLE) break;
                    item = Items.ENCHANTED_GOLDEN_APPLE;
                    break;
                }
                if (!crapple.found() && offHandItem != Items.GOLDEN_APPLE) break;
                item = Items.GOLDEN_APPLE;
            }
        }
        if (this.getTriggerHealth() <= this.healthF.getValue().floatValue() && (InventoryUtility.findItemInInventory(Items.TOTEM_OF_UNDYING).found() || offHandItem == Items.TOTEM_OF_UNDYING)) {
            item = Items.TOTEM_OF_UNDYING;
        }
        if (!this.rcGap.is(RCGap.Off) && AutoTotem.mc.player.getMainHandStack().getItem() instanceof SwordItem && AutoTotem.mc.options.useKey.isPressed() && !(offHandItem instanceof ShieldItem) && (this.rcGap.is(RCGap.Always) || this.rcGap.is(RCGap.OnlySafe) && this.getTriggerHealth() > this.healthF.getValue().floatValue())) {
            if (crapple.found() || offHandItem == Items.GOLDEN_APPLE) {
                item = Items.GOLDEN_APPLE;
            }
            if (gapple.found() || offHandItem == Items.ENCHANTED_GOLDEN_APPLE) {
                item = Items.ENCHANTED_GOLDEN_APPLE;
            }
        }
        if (this.onFall.getValue().booleanValue() && (double)(this.getTriggerHealth() - ((AutoTotem.mc.player.fallDistance - 3.0f) / 2.0f + 3.5f)) < 0.5) {
            item = Items.TOTEM_OF_UNDYING;
        }
        if (this.onElytra.getValue().booleanValue() && AutoTotem.mc.player.isFallFlying()) {
            item = Items.TOTEM_OF_UNDYING;
        }
        if (this.onCrystalInHand.getValue().booleanValue()) {
            for (PlayerEntity playerEntity : Managers.ASYNC.getAsyncPlayers()) {
                if (Managers.FRIEND.isFriend(playerEntity) || playerEntity == AutoTotem.mc.player || !(this.getPlayerPos().squaredDistanceTo(playerEntity.getPos()) < 36.0) || playerEntity.getMainHandStack().getItem() != Items.OBSIDIAN && playerEntity.getMainHandStack().getItem() != Items.END_CRYSTAL && playerEntity.getOffHandStack().getItem() != Items.OBSIDIAN && playerEntity.getOffHandStack().getItem() != Items.END_CRYSTAL) continue;
                item = Items.TOTEM_OF_UNDYING;
            }
        }
        for (Entity entity : AutoTotem.mc.world.getEntities()) {
            if (entity == null || !entity.isAlive() || this.getPlayerPos().squaredDistanceTo(entity.getPos()) > 36.0) continue;
            if (this.onCrystal.getValue().booleanValue() && entity instanceof EndCrystalEntity && (double)(this.getTriggerHealth() - ExplosionUtility.getExplosionDamageWPredict(entity.getPos(), (PlayerEntity)AutoTotem.mc.player, PredictUtility.createBox(this.getPlayerPos(), (Entity)AutoTotem.mc.player), false)) < 0.5) {
                item = Items.TOTEM_OF_UNDYING;
                break;
            }
            if (this.onTnt.getValue().booleanValue() && entity instanceof TntEntity) {
                item = Items.TOTEM_OF_UNDYING;
                break;
            }
            if (this.onMinecartTnt.getValue().booleanValue() && entity instanceof TntMinecartEntity) {
                item = Items.TOTEM_OF_UNDYING;
                break;
            }
            if (!this.onCreeper.getValue().booleanValue() || !(entity instanceof CreeperEntity)) continue;
            item = Items.TOTEM_OF_UNDYING;
            break;
        }
        if (this.onAnchor.getValue().booleanValue()) {
            for (int x = -6; x <= 6; ++x) {
                int var8_14 = -6;
                while (var8_14 <= 6) {
                    for (int z = -6; z <= 6; ++z) {
                        BlockPos bp = new BlockPos(x, (int)var8_14, z);
                        if (AutoTotem.mc.world.getBlockState(bp).getBlock() != Blocks.RESPAWN_ANCHOR) continue;
                        item = Items.TOTEM_OF_UNDYING;
                        break;
                    }
                    ++var8_14;
                }
            }
        }
        for (int i = 9; i < 45; ++i) {
            if (AutoTotem.mc.player.getOffHandStack().getItem() == item) {
                return -1;
            }
            if (!AutoTotem.mc.player.getInventory().getStack(i >= 36 ? i - 36 : i).getItem().equals(item)) continue;
            itemSlot = i >= 36 ? i - 36 : i;
            break;
        }
        if (item == AutoTotem.mc.player.getMainHandStack().getItem() && AutoTotem.mc.options.useKey.isPressed()) {
            return -1;
        }
        return itemSlot;
    }

    private Vec3d getPlayerPos() {
        return AutoTotem.mc.player.getPos();
    }

    private static enum Mode {
        Default,
        Alternative,
        Matrix,
        MatrixPick,
        NewVersion;

    }

    private static enum OffHand {
        Totem,
        Crystal,
        GApple,
        Shield;

    }

    private static enum Swap {
        GappleShield,
        BallShield,
        GappleBall,
        BallTotem;

    }

    public static enum RCGap {
        Off,
        Always,
        OnlySafe;

    }
}

