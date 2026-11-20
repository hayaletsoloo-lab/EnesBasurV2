/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  meteordevelopment.orbit.EventHandler
 *  net.minecraft.client.network.OtherClientPlayerEntity
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.Entity$RemovalReason
 *  net.minecraft.entity.player.PlayerEntity
 */
package thunder.hack.features.modules.combat;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import thunder.hack.events.impl.EventSync;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.features.modules.misc.FakePlayer;
import thunder.hack.features.modules.render.NameTags;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

public final class AntiBot
extends Module {
    public static ArrayList<PlayerEntity> bots = new ArrayList();
    public Setting<Boolean> remove = new Setting<Boolean>("Remove", false);
    public Setting<Boolean> onlyAura = new Setting<Boolean>("OnlyAura", true);
    private final Setting<Mode> mode = new Setting<Mode>("Mode", Mode.UUIDCheck);
    public Setting<Integer> checkticks = new Setting<Integer>("checkTicks", 3, 0, 10, v -> this.mode.getValue() == Mode.MotionCheck);
    private final Timer clearTimer = new Timer();
    private int ticks = 0;

    public AntiBot() {
        super("AntiBot", Module.Category.COMBAT);
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (!this.onlyAura.getValue().booleanValue()) {
            AntiBot.mc.world.getPlayers().forEach(this::markAsBot);
        } else {
            Entity class_12972 = Aura.target;
            if (class_12972 instanceof PlayerEntity) {
                PlayerEntity ent = (PlayerEntity)class_12972;
                this.markAsBot(ent);
            }
        }
        if (this.remove.getValue().booleanValue()) {
            bots.forEach(b -> {
                try {
                    AntiBot.mc.world.removeEntity(b.getId(), Entity.RemovalReason.KILLED);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            });
        }
        if (this.clearTimer.passedMs(10000L)) {
            bots.clear();
            this.ticks = 0;
            this.clearTimer.reset();
        }
    }

    private void markAsBot(PlayerEntity ent) {
        if (bots.contains(ent)) {
            return;
        }
        switch (this.mode.getValue().ordinal()) {
            case 0: {
                if (ent.getUuid().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + ent.getName().getString()).getBytes(StandardCharsets.UTF_8))) || !(ent instanceof OtherClientPlayerEntity) || FakePlayer.fakePlayer != null && ent.getId() == FakePlayer.fakePlayer.getId() || ent.getName().getString().contains("-")) break;
                this.addBot(ent);
                break;
            }
            case 1: {
                double diffX = ent.getX() - ent.prevX;
                double diffZ = ent.getZ() - ent.prevZ;
                if (!(diffX * diffX + diffZ * diffZ > 0.5)) break;
                if (this.ticks >= this.checkticks.getValue()) {
                    this.addBot(ent);
                }
                ++this.ticks;
                break;
            }
            case 2: {
                if (NameTags.getEntityPing(ent) > 0) break;
                this.addBot(ent);
            }
        }
    }

    private void addBot(PlayerEntity entity) {
        this.sendMessage(entity.getName().getString() + " is a bot!");
        bots.add(entity);
    }

    @Override
    public String getDisplayInfo() {
        return String.valueOf(bots.size());
    }

    public static enum Mode {
        UUIDCheck,
        MotionCheck,
        ZeroPing;

    }
}

