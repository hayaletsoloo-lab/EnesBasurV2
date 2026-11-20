/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  meteordevelopment.orbit.EventHandler
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.client.option.KeyBinding
 *  net.minecraft.client.util.InputUtil
 *  net.minecraft.entity.Entity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$PositionAndOnGround
 *  net.minecraft.screen.slot.SlotActionType
 */
package thunder.hack.features.modules.movement;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import thunder.hack.events.impl.EventClickSlot;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;

public class GuiMove
extends Module {
    private final Setting<Bypass> clickBypass = new Setting<Bypass>("Bypass", Bypass.None);
    private final Setting<Boolean> rotateOnArrows = new Setting<Boolean>("RotateOnArrows", true);
    private final Setting<Boolean> sneak = new Setting<Boolean>("sneak", false);
    private final Queue<ClickSlotC2SPacket> storedClicks = new LinkedList<ClickSlotC2SPacket>();
    private AtomicBoolean pause = new AtomicBoolean();

    public GuiMove() {
        super("GuiMove", Module.Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        if (GuiMove.mc.currentScreen != null && !(GuiMove.mc.currentScreen instanceof ChatScreen)) {
            for (KeyBinding k : new KeyBinding[]{GuiMove.mc.options.forwardKey, GuiMove.mc.options.backKey, GuiMove.mc.options.leftKey, GuiMove.mc.options.rightKey, GuiMove.mc.options.jumpKey, GuiMove.mc.options.sprintKey}) {
                k.setPressed(this.isKeyPressed(InputUtil.fromTranslationKey((String)k.getBoundKeyTranslationKey()).getCode()));
            }
            float deltaX = 0.0f;
            float deltaY = 0.0f;
            if (this.rotateOnArrows.getValue().booleanValue()) {
                if (this.isKeyPressed(264)) {
                    deltaY += 30.0f;
                }
                if (this.isKeyPressed(265)) {
                    deltaY -= 30.0f;
                }
                if (this.isKeyPressed(262)) {
                    deltaX += 30.0f;
                }
                if (this.isKeyPressed(263)) {
                    deltaX -= 30.0f;
                }
                if (deltaX != 0.0f || deltaY != 0.0f) {
                    GuiMove.mc.player.changeLookDirection((double)deltaX, (double)deltaY);
                }
            }
            if (this.sneak.getValue().booleanValue()) {
                GuiMove.mc.options.sneakKey.setPressed(this.isKeyPressed(InputUtil.fromTranslationKey((String)GuiMove.mc.options.sneakKey.getBoundKeyTranslationKey()).getCode()));
            }
        }
    }

    @EventHandler
    public void onClickSlot(EventClickSlot e) {
        if (this.clickBypass.is(Bypass.DisableClicks) && (MovementUtility.isMoving() || GuiMove.mc.options.jumpKey.isPressed())) {
            e.cancel();
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (!MovementUtility.isMoving() || !GuiMove.mc.options.jumpKey.isPressed() || this.pause.get()) {
            return;
        }
        Object t = e.getPacket();
        if (t instanceof ClickSlotC2SPacket) {
            ClickSlotC2SPacket click = (ClickSlotC2SPacket)t;
            switch (this.clickBypass.getValue().ordinal()) {
                case 3: {
                    if (click.getActionType() == SlotActionType.PICKUP || click.getActionType() == SlotActionType.PICKUP_ALL) break;
                    this.sendPacket((Packet<?>)new CloseHandledScreenC2SPacket(0));
                    break;
                }
                case 2: {
                    if (!GuiMove.mc.player.isOnGround() || GuiMove.mc.world.getBlockCollisions((Entity)GuiMove.mc.player, GuiMove.mc.player.getBoundingBox().offset(0.0, 0.0656, 0.0)).iterator().hasNext()) break;
                    if (GuiMove.mc.player.isSprinting()) {
                        this.sendPacket((Packet<?>)new ClientCommandC2SPacket((Entity)GuiMove.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                    }
                    this.sendPacket((Packet<?>)new PlayerMoveC2SPacket.PositionAndOnGround(GuiMove.mc.player.getX(), GuiMove.mc.player.getY() + 0.0656, GuiMove.mc.player.getZ(), false));
                    break;
                }
                case 6: {
                    if (!GuiMove.mc.player.isOnGround() || GuiMove.mc.world.getBlockCollisions((Entity)GuiMove.mc.player, GuiMove.mc.player.getBoundingBox().offset(0.0, 2.71875E-7, 0.0)).iterator().hasNext()) break;
                    if (GuiMove.mc.player.isSprinting()) {
                        this.sendPacket((Packet<?>)new ClientCommandC2SPacket((Entity)GuiMove.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                    }
                    this.sendPacket((Packet<?>)new PlayerMoveC2SPacket.PositionAndOnGround(GuiMove.mc.player.getX(), GuiMove.mc.player.getY() + 2.71875E-7, GuiMove.mc.player.getZ(), false));
                    break;
                }
                case 4: {
                    this.sendPacket((Packet<?>)new ClientCommandC2SPacket((Entity)GuiMove.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                    GuiMove.mc.options.forwardKey.setPressed(false);
                    GuiMove.mc.player.input.movementForward = 0.0f;
                    GuiMove.mc.player.input.pressingForward = false;
                    break;
                }
                case 5: {
                    this.storedClicks.add(click);
                    e.cancel();
                }
            }
        }
        if (e.getPacket() instanceof CloseHandledScreenC2SPacket && this.clickBypass.is(Bypass.Delay)) {
            this.pause.set(true);
            while (!this.storedClicks.isEmpty()) {
                this.sendPacket((Packet)(Object)this.storedClicks.poll());
            }
            this.pause.set(false);
        }
    }

    @EventHandler
    public void onPacketSendPost(PacketEvent.SendPost e) {
        if (e.getPacket() instanceof ClickSlotC2SPacket && GuiMove.mc.player.isSprinting() && this.clickBypass.is(Bypass.StrictNCP)) {
            this.sendPacket((Packet<?>)new ClientCommandC2SPacket((Entity)GuiMove.mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        }
    }

    private static enum Bypass {
        DisableClicks,
        None,
        StrictNCP,
        GrimSwap,
        MatrixNcp,
        Delay,
        StrictNCP2;

    }
}

