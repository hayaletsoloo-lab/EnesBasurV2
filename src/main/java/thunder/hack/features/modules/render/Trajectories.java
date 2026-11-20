/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.projectile.ArrowEntity
 *  net.minecraft.item.BowItem
 *  net.minecraft.item.CrossbowItem
 *  net.minecraft.item.EggItem
 *  net.minecraft.item.EnderPearlItem
 *  net.minecraft.item.ExperienceBottleItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.LingeringPotionItem
 *  net.minecraft.item.SnowballItem
 *  net.minecraft.item.SplashPotionItem
 *  net.minecraft.item.TridentItem
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 */
package thunder.hack.features.modules.render;

import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.TridentItem;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

public class Trajectories
extends Module {
    private final Setting<Mode> mode = new Setting<Mode>("ColorMode", Mode.Sync);
    private final Setting<ColorSetting> color = new Setting<ColorSetting>("Color", new ColorSetting(575714484), v -> this.mode.getValue() == Mode.Custom);
    private final Setting<Mode> lmode = new Setting<Mode>("LandedColorMode", Mode.Sync);
    private final Setting<ColorSetting> lcolor = new Setting<ColorSetting>("LandedColor", new ColorSetting(575714484), v -> this.lmode.getValue() == Mode.Custom);

    public Trajectories() {
        super("Trajectories", Module.Category.RENDER);
    }

    private boolean isThrowable(Item item) {
        return item instanceof EnderPearlItem || item instanceof TridentItem || item instanceof ExperienceBottleItem || item instanceof SnowballItem || item instanceof EggItem || item instanceof SplashPotionItem || item instanceof LingeringPotionItem;
    }

    private float getDistance(Item item) {
        return item instanceof BowItem ? 1.0f : 0.4f;
    }

    private float getThrowVelocity(Item item) {
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) {
            return 0.5f;
        }
        if (item instanceof ExperienceBottleItem) {
            return 0.59f;
        }
        if (item instanceof TridentItem) {
            return 2.0f;
        }
        return 1.5f;
    }

    private int getThrowPitch(Item item) {
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem || item instanceof ExperienceBottleItem) {
            return 20;
        }
        return 0;
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        Hand hand;
        if (Trajectories.mc.options.hudHidden) {
            return;
        }
        if (Trajectories.mc.player == null || Trajectories.mc.world == null || !Trajectories.mc.options.getPerspective().isFirstPerson()) {
            return;
        }
        ItemStack mainHand = Trajectories.mc.player.getMainHandStack();
        ItemStack offHand = Trajectories.mc.player.getOffHandStack();
        if (mainHand.getItem() instanceof BowItem || mainHand.getItem() instanceof CrossbowItem || this.isThrowable(mainHand.getItem())) {
            hand = Hand.MAIN_HAND;
        } else if (offHand.getItem() instanceof BowItem || offHand.getItem() instanceof CrossbowItem || this.isThrowable(offHand.getItem())) {
            hand = Hand.OFF_HAND;
        } else {
            return;
        }
        boolean prev_bob = (Boolean)Trajectories.mc.options.getBobView().getValue();
        Trajectories.mc.options.getBobView().setValue(false);
        if (offHand.getItem() instanceof CrossbowItem && EnchantmentHelper.getLevel((RegistryEntry)((RegistryEntry)Trajectories.mc.world.getRegistryManager().get(Enchantments.MULTISHOT.getRegistryRef()).getEntry(Enchantments.MULTISHOT).get()), (ItemStack)offHand) != 0 || mainHand.getItem() instanceof CrossbowItem && EnchantmentHelper.getLevel((RegistryEntry)((RegistryEntry)Trajectories.mc.world.getRegistryManager().get(Enchantments.MULTISHOT.getRegistryRef()).getEntry(Enchantments.MULTISHOT).get()), (ItemStack)mainHand) != 0) {
            this.calcTrajectory(hand == Hand.OFF_HAND ? offHand.getItem() : mainHand.getItem(), Trajectories.mc.player.getYaw() - 10.0f);
            this.calcTrajectory(hand == Hand.OFF_HAND ? offHand.getItem() : mainHand.getItem(), Trajectories.mc.player.getYaw());
            this.calcTrajectory(hand == Hand.OFF_HAND ? offHand.getItem() : mainHand.getItem(), Trajectories.mc.player.getYaw() + 10.0f);
        } else {
            this.calcTrajectory(hand == Hand.OFF_HAND ? offHand.getItem() : mainHand.getItem(), Trajectories.mc.player.getYaw());
        }
        Trajectories.mc.options.getBobView().setValue(prev_bob);
    }

    private void calcTrajectory(Item item, float yaw) {
        double x = Render2DEngine.interpolate(Trajectories.mc.player.prevX, Trajectories.mc.player.getX(), Render3DEngine.getTickDelta());
        double y = Render2DEngine.interpolate(Trajectories.mc.player.prevY, Trajectories.mc.player.getY(), Render3DEngine.getTickDelta());
        double z = Render2DEngine.interpolate(Trajectories.mc.player.prevZ, Trajectories.mc.player.getZ(), Render3DEngine.getTickDelta());
        y = y + (double)Trajectories.mc.player.getEyeHeight(Trajectories.mc.player.getPose()) - 0.1000000014901161;
        if (item == Trajectories.mc.player.getMainHandStack().getItem()) {
            x -= (double)(MathHelper.cos((float)(yaw / 180.0f * (float)Math.PI)) * 0.16f);
            z -= (double)(MathHelper.sin((float)(yaw / 180.0f * (float)Math.PI)) * 0.16f);
        } else {
            x += (double)(MathHelper.cos((float)(yaw / 180.0f * (float)Math.PI)) * 0.16f);
            z += (double)(MathHelper.sin((float)(yaw / 180.0f * (float)Math.PI)) * 0.16f);
        }
        float maxDist = this.getDistance(item);
        double motionX = -MathHelper.sin((float)(yaw / 180.0f * (float)Math.PI)) * MathHelper.cos((float)(Trajectories.mc.player.getPitch() / 180.0f * (float)Math.PI)) * maxDist;
        double motionY = -MathHelper.sin((float)((Trajectories.mc.player.getPitch() - (float)this.getThrowPitch(item)) / 180.0f * 3.141593f)) * maxDist;
        double motionZ = MathHelper.cos((float)(yaw / 180.0f * (float)Math.PI)) * MathHelper.cos((float)(Trajectories.mc.player.getPitch() / 180.0f * (float)Math.PI)) * maxDist;
        float power = (float)Trajectories.mc.player.getItemUseTime() / 20.0f;
        power = (power * power + power * 2.0f) / 3.0f;
        if (power > 1.0f || power == 0.0f) {
            power = 1.0f;
        }
        float distance = MathHelper.sqrt((float)((float)(motionX * motionX + motionY * motionY + motionZ * motionZ)));
        motionX /= (double)distance;
        motionY /= (double)distance;
        motionZ /= (double)distance;
        float pow = (item instanceof BowItem ? power * 2.0f : (item instanceof CrossbowItem ? 2.2f : 1.0f)) * this.getThrowVelocity(item);
        motionX *= (double)pow;
        motionY *= (double)pow;
        motionZ *= (double)pow;
        if (!Trajectories.mc.player.isOnGround()) {
            motionY += Trajectories.mc.player.getVelocity().getY();
        }
        for (int i = 0; i < 300; ++i) {
            BlockHitResult bhr;
            Vec3d lastPos = new Vec3d(x, y, z);
            if (Trajectories.mc.world.getBlockState(new BlockPos((int)(x += motionX), (int)(y += motionY), (int)(z += motionZ))).getBlock() == Blocks.WATER) {
                motionX *= 0.8;
                motionY *= 0.8;
                motionZ *= 0.8;
            } else {
                motionX *= 0.99;
                motionY *= 0.99;
                motionZ *= 0.99;
            }
            motionY = item instanceof BowItem ? (motionY = motionY - (double)0.05f) : (Trajectories.mc.player.getMainHandStack().getItem() instanceof CrossbowItem ? (motionY = motionY - (double)0.05f) : (motionY = motionY - (double)0.03f));
            Vec3d pos = new Vec3d(x, y, z);
            for (Entity ent : Trajectories.mc.world.getEntities()) {
                if (ent instanceof ArrowEntity || ent.equals(Trajectories.mc.player) || !ent.getBoundingBox().intersects(new Box(x - 0.3, y - 0.3, z - 0.3, x + 0.3, y + 0.3, z + 0.3))) continue;
                Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(ent.getBoundingBox(), this.lmode.getValue() == Mode.Sync ? HudEditor.getColor(i * 10) : this.lcolor.getValue().getColorObject(), 2.0f));
                Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(ent.getBoundingBox(), this.lmode.getValue() == Mode.Sync ? Render2DEngine.injectAlpha(HudEditor.getColor(i * 10), 100) : this.lcolor.getValue().getColorObject()));
                break;
            }
            if ((bhr = Trajectories.mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)Trajectories.mc.player))) != null && bhr.getType() == HitResult.Type.BLOCK) {
                Render3DEngine.OUTLINE_SIDE_QUEUE.add(new Render3DEngine.OutlineSideAction(new Box(bhr.getBlockPos()), this.lmode.getValue() == Mode.Sync ? HudEditor.getColor(i * 10) : this.lcolor.getValue().getColorObject(), 2.0f, bhr.getSide()));
                Render3DEngine.FILLED_SIDE_QUEUE.add(new Render3DEngine.FillSideAction(new Box(bhr.getBlockPos()), this.lmode.getValue() == Mode.Sync ? Render2DEngine.injectAlpha(HudEditor.getColor(i * 10), 100) : this.lcolor.getValue().getColorObject(), bhr.getSide()));
                break;
            }
            if (y <= -65.0) break;
            if (motionX == 0.0 && motionY == 0.0 && motionZ == 0.0) continue;
            Render3DEngine.drawLine(lastPos, pos, this.mode.getValue() == Mode.Sync ? HudEditor.getColor(i) : this.color.getValue().getColorObject());
        }
    }

    private static enum Mode {
        Custom,
        Sync;

    }
}

