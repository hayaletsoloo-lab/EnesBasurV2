/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.Hand
 */
package thunder.hack.features.modules.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;

public class ElytraHelper
extends Module {
    private static ElytraHelper INSTANCE = new ElytraHelper();
    private final Setting<Bind> switchButton = new Setting<Bind>("SwitchButton", new Bind(-1, false, false));
    private final Setting<Bind> fireWorkButton = new Setting<Bind>("FireWorkButton", new Bind(-1, false, false));
    private final Setting<FireWorkMode> fireWorkMode = new Setting<FireWorkMode>("FireWorkMode", FireWorkMode.Normal);
    private final Setting<Boolean> autoFirework = new Setting<Boolean>("AutoFirework", false);
    private final Setting<Bind> legitFireworkKey = new Setting<Bind>("Legit Key", new Bind(-1, false, false), v -> this.fireWorkMode.getValue() == FireWorkMode.Legit);
    private final Setting<Boolean> slotReturn = new Setting<Boolean>("SlotReturn", true, v -> this.fireWorkMode.getValue() == FireWorkMode.Normal);
    private final Setting<Integer> delay = new Setting<Integer>("Delay", 100, 1, 1000, v -> this.fireWorkMode.getValue() == FireWorkMode.Normal && this.slotReturn.getValue() != false || this.autoFirework.getValue() != false);
    private final Setting<Boolean> swing = new Setting<Boolean>("Swing", true);
    private final Timer switchTimer = new Timer();
    private final Timer fireworkTimer = new Timer();
    private final Timer returnTimer = new Timer();
    private int returnSlot = -1;
    private int returnInvSlot = -1;
    public static boolean swapping = false;

    public static ElytraHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ElytraHelper();
        }
        return INSTANCE;
    }

    public ElytraHelper() {
        super("ElytraHelper", Module.Category.PLAYER);
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if (ElytraHelper.mc.player == null || ElytraHelper.mc.world == null) {
            return;
        }
        if (ElytraHelper.mc.currentScreen == null) {
            if (this.switchButton.getValue().getKey() != -1 && this.isKeyPressed(this.switchButton.getValue().getKey())) {
                this.swapChest(false);
            }
            if (this.fireWorkButton.getValue().getKey() != -1 && this.isKeyPressed(this.fireWorkButton.getValue().getKey()) && ElytraHelper.mc.player.isFallFlying()) {
                this.useFireWork();
            }
            if (this.legitFireworkKey.getValue().getKey() != -1 && this.isKeyPressed(this.legitFireworkKey.getValue().getKey()) && ElytraHelper.mc.player.isFallFlying()) {
                this.useFireWork();
            }
        }
        if (this.autoFirework.getValue().booleanValue() && ElytraHelper.mc.player.isFallFlying() && ElytraHelper.mc.currentScreen == null) {
            float tps = Managers.SERVER.getTPS();
            float tpsFactor = 20.0f / (tps <= 0.0f ? 20.0f : tps);
            long syncedDelay = (long)((float)this.delay.getValue().intValue() * tpsFactor);
            if (this.fireworkTimer.passedMs(syncedDelay)) {
                this.useFireWork();
                this.fireworkTimer.reset();
            }
        }
        if (this.returnSlot != -1 && this.returnTimer.passedMs(this.delay.getValue().intValue())) {
            if (this.returnInvSlot != -1) {
                ElytraHelper.mc.interactionManager.clickSlot(ElytraHelper.mc.player.currentScreenHandler.syncId, this.returnInvSlot, ElytraHelper.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, (PlayerEntity)ElytraHelper.mc.player);
                this.sendPacket((Packet<?>)new CloseHandledScreenC2SPacket(ElytraHelper.mc.player.currentScreenHandler.syncId));
                this.returnInvSlot = -1;
            }
            this.returnSlot = -1;
        }
    }

    public void useFireWork() {
        SearchInvResult fireWorkResult = InventoryUtility.findItemInInventory(Items.FIREWORK_ROCKET);
        if (!fireWorkResult.found()) {
            this.sendMessage(ClientSettings.isRu() ? "\u0423 \u0442\u0435\u0431\u044f \u043d\u0435\u0442 \u0444\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a\u043e\u0432!" : "You've got no fireworks!");
            return;
        }
        if (this.fireWorkMode.getValue() == FireWorkMode.Legit) {
            int currentSlot = ElytraHelper.mc.player.getInventory().selectedSlot;
            int fireworkSlot = fireWorkResult.slot();
            if (fireworkSlot == currentSlot + 36) {
                ElytraHelper.mc.interactionManager.interactItem((PlayerEntity)ElytraHelper.mc.player, Hand.MAIN_HAND);
                if (this.swing.getValue().booleanValue()) {
                    ElytraHelper.mc.player.swingHand(Hand.MAIN_HAND);
                }
                return;
            }
            ElytraHelper.mc.interactionManager.clickSlot(ElytraHelper.mc.player.currentScreenHandler.syncId, fireworkSlot, currentSlot, SlotActionType.SWAP, (PlayerEntity)ElytraHelper.mc.player);
            InteractionUtility.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, ElytraHelper.mc.player.getYaw(), ElytraHelper.mc.player.getPitch()));
            if (this.swing.getValue().booleanValue()) {
                ElytraHelper.mc.player.swingHand(Hand.MAIN_HAND);
            }
            ElytraHelper.mc.interactionManager.clickSlot(ElytraHelper.mc.player.currentScreenHandler.syncId, fireworkSlot, currentSlot, SlotActionType.SWAP, (PlayerEntity)ElytraHelper.mc.player);
            return;
        }
        SearchInvResult hotbarFireWorkResult = InventoryUtility.findItemInHotBar(Items.FIREWORK_ROCKET);
        if (this.fireWorkMode.getValue() == FireWorkMode.Normal && this.slotReturn.getValue().booleanValue()) {
            this.returnSlot = ElytraHelper.mc.player.getInventory().selectedSlot;
            this.returnInvSlot = hotbarFireWorkResult.found() ? -1 : (fireWorkResult.found() ? fireWorkResult.slot() : -1);
            this.returnTimer.reset();
        }
        InventoryUtility.saveSlot();
        if (hotbarFireWorkResult.found()) {
            hotbarFireWorkResult.switchTo();
        } else {
            ElytraHelper.mc.interactionManager.clickSlot(ElytraHelper.mc.player.currentScreenHandler.syncId, fireWorkResult.slot(), ElytraHelper.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, (PlayerEntity)ElytraHelper.mc.player);
            this.sendPacket((Packet<?>)new CloseHandledScreenC2SPacket(ElytraHelper.mc.player.currentScreenHandler.syncId));
        }
        ElytraHelper.mc.interactionManager.interactItem((PlayerEntity)ElytraHelper.mc.player, Hand.MAIN_HAND);
        if (this.swing.getValue().booleanValue()) {
            ElytraHelper.mc.player.swingHand(Hand.MAIN_HAND);
        }
        if (this.fireWorkMode.getValue() == FireWorkMode.Silent) {
            InventoryUtility.returnSlot();
            if (!hotbarFireWorkResult.found()) {
                ElytraHelper.mc.interactionManager.clickSlot(ElytraHelper.mc.player.currentScreenHandler.syncId, fireWorkResult.slot(), ElytraHelper.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, (PlayerEntity)ElytraHelper.mc.player);
                this.sendPacket((Packet<?>)new CloseHandledScreenC2SPacket(ElytraHelper.mc.player.currentScreenHandler.syncId));
            }
        }
    }

    public static int getChestPlateSlot() {
        Item[] items = new Item[]{Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.LEATHER_CHESTPLATE};
        Item[] var1 = items;
        int var2 = items.length;
        for (int var3 = 0; var3 < var2; ++var3) {
            Item item = var1[var3];
            SearchInvResult slot = InventoryUtility.findItemInInventory(item);
            if (!slot.found()) continue;
            return slot.slot();
        }
        return -1;
    }

    private void swapChest(boolean disable) {
        SearchInvResult result = InventoryUtility.findItemInInventory(Items.ELYTRA);
        if (ElytraHelper.mc.player.getInventory().getStack(38).getItem() == Items.ELYTRA) {
            int slot = ElytraHelper.getChestPlateSlot();
            if (slot == -1) {
                if (disable) {
                    this.disable(ClientSettings.isRu() ? "\u0423 \u0442\u0435\u0431\u044f \u043d\u0435\u0442 \u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a\u0430!" : "You don't have keyCodec chestplate!");
                } else {
                    this.sendMessage(ClientSettings.isRu() ? "\u0423 \u0442\u0435\u0431\u044f \u043d\u0435\u0442 \u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a\u0430!" : "You don't have keyCodec chestplate!");
                }
                return;
            }
            ElytraHelper.clickSlot(slot);
            ElytraHelper.clickSlot(6);
            ElytraHelper.clickSlot(slot);
            this.sendPacket((Packet<?>)new CloseHandledScreenC2SPacket(ElytraHelper.mc.player.currentScreenHandler.syncId));
        } else {
            if (!result.found()) {
                if (disable) {
                    this.disable(ClientSettings.isRu() ? "\u0423 \u0442\u0435\u0431\u044f \u043d\u0435\u0442 \u044d\u043b\u0438\u0442\u0440\u044b!" : "You don't have an elytra!");
                } else {
                    this.sendMessage(ClientSettings.isRu() ? "\u0423 \u0442\u0435\u0431\u044f \u043d\u0435\u0442 \u044d\u043b\u0438\u0442\u0440\u044b!" : "You don't have an elytra!");
                }
                return;
            }
            ElytraHelper.clickSlot(result.slot());
            ElytraHelper.clickSlot(6);
            ElytraHelper.clickSlot(result.slot());
            this.sendPacket((Packet<?>)new CloseHandledScreenC2SPacket(ElytraHelper.mc.player.currentScreenHandler.syncId));
        }
        if (disable) {
            this.disable(ClientSettings.isRu() ? "\u0421\u0432\u0430\u043f\u043d\u0443\u043b! \u041e\u0442\u043a\u043b\u044e\u0447\u0430\u044e.." : "Swapped! Disabling..");
        }
    }

    public String getFireWorkMode() {
        return this.fireWorkMode.getValue().name();
    }

    public boolean isSwingEnabled() {
        return this.swing.getValue();
    }

    public static enum FireWorkMode {
        Silent,
        Normal,
        Legit;

    }
}

