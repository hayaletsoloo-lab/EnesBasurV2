/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  meteordevelopment.orbit.EventHandler
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.PotionContentsComponent
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.SplashPotionItem
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.util.Hand
 */
package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventAfterRotate;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.utility.Timer;

public final class AutoBuff
extends Module {
    private final Setting<Boolean> strength = new Setting<Boolean>("Strength", true);
    private final Setting<Boolean> speed = new Setting<Boolean>("Speed", true);
    private final Setting<Boolean> fire = new Setting<Boolean>("FireResistance", true);
    private final Setting<BooleanSettingGroup> heal = new Setting<BooleanSettingGroup>("InstantHealing", new BooleanSettingGroup(true));
    private final Setting<Integer> healthH = new Setting<Integer>("Health", 8, 0, 20).addToGroup(this.heal);
    private final Setting<BooleanSettingGroup> regen = new Setting<BooleanSettingGroup>("Regeneration", new BooleanSettingGroup(true));
    private final Setting<TriggerOn> triggerOn = new Setting<TriggerOn>("Trigger", TriggerOn.LackOfRegen).addToGroup(this.regen);
    private final Setting<Integer> healthR = new Setting<Integer>("HP", 8, 0, 20, v -> this.triggerOn.is(TriggerOn.Health)).addToGroup(this.regen);
    private final Setting<Boolean> onDaGround = new Setting<Boolean>("OnlyOnGround", true);
    private final Setting<Boolean> pauseAura = new Setting<Boolean>("PauseAura", false);
    public Timer timer = new Timer();
    private boolean spoofed = false;

    public AutoBuff() {
        super("AutoBuff", Module.Category.COMBAT);
    }

    public static int getPotionSlot(Potions potion) {
        for (int i = 0; i < 9; ++i) {
            if (!AutoBuff.isStackPotion(AutoBuff.mc.player.getInventory().getStack(i), potion)) continue;
            return i;
        }
        return -1;
    }

    public static boolean isPotionOnHotBar(Potions potions) {
        return AutoBuff.getPotionSlot(potions) != -1;
    }

    public static boolean isStackPotion(ItemStack stack, Potions potion) {
        if (stack == null) {
            return false;
        }
        if (stack.getItem() instanceof SplashPotionItem) {
            PotionContentsComponent potionContentsComponent = (PotionContentsComponent)stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
            RegistryEntry id = null;
            switch (potion.ordinal()) {
                case 0: {
                    id = StatusEffects.STRENGTH;
                    break;
                }
                case 1: {
                    id = StatusEffects.SPEED;
                    break;
                }
                case 2: {
                    id = StatusEffects.FIRE_RESISTANCE;
                    break;
                }
                case 3: {
                    id = StatusEffects.INSTANT_HEALTH;
                    break;
                }
                case 4: {
                    id = StatusEffects.REGENERATION;
                }
            }
            for (StatusEffectInstance effect : potionContentsComponent.getEffects()) {
                if (effect.getEffectType() != id) continue;
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPostRotationSet(EventAfterRotate event) {
        if (Aura.target != null && AutoBuff.mc.player.getAttackCooldownProgress(1.0f) > 0.5f) {
            return;
        }
        if (AutoBuff.mc.player.age > 80 && this.shouldThrow()) {
            AutoBuff.mc.player.setPitch(90.0f);
            this.spoofed = true;
        }
    }

    private boolean shouldThrow() {
        return !AutoBuff.mc.player.hasStatusEffect(StatusEffects.SPEED) && AutoBuff.isPotionOnHotBar(Potions.SPEED) && this.speed.getValue() != false || !AutoBuff.mc.player.hasStatusEffect(StatusEffects.STRENGTH) && AutoBuff.isPotionOnHotBar(Potions.STRENGTH) && this.strength.getValue() != false || !AutoBuff.mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) && AutoBuff.isPotionOnHotBar(Potions.FIRERES) && this.fire.getValue() != false || AutoBuff.mc.player.getHealth() + AutoBuff.mc.player.getAbsorptionAmount() < (float)this.healthH.getValue().intValue() && AutoBuff.isPotionOnHotBar(Potions.HEAL) && this.heal.getValue().isEnabled() || !AutoBuff.mc.player.hasStatusEffect(StatusEffects.REGENERATION) && this.triggerOn.is(TriggerOn.LackOfRegen) && AutoBuff.isPotionOnHotBar(Potions.REGEN) && this.regen.getValue().isEnabled() || AutoBuff.mc.player.getHealth() + AutoBuff.mc.player.getAbsorptionAmount() < (float)this.healthR.getValue().intValue() && this.triggerOn.is(TriggerOn.Health) && AutoBuff.isPotionOnHotBar(Potions.REGEN) && this.regen.getValue().isEnabled();
    }

    @EventHandler
    public void onPostSync(EventPostSync e) {
        if (Aura.target != null && AutoBuff.mc.player.getAttackCooldownProgress(1.0f) > 0.5f) {
            return;
        }
        if (this.onDaGround.getValue().booleanValue() && !AutoBuff.mc.player.isOnGround()) {
            return;
        }
        if (AutoBuff.mc.player.age > 80 && this.shouldThrow() && this.timer.passedMs(1000L) && this.spoofed) {
            if (!AutoBuff.mc.player.hasStatusEffect(StatusEffects.SPEED) && AutoBuff.isPotionOnHotBar(Potions.SPEED) && this.speed.getValue().booleanValue()) {
                this.throwPotion(Potions.SPEED);
            }
            if (!AutoBuff.mc.player.hasStatusEffect(StatusEffects.STRENGTH) && AutoBuff.isPotionOnHotBar(Potions.STRENGTH) && this.strength.getValue().booleanValue()) {
                this.throwPotion(Potions.STRENGTH);
            }
            if (!AutoBuff.mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) && AutoBuff.isPotionOnHotBar(Potions.FIRERES) && this.fire.getValue().booleanValue()) {
                this.throwPotion(Potions.FIRERES);
            }
            if (AutoBuff.mc.player.getHealth() + AutoBuff.mc.player.getAbsorptionAmount() < (float)this.healthH.getValue().intValue() && this.heal.getValue().isEnabled() && AutoBuff.isPotionOnHotBar(Potions.HEAL)) {
                this.throwPotion(Potions.HEAL);
            }
            if ((!AutoBuff.mc.player.hasStatusEffect(StatusEffects.REGENERATION) && this.triggerOn.is(TriggerOn.LackOfRegen) || AutoBuff.mc.player.getHealth() + AutoBuff.mc.player.getAbsorptionAmount() < (float)this.healthR.getValue().intValue() && this.triggerOn.is(TriggerOn.Health)) && AutoBuff.isPotionOnHotBar(Potions.REGEN) && this.regen.getValue().isEnabled()) {
                this.throwPotion(Potions.REGEN);
            }
            this.sendPacket((Packet<?>)new UpdateSelectedSlotC2SPacket(AutoBuff.mc.player.getInventory().selectedSlot));
            this.timer.reset();
            this.spoofed = false;
        }
    }

    public void throwPotion(Potions potion) {
        if (this.pauseAura.getValue().booleanValue()) {
            ModuleManager.aura.pause();
        }
        this.sendPacket((Packet<?>)new UpdateSelectedSlotC2SPacket(AutoBuff.getPotionSlot(potion)));
        this.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, AutoBuff.mc.player.getYaw(), AutoBuff.mc.player.getPitch()));
    }

    public static enum TriggerOn {
        LackOfRegen,
        Health;

    }

    public static enum Potions {
        STRENGTH,
        SPEED,
        FIRERES,
        HEAL,
        REGEN;

    }
}

