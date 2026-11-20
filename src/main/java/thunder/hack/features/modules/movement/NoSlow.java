/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  meteordevelopment.orbit.EventHandler
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.Direction
 */
package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import thunder.hack.events.impl.EventKeyboardInput;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.SettingGroup;

public class NoSlow
extends Module {
    public final Setting<Mode> mode = new Setting<Mode>("Mode", Mode.NCP);
    private final Setting<Boolean> mainHand = new Setting<Boolean>("MainHand", true);
    private final Setting<SettingGroup> selection = new Setting<SettingGroup>("Selection", new SettingGroup(false, 0));
    private final Setting<Boolean> food = new Setting<Boolean>("Food", true).addToGroup(this.selection);
    private final Setting<Boolean> projectiles = new Setting<Boolean>("Projectiles", true).addToGroup(this.selection);
    private final Setting<Boolean> shield = new Setting<Boolean>("Shield", true).addToGroup(this.selection);
    public final Setting<Boolean> soulSand = new Setting<Boolean>("SoulSand", true).addToGroup(this.selection);
    public final Setting<Boolean> honey = new Setting<Boolean>("Honey", true).addToGroup(this.selection);
    public final Setting<Boolean> slime = new Setting<Boolean>("Slime", true).addToGroup(this.selection);
    public final Setting<Boolean> ice = new Setting<Boolean>("Ice", true).addToGroup(this.selection);
    public final Setting<Boolean> sweetBerryBush = new Setting<Boolean>("SweetBerryBush", true).addToGroup(this.selection);
    public final Setting<Boolean> sneak = new Setting<Boolean>("Sneak", false).addToGroup(this.selection);
    public final Setting<Boolean> crawl = new Setting<Boolean>("Crawl", false).addToGroup(this.selection);
    private boolean returnSneak;

    public NoSlow() {
        super("NoSlow", Module.Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        if (this.returnSneak) {
            NoSlow.mc.options.sneakKey.setPressed(false);
            NoSlow.mc.player.setSprinting(true);
            this.returnSneak = false;
        }
        if (NoSlow.mc.player.isUsingItem() && !NoSlow.mc.player.isRiding() && !NoSlow.mc.player.isFallFlying()) {
            switch (this.mode.getValue().ordinal()) {
                case 1: {
                    this.sendPacket((Packet<?>)new UpdateSelectedSlotC2SPacket(NoSlow.mc.player.getInventory().selectedSlot));
                    break;
                }
                case 4: {
                    if (!NoSlow.mc.player.isOnGround() || !NoSlow.mc.options.jumpKey.isPressed()) break;
                    NoSlow.mc.options.sneakKey.setPressed(true);
                    this.returnSneak = true;
                    break;
                }
                case 3: {
                    if (NoSlow.mc.player.getActiveHand() == Hand.OFF_HAND) {
                        this.sendPacket((Packet<?>)new UpdateSelectedSlotC2SPacket(NoSlow.mc.player.getInventory().selectedSlot % 8 + 1));
                        this.sendPacket((Packet<?>)new UpdateSelectedSlotC2SPacket(NoSlow.mc.player.getInventory().selectedSlot % 7 + 2));
                        this.sendPacket((Packet<?>)new UpdateSelectedSlotC2SPacket(NoSlow.mc.player.getInventory().selectedSlot));
                        break;
                    }
                    if (!this.mainHand.getValue().booleanValue()) break;
                    this.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id, NoSlow.mc.player.getYaw(), NoSlow.mc.player.getPitch()));
                    break;
                }
                case 2: {
                    if (NoSlow.mc.player.isOnGround() && !NoSlow.mc.options.jumpKey.isPressed()) {
                        NoSlow.mc.player.setVelocity(NoSlow.mc.player.getVelocity().x * 0.3, NoSlow.mc.player.getVelocity().y, NoSlow.mc.player.getVelocity().z * 0.3);
                        break;
                    }
                    if (!(NoSlow.mc.player.fallDistance > 0.2f)) break;
                    NoSlow.mc.player.setVelocity(NoSlow.mc.player.getVelocity().x * (double)0.95f, NoSlow.mc.player.getVelocity().y, NoSlow.mc.player.getVelocity().z * (double)0.95f);
                    break;
                }
                case 5: {
                    if (NoSlow.mc.player.getActiveHand() == Hand.OFF_HAND) {
                        this.sendPacket((Packet<?>)new UpdateSelectedSlotC2SPacket(NoSlow.mc.player.getInventory().selectedSlot % 8 + 1));
                        this.sendPacket((Packet<?>)new UpdateSelectedSlotC2SPacket(NoSlow.mc.player.getInventory().selectedSlot % 7 + 2));
                        this.sendPacket((Packet<?>)new UpdateSelectedSlotC2SPacket(NoSlow.mc.player.getInventory().selectedSlot));
                        break;
                    }
                    if (!this.mainHand.getValue().booleanValue() || NoSlow.mc.player.getItemUseTime() > 3 && NoSlow.mc.player.age % 2 != 0) break;
                    this.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id, NoSlow.mc.player.getYaw(), NoSlow.mc.player.getPitch()));
                    break;
                }
                case 6: {
                    if (!NoSlow.mc.player.isOnGround()) break;
                    if (NoSlow.mc.player.age % 2 == 0) {
                        NoSlow.mc.player.setVelocity(NoSlow.mc.player.getVelocity().x * 0.5, NoSlow.mc.player.getVelocity().y, NoSlow.mc.player.getVelocity().z * 0.5);
                        break;
                    }
                    NoSlow.mc.player.setVelocity(NoSlow.mc.player.getVelocity().x * (double)0.95f, NoSlow.mc.player.getVelocity().y, NoSlow.mc.player.getVelocity().z * (double)0.95f);
                    break;
                }
                case 7: {
                    if (NoSlow.mc.player.getItemUseTime() > 3) break;
                    this.sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, NoSlow.mc.player.getBlockPos().up(), Direction.NORTH, id));
                }
            }
        }
    }

    @EventHandler
    public void onKeyboardInput(EventKeyboardInput e) {
        if (this.mode.getValue() == Mode.Matrix3 && NoSlow.mc.player.isUsingItem() && !NoSlow.mc.player.isFallFlying()) {
            NoSlow.mc.player.input.movementForward *= 5.0f;
            NoSlow.mc.player.input.movementSideways *= 5.0f;
            float mult = 1.0f;
            if (NoSlow.mc.player.isOnGround()) {
                if (NoSlow.mc.player.input.movementForward != 0.0f && NoSlow.mc.player.input.movementSideways != 0.0f) {
                    NoSlow.mc.player.input.movementForward *= 0.35f;
                    NoSlow.mc.player.input.movementSideways *= 0.35f;
                } else {
                    NoSlow.mc.player.input.movementForward *= 0.5f;
                    NoSlow.mc.player.input.movementSideways *= 0.5f;
                }
            } else {
                mult = NoSlow.mc.player.input.movementForward != 0.0f && NoSlow.mc.player.input.movementSideways != 0.0f ? 0.47f : 0.67f;
            }
            NoSlow.mc.player.input.movementForward *= mult;
            NoSlow.mc.player.input.movementSideways *= mult;
        }
    }

    public boolean canNoSlow() {
        if (this.mode.getValue() == Mode.Matrix3) {
            return false;
        }
        if (!this.food.getValue().booleanValue() && NoSlow.mc.player.getActiveItem().getComponents().contains(DataComponentTypes.FOOD)) {
            return false;
        }
        if (!this.shield.getValue().booleanValue() && NoSlow.mc.player.getActiveItem().getItem() == Items.SHIELD) {
            return false;
        }
        if (!(this.projectiles.getValue().booleanValue() || NoSlow.mc.player.getActiveItem().getItem() != Items.CROSSBOW && NoSlow.mc.player.getActiveItem().getItem() != Items.BOW && NoSlow.mc.player.getActiveItem().getItem() != Items.TRIDENT)) {
            return false;
        }
        if (this.mode.getValue() == Mode.MusteryGrief && NoSlow.mc.player.isOnGround() && !NoSlow.mc.options.jumpKey.isPressed()) {
            return false;
        }
        if (!this.mainHand.getValue().booleanValue() && NoSlow.mc.player.getActiveHand() == Hand.MAIN_HAND) {
            return false;
        }
        return !NoSlow.mc.player.getOffHandStack().getComponents().contains(DataComponentTypes.FOOD) && NoSlow.mc.player.getOffHandStack().getItem() != Items.SHIELD || this.mode.getValue() != Mode.GrimNew && this.mode.getValue() != Mode.Grim || NoSlow.mc.player.getActiveHand() != Hand.MAIN_HAND;
    }

    public static enum Mode {
        NCP,
        StrictNCP,
        Matrix,
        Grim,
        MusteryGrief,
        GrimNew,
        Matrix2,
        LFCraft,
        Matrix3;

    }
}

