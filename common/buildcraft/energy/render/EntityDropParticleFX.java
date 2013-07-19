package buildcraft.energy.render;

import net.minecraft.client.particle.*;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockFluid;
import net.minecraft.block.material.Material;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class EntityDropParticleFX extends EntityFX {

	/**
	 * The height of the current bob
	 */
	private int bobTimer;

	public EntityDropParticleFX(World world, double x, double y, double z, float particleRed, float particleGreen, float particleBlue) {
		super(world, x, y, z, 0.0D, 0.0D, 0.0D);
		this.motionX = this.motionY = this.motionZ = 0.0D;

//        if (par8Material == Material.water)
//        {
//            this.particleRed = 0.0F;
//            this.particleGreen = 0.0F;
//            this.particleBlue = 1.0F;
//        }
//        else
//        {
//            this.particleRed = 1.0F;
//            this.particleGreen = 0.0F;
//            this.particleBlue = 0.0F;
//        }
		this.particleRed = particleRed;
		this.particleGreen = particleGreen;
		this.particleBlue = particleBlue;

		this.setParticleTextureIndex(113);
		this.setSize(0.01F, 0.01F);
		this.particleGravity = 0.06F;
		this.bobTimer = 40;
		this.particleMaxAge = (int) (64.0D / (Math.random() * 0.8D + 0.2D));
		this.motionX = this.motionY = this.motionZ = 0.0D;
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		this.motionY -= (double) this.particleGravity;

		if (this.bobTimer-- > 0) {
			this.motionX *= 0.02D;
			this.motionY *= 0.02D;
			this.motionZ *= 0.02D;
			this.setParticleTextureIndex(113);
		} else {
			this.setParticleTextureIndex(112);
		}

		this.moveEntity(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;

		if (this.particleMaxAge-- <= 0) {
			this.setDead();
		}

		if (this.onGround) {
			this.setParticleTextureIndex(114);

			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
		}

		Material material = this.worldObj.getBlockMaterial(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));

		if (material.isLiquid() || material.isSolid()) {
			double d0 = (double) ((float) (MathHelper.floor_double(this.posY) + 1) - BlockFluid.getFluidHeightPercent(this.worldObj.getBlockMetadata(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ))));

			if (this.posY < d0) {
				this.setDead();
			}
		}
	}
}
