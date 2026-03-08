/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
// public class EntityRobotEnergyParticle extends EntityFX
public class EntityRobotEnergyParticle extends SmokeParticle {
    private float smokeParticleScale;
    private final SpriteSet spriteSet;

    public EntityRobotEnergyParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
        this(world, x, y, z, vx, vy, vz, 1.0F, spriteSet);
    }

    // public EntityRobotEnergyParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, float size)
    public EntityRobotEnergyParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, float size, SpriteSet spriteSet) {
        super(world, x, y, z, vx, vy, vz, size, spriteSet);
        this.spriteSet = spriteSet;
        // this.motionX *= 0.10000000149011612D;
        this.xd *= 0.10000000149011612D;
        // this.motionY *= 0.10000000149011612D;
        this.yd *= 0.10000000149011612D;
        // this.motionZ *= 0.10000000149011612D;
        this.zd *= 0.10000000149011612D;
        // this.motionX += vx;
        this.xd += vx;
        // this.motionY += vy;
        this.yd += vy;
        // this.motionZ += vz;
        this.zd += vz;
        // this.particleRed = (float) (Math.random() * 0.6);
        this.rCol = (float) (Math.random() * 0.6);
        // this.particleGreen = 0;
        this.gCol = 0;
        // this.particleBlue = 0;
        this.bCol = 0;
        // this.particleScale *= 0.75F;
        this.quadSize *= 0.75F;
        // this.particleScale *= size;
        this.quadSize *= size;
        // this.smokeParticleScale = this.particleScale;
        this.smokeParticleScale = this.quadSize;
        // this.particleMaxAge = (int) (16.0D / (Math.random() * 0.8D + 0.2D));
        this.lifetime = (int) (16.0D / (Math.random() * 0.8D + 0.2D));
        // this.particleMaxAge = (int) (this.particleMaxAge * size);
        this.lifetime = (int) (this.lifetime * size);
        // this.noClip = false;
        this.speedUpWhenYMotionIsBlocked = false;
    }

    // Calen 1.18.2
    public void setSize(float quadSize) {
        this.quadSize = quadSize;
    }

    @Override
    // public void renderParticle(WorldRenderer worldRenderer, Entity entity, float partialTicks, float f1, float f2, float f3, float f4, float f5)
    public void render(VertexConsumer vertexConsumer, Camera camera, float partialTicks) {
        // float f6 = (this.particleAge + partialTicks) / this.particleMaxAge * 32.0F;
        float f6 = (this.age + partialTicks) / this.lifetime * 32.0F;

        if (f6 < 0.0F) {
            f6 = 0.0F;
        }

        if (f6 > 1.0F) {
            f6 = 1.0F;
        }

        // this.particleScale = this.smokeParticleScale * f6;// FIXME EntityRobotEnergyParticle
        this.quadSize = this.smokeParticleScale * f6;// FIXME EntityRobotEnergyParticle
        // super.renderParticle(worldRenderer, entity, partialTicks, f1, f2, f3, f4, f5);
        super.render(vertexConsumer, camera, partialTicks);
    }

    /** Called to update the entity's position/logic. */
    @Override
    // public void onUpdate()
    public void tick() {
//        this.prevPosX = this.posX;
//        this.prevPosY = this.posY;
//        this.prevPosZ = this.posZ;
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

//        if (this.particleAge++ >= this.particleMaxAge) {
//            this.setDead();
//        }
        if (this.age++ >= this.lifetime) {
            this.remove();
        }

        // this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
        this.setSpriteFromAge(this.spriteSet);
        // this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.move(this.xd, this.yd, this.zd);

//        this.motionX *= 0.98;
//        this.motionY += 0.0005;
//        this.motionZ *= 0.98;
        this.xd *= 0.98;
        this.yd += 0.0005;
        this.zd *= 0.98;

//        if (this.onGround) {
//            this.motionX *= 0.699999988079071D;
//            this.motionZ *= 0.699999988079071D;
//        }
        if (this.onGround) {
            this.xd *= 0.699999988079071D;
            this.zd *= 0.699999988079071D;
        }
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(@Nonnull SimpleParticleType type, @Nonnull ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new EntityRobotEnergyParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }
}
