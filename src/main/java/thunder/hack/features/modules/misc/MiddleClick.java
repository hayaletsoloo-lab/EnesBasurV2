/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  meteordevelopment.orbit.EventHandler
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.PotionContentsComponent
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$LookAndOnGround
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.EntityHitResult
 *  net.minecraft.util.hit.HitResult
 */
package thunder.hack.features.modules.misc;

import java.util.function.Consumer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.AsyncManager;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventSync;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlayerUtility;

public class MiddleClick
extends Module {
    private final Setting<Action> onBlock = new Setting<Action>("OnBlock", Action.Xp);
    private final Setting<Action> onAir = new Setting<Action>("OnAir", Action.Pearl);
    private final Setting<Action> onEntity = new Setting<Action>("OnEntity", Action.Friend);
    private final Setting<Action> onFlying = new Setting<Action>("OnElytra", Action.None);
    private final Setting<Boolean> silent = new Setting<Boolean>("Silent", true);
    private final Setting<Boolean> inventory = new Setting<Boolean>("Inventory", true);
    private final Setting<Integer> swapDelay = new Setting<Integer>("SwapDelay", 100, 0, 1000, v -> this.silent.getValue() == false);
    private final Setting<BooleanSettingGroup> antiWaste = new Setting<BooleanSettingGroup>("AntiWaste", new BooleanSettingGroup(true));
    private final Setting<Integer> durability = new Setting<Integer>("StopOn", 90, 0, 100).addToGroup(this.antiWaste);
    public final Setting<Boolean> antiPickUp = new Setting<Boolean>("AntiPickUp", true);
    private final Setting<Boolean> feetExp = new Setting<Boolean>("FeetXP", false);
    private static final Timer timer = new Timer();
    private String state = "none";

    public MiddleClick() {
        super("MiddleClick", Module.Category.MISC);
    }

    @Override
    public String getDisplayInfo() {
        return this.state;
    }

    @EventHandler
    private void onSync(EventSync event) {
        if (MiddleClick.mc.currentScreen == null) {
            EntityHitResult ehr;
            HitResult target = MiddleClick.mc.crosshairTarget;
            if (MiddleClick.mc.player.isFallFlying()) {
                if (MiddleClick.mc.options.pickItemKey.isPressed()) {
                    this.onFlying.getValue().doAction(event);
                }
                this.state = this.onFlying.getValue().toString();
                return;
            }
            if (target instanceof EntityHitResult && (ehr = (EntityHitResult)target).getEntity() instanceof PlayerEntity) {
                if (MiddleClick.mc.options.pickItemKey.isPressed()) {
                    this.onEntity.getValue().doAction(event);
                }
                this.state = this.onEntity.getValue().toString();
                return;
            }
            if (target instanceof BlockHitResult) {
                BlockHitResult bhr = (BlockHitResult)target;
                if (MiddleClick.mc.world.isAir(bhr.getBlockPos())) {
                    if (MiddleClick.mc.options.pickItemKey.isPressed()) {
                        this.onAir.getValue().doAction(event);
                    }
                    this.state = this.onAir.getValue().toString();
                } else {
                    if (MiddleClick.mc.options.pickItemKey.isPressed()) {
                        this.onBlock.getValue().doAction(event);
                    }
                    this.state = this.onBlock.getValue().toString();
                }
                return;
            }
            if (MiddleClick.mc.options.pickItemKey.isPressed()) {
                this.onAir.getValue().doAction(event);
            }
            this.state = this.onAir.getValue().toString();
        }
    }

    private static boolean needXp() {
        for (ItemStack stack : MiddleClick.mc.player.getArmorItems()) {
            if (!(PlayerUtility.calculatePercentage(stack) < (float)ModuleManager.middleClick.durability.getValue().intValue())) continue;
            return true;
        }
        return false;
    }

    private static int getHpSlot() {
        for (int i = 0; i < 9; ++i) {
            if (!MiddleClick.isStackPotion(MiddleClick.mc.player.getInventory().getStack(i))) continue;
            return i;
        }
        return -1;
    }

    private static int findHpInInventory() {
        for (int i = 36; i >= 0; --i) {
            if (!MiddleClick.isStackPotion(MiddleClick.mc.player.getInventory().getStack(i))) continue;
            return i < 9 ? i + 36 : i;
        }
        return -1;
    }

    private static boolean isStackPotion(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        if (stack.getItem() == Items.SPLASH_POTION) {
            PotionContentsComponent potionContentsComponent = (PotionContentsComponent)stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
            for (StatusEffectInstance effect : potionContentsComponent.getEffects()) {
                if (effect.getEffectType().value() != StatusEffects.INSTANT_HEALTH.value()) continue;
                return true;
            }
        }
        return false;
    }

    public static enum Action {
        HealingPot(e -> {
            if (timer.every(250L)) {
                if (MiddleClick.getHpSlot() != -1) {
                    int hpSlot = MiddleClick.getHpSlot();
                    int originalSlot = Module.mc.player.getInventory().selectedSlot;
                    if (hpSlot != -1) {
                        InventoryUtility.switchTo(hpSlot);
                        InteractionUtility.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Module.mc.player.getYaw(), Module.mc.player.getPitch()));
                        InventoryUtility.switchTo(originalSlot);
                    }
                } else {
                    int hpSlot = MiddleClick.findHpInInventory();
                    if (hpSlot != -1) {
                        Module.mc.interactionManager.clickSlot(Module.mc.player.currentScreenHandler.syncId, hpSlot, Module.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, (PlayerEntity)Module.mc.player);
                        InteractionUtility.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Module.mc.player.getYaw(), Module.mc.player.getPitch()));
                        Module.mc.player.networkHandler.sendPacket((Packet)new HandSwingC2SPacket(Hand.MAIN_HAND));
                        Module.mc.interactionManager.clickSlot(Module.mc.player.currentScreenHandler.syncId, hpSlot, Module.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, (PlayerEntity)Module.mc.player);
                    }
                }
            }
        }),
        Firework(e -> {
            if (timer.every(250L)) {
                int epSlot1 = InventoryUtility.findItemInHotBar(Items.FIREWORK_ROCKET).slot();
                if (ModuleManager.middleClick.silent.getValue().booleanValue()) {
                    if (!ModuleManager.middleClick.inventory.getValue().booleanValue() || ModuleManager.middleClick.inventory.getValue().booleanValue() && epSlot1 != -1) {
                        int originalSlot = Module.mc.player.getInventory().selectedSlot;
                        if (epSlot1 != -1) {
                            Module.mc.player.getInventory().selectedSlot = epSlot1;
                            Module.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(epSlot1));
                            InteractionUtility.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Module.mc.player.getYaw(), Module.mc.player.getPitch()));
                            Module.mc.player.networkHandler.sendPacket((Packet)new HandSwingC2SPacket(Hand.MAIN_HAND));
                            Module.mc.player.getInventory().selectedSlot = originalSlot;
                            Module.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(originalSlot));
                        }
                    } else {
                        int epSlot = InventoryUtility.findItemInInventory(Items.FIREWORK_ROCKET).slot();
                        if (epSlot != -1) {
                            Module.mc.interactionManager.clickSlot(Module.mc.player.currentScreenHandler.syncId, epSlot, Module.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, (PlayerEntity)Module.mc.player);
                            InteractionUtility.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Module.mc.player.getYaw(), Module.mc.player.getPitch()));
                            Module.mc.player.networkHandler.sendPacket((Packet)new HandSwingC2SPacket(Hand.MAIN_HAND));
                            Module.mc.interactionManager.clickSlot(Module.mc.player.currentScreenHandler.syncId, epSlot, Module.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, (PlayerEntity)Module.mc.player);
                        }
                    }
                } else if (!ModuleManager.middleClick.inventory.getValue().booleanValue() || ModuleManager.middleClick.inventory.getValue().booleanValue() && epSlot1 != -1) {
                    if (epSlot1 != -1) {
                        new PearlThread(Module.mc.player, epSlot1, Module.mc.player.getInventory().selectedSlot, ModuleManager.middleClick.swapDelay.getValue(), false).start();
                    }
                } else {
                    int epSlot = InventoryUtility.findItemInInventory(Items.FIREWORK_ROCKET).slot();
                    if (epSlot != -1) {
                        new PearlThread(Module.mc.player, epSlot, Module.mc.player.getInventory().selectedSlot, ModuleManager.middleClick.swapDelay.getValue(), true).start();
                    }
                }
            }
        }),
        Friend(e -> {
            EntityHitResult ehr;
            HitResult patt0$temp;
            if (timer.every(800L) && (patt0$temp = Module.mc.crosshairTarget) instanceof EntityHitResult && (ehr = (EntityHitResult)patt0$temp).getEntity() instanceof PlayerEntity) {
                if (Managers.FRIEND.isFriend(ehr.getEntity().getName().getString())) {
                    Managers.FRIEND.removeFriend(ehr.getEntity().getName().getString());
                    Command.sendMessage(ClientSettings.isRu() ? "\u00a7b" + ehr.getEntity().getName().getString() + "\u00a7r \u0443\u0434\u0430\u043b\u0435\u043d \u0438\u0437 \u0434\u0440\u0443\u0437\u0435\u0439!" : "\u00a7b" + ehr.getEntity().getName().getString() + "\u00a7r removed from friends!");
                } else {
                    Managers.FRIEND.addFriend(ehr.getEntity().getName().getString());
                    Command.sendMessage(ClientSettings.isRu() ? "\u0414\u043e\u0431\u0430\u0432\u043b\u0435\u043d \u0434\u0440\u0443\u0433 \u00a7b" + ehr.getEntity().getName().getString() : "Added friend \u00a7b" + ehr.getEntity().getName().getString());
                }
            }
        }),
        Pearl(e -> {
            if (timer.every(500L)) {
                if (ModuleManager.aura.isEnabled() && Aura.target != null) {
                    Module.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.LookAndOnGround(Module.mc.player.getYaw(), Module.mc.player.getPitch(), Module.mc.player.isOnGround()));
                }
                int epSlot1 = InventoryUtility.findItemInHotBar(Items.ENDER_PEARL).slot();
                if (ModuleManager.middleClick.silent.getValue().booleanValue()) {
                    if (!ModuleManager.middleClick.inventory.getValue().booleanValue() || ModuleManager.middleClick.inventory.getValue().booleanValue() && epSlot1 != -1) {
                        int originalSlot = Module.mc.player.getInventory().selectedSlot;
                        if (epSlot1 != -1) {
                            Module.mc.player.getInventory().selectedSlot = epSlot1;
                            Module.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(epSlot1));
                            InteractionUtility.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Module.mc.player.getYaw(), Module.mc.player.getPitch()));
                            Module.mc.player.networkHandler.sendPacket((Packet)new HandSwingC2SPacket(Hand.MAIN_HAND));
                            Module.mc.player.getInventory().selectedSlot = originalSlot;
                            Module.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(originalSlot));
                        }
                    } else {
                        int epSlot = InventoryUtility.findItemInInventory(Items.ENDER_PEARL).slot();
                        if (epSlot != -1) {
                            Module.mc.interactionManager.clickSlot(Module.mc.player.currentScreenHandler.syncId, epSlot, Module.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, (PlayerEntity)Module.mc.player);
                            InteractionUtility.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Module.mc.player.getYaw(), Module.mc.player.getPitch()));
                            Module.mc.player.networkHandler.sendPacket((Packet)new HandSwingC2SPacket(Hand.MAIN_HAND));
                            Module.mc.interactionManager.clickSlot(Module.mc.player.currentScreenHandler.syncId, epSlot, Module.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, (PlayerEntity)Module.mc.player);
                        }
                    }
                } else if (!ModuleManager.middleClick.inventory.getValue().booleanValue() || ModuleManager.middleClick.inventory.getValue().booleanValue() && epSlot1 != -1) {
                    if (epSlot1 != -1) {
                        new PearlThread(Module.mc.player, epSlot1, Module.mc.player.getInventory().selectedSlot, ModuleManager.middleClick.swapDelay.getValue(), false).start();
                    }
                } else {
                    int epSlot = InventoryUtility.findItemInInventory(Items.ENDER_PEARL).slot();
                    if (epSlot != -1) {
                        new PearlThread(Module.mc.player, epSlot, Module.mc.player.getInventory().selectedSlot, ModuleManager.middleClick.swapDelay.getValue(), true).start();
                    }
                }
            }
        }),
        Xp(e -> {
            if (ModuleManager.middleClick.feetExp.getValue().booleanValue() && (!ModuleManager.middleClick.antiWaste.getValue().isEnabled() || MiddleClick.needXp())) {
                Module.mc.player.setPitch(90.0f);
            }
            e.addPostAction(() -> {
                int slot;
                if (ModuleManager.middleClick.antiWaste.getValue().isEnabled() && !MiddleClick.needXp()) {
                    return;
                }
                if (Module.mc.options.pickItemKey.isPressed() && (slot = InventoryUtility.findItemInHotBar(Items.EXPERIENCE_BOTTLE).slot()) != -1) {
                    int lastSlot = Module.mc.player.getInventory().selectedSlot;
                    InventoryUtility.switchTo(slot);
                    InteractionUtility.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Module.mc.player.getYaw(), Module.mc.player.getPitch()));
                    if (ModuleManager.middleClick.silent.getValue().booleanValue()) {
                        InventoryUtility.switchTo(lastSlot);
                    }
                }
            });
        }),
        None(e -> {});

        private final Consumer<EventSync> r;

        private Action(Consumer<EventSync> r) {
            this.r = r;
        }

        public void doAction(EventSync e) {
            this.r.accept(e);
        }
    }

    public static class PearlThread
    extends Thread {
        public ClientPlayerEntity player;
        int epSlot;
        int originalSlot;
        int delay;
        boolean inv;

        public PearlThread(ClientPlayerEntity entityPlayer, int epSlot, int originalSlot, int delay, boolean inventory) {
            this.player = entityPlayer;
            this.epSlot = epSlot;
            this.originalSlot = originalSlot;
            this.delay = delay;
            this.inv = inventory;
        }

        @Override
        public void run() {
            if (!this.inv) {
                InventoryUtility.switchTo(this.epSlot);
                AsyncManager.sleep(this.delay);
                InteractionUtility.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Module.mc.player.getYaw(), Module.mc.player.getPitch()));
                Module.mc.player.networkHandler.sendPacket((Packet)new HandSwingC2SPacket(Hand.MAIN_HAND));
                AsyncManager.sleep(this.delay);
                InventoryUtility.switchTo(this.originalSlot);
            } else {
                Module.mc.interactionManager.clickSlot(Module.mc.player.currentScreenHandler.syncId, this.epSlot, this.originalSlot, SlotActionType.SWAP, (PlayerEntity)Module.mc.player);
                AsyncManager.sleep(this.delay);
                if (ModuleManager.aura.isEnabled() && Aura.target != null) {
                    Module.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.LookAndOnGround(Module.mc.player.getYaw(), Module.mc.player.getPitch(), Module.mc.player.isOnGround()));
                }
                InteractionUtility.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Module.mc.player.getYaw(), Module.mc.player.getPitch()));
                Module.mc.player.networkHandler.sendPacket((Packet)new HandSwingC2SPacket(Hand.MAIN_HAND));
                AsyncManager.sleep(this.delay);
                Module.mc.interactionManager.clickSlot(Module.mc.player.currentScreenHandler.syncId, this.epSlot, this.originalSlot, SlotActionType.SWAP, (PlayerEntity)Module.mc.player);
            }
            super.run();
        }
    }
}

