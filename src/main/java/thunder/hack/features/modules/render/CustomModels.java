/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.RenderLayer
 *  net.minecraft.client.render.VertexConsumer
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.entity.model.EntityModelLayers
 *  net.minecraft.client.render.entity.model.RabbitEntityModel
 *  net.minecraft.client.render.entity.model.VexEntityModel
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.mob.VexEntity
 *  net.minecraft.entity.passive.RabbitEntity
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.world.World
 */
package thunder.hack.features.modules.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.RabbitEntityModel;
import net.minecraft.client.render.entity.model.VexEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class CustomModels
extends Module {
    public final Setting<Mode> mode = new Setting<Mode>("Mode", Mode.Rabbit);
    public final Setting<Float> scale = new Setting<Float>("Scale", Float.valueOf(1.5f), Float.valueOf(0.1f), Float.valueOf(5.0f));
    public RabbitEntityModel<RabbitEntity> rabbitModel;
    public VexEntityModel devilModel;
    public RabbitEntity dummyRabbit;
    public VexEntity dummyDevil;
    private static final Identifier RABBIT_TEXTURE = Identifier.of((String)"textures/entity/rabbit/white.png");
    private static final Identifier DEVIL_TEXTURE = Identifier.of((String)"textures/entity/illager/vex.png");
    private static final Identifier BLACK_RABBIT_TEXTURE = Identifier.of((String)"textures/entity/rabbit/black.png");

    public CustomModels() {
        super("CustomModels", Module.Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (this.rabbitModel == null) {
            this.rabbitModel = new RabbitEntityModel(mc.getEntityModelLoader().getModelPart(EntityModelLayers.RABBIT));
            this.devilModel = new VexEntityModel(mc.getEntityModelLoader().getModelPart(EntityModelLayers.VEX));
        }
    }

    public boolean onRenderModel(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, LivingEntity entity, float tickDelta) {
        if (!this.isEnabled() || entity != CustomModels.mc.player) {
            return false;
        }
        if (this.rabbitModel == null || this.devilModel == null) {
            this.rabbitModel = new RabbitEntityModel(mc.getEntityModelLoader().getModelPart(EntityModelLayers.RABBIT));
            this.devilModel = new VexEntityModel(mc.getEntityModelLoader().getModelPart(EntityModelLayers.VEX));
        }
        if ((this.dummyRabbit == null || this.dummyDevil == null) && CustomModels.mc.world != null) {
            this.dummyRabbit = new RabbitEntity(EntityType.RABBIT, (World)CustomModels.mc.world);
            this.dummyDevil = new VexEntity(EntityType.VEX, (World)CustomModels.mc.world);
        }
        float h = MathHelper.lerpAngleDegrees((float)tickDelta, (float)entity.prevBodyYaw, (float)entity.bodyYaw);
        float j = MathHelper.lerpAngleDegrees((float)tickDelta, (float)entity.prevHeadYaw, (float)entity.headYaw);
        float headYaw = j - h;
        float headPitch = MathHelper.lerp((float)tickDelta, (float)entity.prevPitch, (float)entity.getPitch());
        float s = this.scale.getValue().floatValue();
        matrices.push();
        matrices.scale(s, s, s);
        if (this.mode.getValue() == Mode.Rabbit || this.mode.getValue() == Mode.BlackRabbit) {
            Identifier tex = this.mode.getValue() == Mode.Rabbit ? RABBIT_TEXTURE : BLACK_RABBIT_TEXTURE;
            VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull((Identifier)tex));
            if (this.dummyRabbit != null) {
                this.rabbitModel.setAngles(this.dummyRabbit, entity.limbAnimator.getPos(tickDelta), entity.limbAnimator.getSpeed(tickDelta), (float)entity.age + tickDelta, headYaw, headPitch);
            }
            this.rabbitModel.render(matrices, buffer, light, overlay, -1);
        } else if (this.mode.getValue() == Mode.Devil) {
            VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent((Identifier)DEVIL_TEXTURE));
            if (this.dummyDevil != null) {
                this.devilModel.setAngles(this.dummyDevil, entity.limbAnimator.getPos(tickDelta), entity.limbAnimator.getSpeed(tickDelta), (float)entity.age + tickDelta, headYaw, headPitch);
            }
            this.devilModel.render(matrices, buffer, light, overlay, -65536);
        }
        matrices.pop();
        return true;
    }

    public static enum Mode {
        Rabbit,
        Devil,
        BlackRabbit;

    }
}

