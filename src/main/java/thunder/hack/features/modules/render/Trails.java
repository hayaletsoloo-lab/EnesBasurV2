/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DstFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SrcFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.client.render.Tessellator
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.entity.projectile.ArrowEntity
 *  net.minecraft.entity.projectile.thrown.EnderPearlEntity
 *  net.minecraft.entity.projectile.thrown.ExperienceBottleEntity
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.RotationAxis
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 *  org.joml.Matrix4f
 */
package thunder.hack.features.modules.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.features.modules.render.HitParticles;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.interfaces.IEntity;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;

public class Trails
extends Module {
    private final Setting<Boolean> xp = new Setting<Boolean>("Xp", false);
    private final Setting<Particles> pearls = new Setting<Particles>("Pearls", Particles.Particles);
    private final Setting<Particles> arrows = new Setting<Particles>("Arrows", Particles.Particles);
    private final Setting<Players> players = new Setting<Players>("Players", Players.Particles);
    private final Setting<Boolean> onlySelf = new Setting<Boolean>("OnlySelf", false, v -> this.players.getValue() != Players.None);
    private final Setting<ColorSetting> color = new Setting<ColorSetting>("Color", new ColorSetting(-2013200640));
    private final Setting<Float> down = new Setting<Float>("Down", Float.valueOf(0.5f), Float.valueOf(0.0f), Float.valueOf(2.0f));
    private final Setting<Float> width = new Setting<Float>("Height", Float.valueOf(1.3f), Float.valueOf(0.1f), Float.valueOf(2.0f));
    private final Setting<Integer> speed = new Setting<Integer>("Speed", 2, 1, 20, v -> this.players.is(Players.Particles) || this.arrows.is(Particles.Particles) || this.pearls.is(Particles.Particles));
    private final Setting<HitParticles.Mode> mode = new Setting<HitParticles.Mode>("Mode", HitParticles.Mode.Stars);
    private final Setting<HitParticles.Physics> physics = new Setting<HitParticles.Physics>("Physics", HitParticles.Physics.Fall, v -> this.players.is(Players.Particles) || this.arrows.is(Particles.Particles) || this.pearls.is(Particles.Particles));
    private final Setting<Integer> starsScale = new Setting<Integer>("Scale", 3, 1, 10, v -> this.players.is(Players.Particles) || this.arrows.is(Particles.Particles) || this.pearls.is(Particles.Particles));
    private final Setting<Integer> amount = new Setting<Integer>("Amount", 2, 1, 5, v -> this.players.is(Players.Particles) || this.arrows.is(Particles.Particles) || this.pearls.is(Particles.Particles));
    private final Setting<Integer> lifeTime = new Setting<Integer>("LifeTime", 2, 1, 10, v -> this.players.is(Players.Particles) || this.arrows.is(Particles.Particles) || this.pearls.is(Particles.Particles));
    private final Setting<Mode> lmode = new Setting<Mode>("ColorMode", Mode.Sync);
    private final Setting<ColorSetting> lcolor = new Setting<ColorSetting>("Color2", new ColorSetting(575714484), v -> this.lmode.getValue() == Mode.Custom);
    private List<Particle> particles = new ArrayList<Particle>();

    public Trails() {
        super("Trails", Module.Category.RENDER);
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        for (Entity en : Managers.ASYNC.getAsyncEntities()) {
            if (en instanceof EnderPearlEntity && this.pearls.is(Particles.Trail)) {
                this.calcTrajectory(en);
            }
            if (en instanceof ArrowEntity && this.arrows.is(Particles.Trail)) {
                this.calcTrajectory(en);
            }
            if (!(en instanceof ExperienceBottleEntity) || !this.xp.getValue().booleanValue()) continue;
            this.calcTrajectory(en);
        }
        if (this.players.getValue() == Players.Trail) {
            for (PlayerEntity entity : Trails.mc.world.getPlayers()) {
                if (entity != Trails.mc.player && this.onlySelf.getValue().booleanValue()) continue;
                float alpha = (float)this.color.getValue().getAlpha() / 255.0f;
                if (((IEntity)entity).getTrails().isEmpty()) continue;
                stack.push();
                RenderSystem.disableCull();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                RenderSystem.depthFunc((int)515);
                RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                for (int i = 0; i < ((IEntity)entity).getTrails().size(); ++i) {
                    Trail ctx = ((IEntity)entity).getTrails().get(i);
                    Vec3d pos = ctx.interpolate(Render3DEngine.getTickDelta());
                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float)pos.x, (float)pos.y + this.down.getValue().floatValue(), (float)pos.z).color(Render2DEngine.injectAlpha(((IEntity)entity).getTrails().get(i).color(), (int)((double)alpha * ctx.animation(Render3DEngine.getTickDelta()) * 255.0)).getRGB());
                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float)pos.x, (float)pos.y + this.width.getValue().floatValue() + this.down.getValue().floatValue(), (float)pos.z).color(Render2DEngine.injectAlpha(((IEntity)entity).getTrails().get(i).color(), (int)((double)alpha * ctx.animation(Render3DEngine.getTickDelta()) * 255.0)).getRGB());
                }
                Render2DEngine.endBuilding(bufferBuilder);
                Render3DEngine.endRender();
                RenderSystem.enableCull();
                RenderSystem.disableDepthTest();
                stack.pop();
            }
        } else if (this.players.getValue() == Players.Tail) {
            for (PlayerEntity entity : Trails.mc.world.getPlayers()) {
                if (entity != Trails.mc.player && this.onlySelf.getValue().booleanValue()) continue;
                float alpha = this.color.getValue().getAlpha();
                Camera camera = Trails.mc.gameRenderer.getCamera();
                stack.push();
                RenderSystem.setShaderTexture((int)0, (Identifier)TextureStorage.firefly);
                RenderSystem.enableBlend();
                RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask((boolean)false);
                RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
                BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                int size = ((IEntity)entity).getTrails().size();
                if (!((IEntity)entity).getTrails().isEmpty()) {
                    for (int i = 0; i < size; ++i) {
                        Trail ctx = ((IEntity)entity).getTrails().get(i);
                        MatrixStack matrices = new MatrixStack();
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
                        Vec3d pos = ctx.interpolate(Render3DEngine.getTickDelta());
                        matrices.translate(pos.x, pos.y + (double)0.9f, pos.z);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                        Matrix4f matrix = matrices.peek().getPositionMatrix();
                        Color col = ctx.color();
                        float sc = 0.6f;
                        float colorFactor = (float)i / (float)size;
                        col = Render2DEngine.interpolateColorC(col, Color.WHITE, (float)Math.pow(1.0f - colorFactor, 2.0));
                        float animPow = (float)ctx.animation(Render3DEngine.getTickDelta());
                        int animatedAlpha = (int)(alpha * animPow);
                        bufferBuilder.vertex(matrix, -sc, sc, 0.0f).texture(0.0f, 1.0f).color(Render2DEngine.injectAlpha(col, animatedAlpha).getRGB());
                        bufferBuilder.vertex(matrix, sc, sc, 0.0f).texture(1.0f, 1.0f).color(Render2DEngine.injectAlpha(col, animatedAlpha).getRGB());
                        bufferBuilder.vertex(matrix, sc, -sc, 0.0f).texture(1.0f, 0.0f).color(Render2DEngine.injectAlpha(col, animatedAlpha).getRGB());
                        bufferBuilder.vertex(matrix, -sc, -sc, 0.0f).texture(0.0f, 0.0f).color(Render2DEngine.injectAlpha(col, animatedAlpha).getRGB());
                    }
                }
                Render2DEngine.endBuilding(bufferBuilder);
                RenderSystem.depthMask((boolean)true);
                RenderSystem.disableDepthTest();
                RenderSystem.disableBlend();
                stack.pop();
            }
        } else if (this.players.getValue() == Players.Cute) {
            for (PlayerEntity entity : Trails.mc.world.getPlayers()) {
                Vec3d pos;
                Trail ctx;
                int i;
                if (entity != Trails.mc.player && this.onlySelf.getValue().booleanValue()) continue;
                float alpha = (float)this.color.getValue().getAlpha() / 255.0f;
                if (((IEntity)entity).getTrails().isEmpty()) continue;
                stack.push();
                RenderSystem.disableCull();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                RenderSystem.depthFunc((int)515);
                float step = (float)(Trails.mc.player.getBoundingBox().getLengthY() / 5.0);
                RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                for (i = 0; i < ((IEntity)entity).getTrails().size(); ++i) {
                    ctx = ((IEntity)entity).getTrails().get(i);
                    pos = ctx.interpolate(Render3DEngine.getTickDelta());
                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float)pos.x, (float)pos.y, (float)pos.z).color(Render2DEngine.injectAlpha(new Color(69, 221, 255), (int)((double)alpha * ctx.animation(Render3DEngine.getTickDelta()) * 255.0)).getRGB());
                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float)pos.x, (float)pos.y + step, (float)pos.z).color(Render2DEngine.injectAlpha(new Color(69, 221, 255), (int)((double)alpha * ctx.animation(Render3DEngine.getTickDelta()) * 255.0)).getRGB());
                }
                Render2DEngine.endBuilding(bufferBuilder);
                RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                for (i = 0; i < ((IEntity)entity).getTrails().size(); ++i) {
                    ctx = ((IEntity)entity).getTrails().get(i);
                    pos = ctx.interpolate(Render3DEngine.getTickDelta());
                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float)pos.x, (float)pos.y + step, (float)pos.z).color(Render2DEngine.injectAlpha(new Color(248, 139, 160), (int)((double)alpha * ctx.animation(Render3DEngine.getTickDelta()) * 255.0)).getRGB());
                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float)pos.x, (float)pos.y + step * 2.0f, (float)pos.z).color(Render2DEngine.injectAlpha(new Color(248, 139, 160), (int)((double)alpha * ctx.animation(Render3DEngine.getTickDelta()) * 255.0)).getRGB());
                }
                Render2DEngine.endBuilding(bufferBuilder);
                RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                for (i = 0; i < ((IEntity)entity).getTrails().size(); ++i) {
                    ctx = ((IEntity)entity).getTrails().get(i);
                    pos = ctx.interpolate(Render3DEngine.getTickDelta());
                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float)pos.x, (float)pos.y + step * 2.0f, (float)pos.z).color(Render2DEngine.injectAlpha(new Color(255, 255, 255), (int)((double)alpha * ctx.animation(Render3DEngine.getTickDelta()) * 255.0)).getRGB());
                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float)pos.x, (float)pos.y + step * 3.0f, (float)pos.z).color(Render2DEngine.injectAlpha(new Color(255, 255, 255), (int)((double)alpha * ctx.animation(Render3DEngine.getTickDelta()) * 255.0)).getRGB());
                }
                Render2DEngine.endBuilding(bufferBuilder);
                RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                for (i = 0; i < ((IEntity)entity).getTrails().size(); ++i) {
                    ctx = ((IEntity)entity).getTrails().get(i);
                    pos = ctx.interpolate(Render3DEngine.getTickDelta());
                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float)pos.x, (float)pos.y + step * 3.0f, (float)pos.z).color(Render2DEngine.injectAlpha(new Color(248, 139, 160), (int)((double)alpha * ctx.animation(Render3DEngine.getTickDelta()) * 255.0)).getRGB());
                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float)pos.x, (float)pos.y + step * 4.0f, (float)pos.z).color(Render2DEngine.injectAlpha(new Color(248, 139, 160), (int)((double)alpha * ctx.animation(Render3DEngine.getTickDelta()) * 255.0)).getRGB());
                }
                Render2DEngine.endBuilding(bufferBuilder);
                RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
                for (i = 0; i < ((IEntity)entity).getTrails().size(); ++i) {
                    ctx = ((IEntity)entity).getTrails().get(i);
                    pos = ctx.interpolate(Render3DEngine.getTickDelta());
                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float)pos.x, (float)pos.y + step * 4.0f, (float)pos.z).color(Render2DEngine.injectAlpha(new Color(69, 221, 255), (int)((double)alpha * ctx.animation(Render3DEngine.getTickDelta()) * 255.0)).getRGB());
                    bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float)pos.x, (float)pos.y + step * 5.0f, (float)pos.z).color(Render2DEngine.injectAlpha(new Color(69, 221, 255), (int)((double)alpha * ctx.animation(Render3DEngine.getTickDelta()) * 255.0)).getRGB());
                }
                Render2DEngine.endBuilding(bufferBuilder);
                Render3DEngine.endRender();
                RenderSystem.enableCull();
                RenderSystem.disableDepthTest();
                stack.pop();
            }
        }
        if (!this.particles.isEmpty()) {
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask((boolean)false);
            RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
            switch (this.mode.getValue()) {
                case Stars: {
                    RenderSystem.setShaderTexture((int)0, (Identifier)TextureStorage.star);
                    break;
                }
                case Bloom: {
                    RenderSystem.setShaderTexture((int)0, (Identifier)TextureStorage.firefly);
                    break;
                }
                case Hearts: {
                    RenderSystem.setShaderTexture((int)0, (Identifier)TextureStorage.heart);
                    break;
                }
                default: {
                    return;
                }
            }
            RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            if (Trails.mc.player != null && Trails.mc.world != null) {
                this.particles.forEach(p -> p.render(stack, bufferBuilder));
            }
            Render2DEngine.endBuilding(bufferBuilder);
            RenderSystem.disableBlend();
            RenderSystem.depthMask((boolean)true);
            RenderSystem.disableDepthTest();
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        }
    }

    @Override
    public void onUpdate() {
        Color c = this.lmode.getValue() == Mode.Sync ? HudEditor.getColor(Trails.mc.player.age % 360) : this.lcolor.getValue().getColorObject();
        for (PlayerEntity player : Trails.mc.world.getPlayers()) {
            if (player.getPos().getZ() != player.prevZ || player.getPos().getX() != player.prevX && !this.onlySelf.getValue().booleanValue()) {
                ((IEntity)player).getTrails().add(new Trail(new Vec3d(player.prevX, player.prevY, player.prevZ), player.getPos(), c));
                if (this.players.is(Players.Particles)) {
                    for (int i = 0; i < this.amount.getValue(); ++i) {
                        this.particles.add(new Particle(player.getX(), MathUtility.random((float)(player.getY() + (double)player.getHeight()), (float)player.getY()), player.getZ(), c));
                    }
                }
            }
            ((IEntity)player).getTrails().removeIf(Trail::update);
        }
        for (Entity en : Managers.ASYNC.getAsyncEntities()) {
            if (en instanceof ArrowEntity && ((ArrowEntity)en).prevY != en.getY() && this.arrows.is(Particles.Particles)) {
                for (int i = 0; i < 5; ++i) {
                    this.particles.add(new Particle(en.getX(), en.getY(), en.getZ(), HudEditor.getColor(Trails.mc.player.age)));
                }
            }
            if (!(en instanceof EnderPearlEntity) || !this.pearls.is(Particles.Particles)) continue;
            for (int i = 0; i < 5; ++i) {
                this.particles.add(new Particle(en.getX(), en.getY(), en.getZ(), HudEditor.getColor(Trails.mc.player.age)));
            }
        }
        if (Managers.PLAYER.currentPlayerSpeed != 0.0f) {
            ((IEntity)Trails.mc.player).getTrails().add(new Trail(new Vec3d(Trails.mc.player.prevX, Trails.mc.player.prevY, Trails.mc.player.prevZ), Trails.mc.player.getPos(), c));
            if (this.players.is(Players.Particles)) {
                for (int i = 0; i < this.amount.getValue(); ++i) {
                    this.particles.add(new Particle(Trails.mc.player.getX(), MathUtility.random((float)(Trails.mc.player.getY() + (double)Trails.mc.player.getHeight()), (float)Trails.mc.player.getY()), Trails.mc.player.getZ(), c));
                }
            }
        }
        this.particles.removeIf(particle -> (float)(System.currentTimeMillis() - particle.time) > 1000.0f * (float)this.lifeTime.getValue().intValue());
    }

    private void calcTrajectory(Entity e) {
        double motionX = e.getVelocity().x;
        double motionY = e.getVelocity().y;
        double motionZ = e.getVelocity().z;
        double x = e.getX();
        double y = e.getY();
        double z = e.getZ();
        Vec3d lastPos = new Vec3d(x, y, z);
        for (int i = 0; i < 300; ++i) {
            lastPos = new Vec3d(x, y, z);
            if (Trails.mc.world.getBlockState(new BlockPos((int)(x += motionX), (int)(y += motionY), (int)(z += motionZ))).getBlock() == Blocks.WATER) {
                motionX *= 0.8;
                motionY *= 0.8;
                motionZ *= 0.8;
            } else {
                motionX *= 0.99;
                motionY *= 0.99;
                motionZ *= 0.99;
            }
            motionY = e instanceof ArrowEntity ? (motionY = motionY - (double)0.05f) : (motionY = motionY - (double)0.03f);
            Vec3d pos = new Vec3d(x, y, z);
            if (Trails.mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)Trails.mc.player)) != null && (Trails.mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)Trails.mc.player)).getType() == HitResult.Type.ENTITY || Trails.mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)Trails.mc.player)).getType() == HitResult.Type.BLOCK) || y <= -65.0) break;
            if (e.getVelocity().x == 0.0 && e.getVelocity().y == 0.0 && e.getVelocity().z == 0.0) continue;
            int alpha = (int)MathUtility.clamp(255.0f * ((float)i / 8.0f), 0.0f, 255.0f);
            Render3DEngine.drawLine(lastPos, pos, this.lmode.getValue() == Mode.Sync ? Render2DEngine.injectAlpha(HudEditor.getColor(i * 5), alpha) : Render2DEngine.injectAlpha(this.lcolor.getValue().getColorObject(), alpha));
        }
    }

    private static enum Particles {
        Trail,
        Particles,
        None;

    }

    private static enum Players {
        Trail,
        Particles,
        Cute,
        Tail,
        None;

    }

    private static enum Mode {
        Custom,
        Sync;

    }

    public static class Trail {
        private final Vec3d from;
        private final Vec3d to;
        private final Color color;
        private int ticks;
        private int prevTicks;

        public Trail(Vec3d from, Vec3d to, Color color) {
            this.from = from;
            this.to = to;
            this.ticks = 10;
            this.color = color;
        }

        public Vec3d interpolate(float pt) {
            double x = this.from.x + (this.to.x - this.from.x) * (double)pt - Module.mc.getEntityRenderDispatcher().camera.getPos().getX();
            double y = this.from.y + (this.to.y - this.from.y) * (double)pt - Module.mc.getEntityRenderDispatcher().camera.getPos().getY();
            double z = this.from.z + (this.to.z - this.from.z) * (double)pt - Module.mc.getEntityRenderDispatcher().camera.getPos().getZ();
            return new Vec3d(x, y, z);
        }

        public double animation(float pt) {
            return (double)((float)this.prevTicks + (float)(this.ticks - this.prevTicks) * pt) / 10.0;
        }

        public boolean update() {
            this.prevTicks = this.ticks;
            return this.ticks-- <= 0;
        }

        public Color color() {
            return this.color;
        }
    }

    public class Particle {
        double x;
        double y;
        double z;
        double motionX;
        double motionY;
        double motionZ;
        long time;
        Color color;

        public Particle(double x, double y, double z, Color color) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.motionX = MathUtility.random(-((float)Trails.this.speed.getValue().intValue()) / 200.0f, (float)Trails.this.speed.getValue().intValue() / 200.0f);
            this.motionY = MathUtility.random(-((float)Trails.this.speed.getValue().intValue()) / 200.0f, (float)Trails.this.speed.getValue().intValue() / 200.0f);
            this.motionZ = MathUtility.random(-((float)Trails.this.speed.getValue().intValue()) / 200.0f, (float)Trails.this.speed.getValue().intValue() / 200.0f);
            this.time = System.currentTimeMillis();
            this.color = color;
        }

        public void update() {
            double sp = (float)Trails.this.starsScale.getValue().intValue() / 10.0f;
            this.x += this.motionX;
            this.y += this.motionY;
            this.z += this.motionZ;
            if (this.posBlock(this.x, this.y - (double)((float)Trails.this.starsScale.getValue().intValue() / 10.0f), this.z)) {
                this.motionY = -this.motionY / 1.1;
            } else if (this.posBlock(this.x, this.y, this.z) || this.posBlock(this.x - sp, this.y, this.z - sp) || this.posBlock(this.x + sp, this.y, this.z + sp) || this.posBlock(this.x + sp, this.y, this.z - sp) || this.posBlock(this.x - sp, this.y, this.z + sp) || this.posBlock(this.x + sp, this.y, this.z) || this.posBlock(this.x - sp, this.y, this.z) || this.posBlock(this.x, this.y, this.z - sp) || this.posBlock(this.x, this.y, this.z + sp)) {
                this.motionX = -this.motionX;
                this.motionZ = -this.motionZ;
            }
            if (Trails.this.physics.getValue() == HitParticles.Physics.Fall) {
                this.motionY -= (double)5.0E-4f;
            }
            this.motionX /= 1.005;
            this.motionZ /= 1.005;
            this.motionY /= 1.005;
        }

        public void render(MatrixStack matrixStack, BufferBuilder bufferBuilder) {
            this.update();
            float scale = (float)Trails.this.starsScale.getValue().intValue() / 10.0f;
            double posX = this.x - Module.mc.getEntityRenderDispatcher().camera.getPos().getX();
            double posY = this.y - Module.mc.getEntityRenderDispatcher().camera.getPos().getY();
            double posZ = this.z - Module.mc.getEntityRenderDispatcher().camera.getPos().getZ();
            Camera camera = Module.mc.gameRenderer.getCamera();
            MatrixStack matrices = new MatrixStack();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
            matrices.translate(posX, posY, posZ);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            float colorAnim = (float)(System.currentTimeMillis() - this.time) / (1000.0f * (float)Trails.this.lifeTime.getValue().intValue());
            Color c = Trails.this.lmode.getValue() == Mode.Sync ? HudEditor.getColor((int)(360.0f * colorAnim)) : Trails.this.lcolor.getValue().getColorObject();
            bufferBuilder.vertex(matrix, -scale / 2.0f, scale, 0.0f).texture(0.0f, 1.0f).color(Render2DEngine.applyOpacity(c, 1.0f - colorAnim).getRGB());
            bufferBuilder.vertex(matrix, scale, scale, 0.0f).texture(1.0f, 1.0f).color(Render2DEngine.applyOpacity(c, 1.0f - colorAnim).getRGB());
            bufferBuilder.vertex(matrix, scale, -scale / 2.0f, 0.0f).texture(1.0f, 0.0f).color(Render2DEngine.applyOpacity(c, 1.0f - colorAnim).getRGB());
            bufferBuilder.vertex(matrix, -scale / 2.0f, -scale / 2.0f, 0.0f).texture(0.0f, 0.0f).color(Render2DEngine.applyOpacity(c, 1.0f - colorAnim).getRGB());
        }

        private boolean posBlock(double x, double y, double z) {
            Block b = Module.mc.world.getBlockState(BlockPos.ofFloored((double)x, (double)y, (double)z)).getBlock();
            return b != Blocks.AIR && b != Blocks.WATER && b != Blocks.LAVA;
        }
    }
}

