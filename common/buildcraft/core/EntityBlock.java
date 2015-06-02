/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityBlock extends Entity {

	public float shadowSize = 0;
	public float rotationX = 0;
	public float rotationY = 0;
	public float rotationZ = 0;
	public double iSize, jSize, kSize;
	private int brightness = -1;
	public ResourceLocation resource;
	public IBlockState blockState;
	
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite texture;

	public EntityBlock(World world) {
		super(world);
		preventEntitySpawning = false;
		noClip = true;
		isImmuneToFire = true;
		ignoreFrustumCheck = true;
	}

	public EntityBlock(World world, double xPos, double yPos, double zPos) {
		super(world);
		setPositionAndRotation(xPos, yPos, zPos, 0, 0);
	}

	public EntityBlock(World world, double i, double j, double k, double iSize, double jSize, double kSize) {
		this(world);
		this.iSize = iSize;
		this.jSize = jSize;
		this.kSize = kSize;
		setPositionAndRotation(i, j, k, 0, 0);
		this.motionX = 0.0;
		this.motionY = 0.0;
		this.motionZ = 0.0;
	}

	@Override
	public void setPosition(double d, double d1, double d2) {
		super.setPosition(d, d1, d2);
		this.setEntityBoundingBox(AxisAlignedBB.fromBounds(posX, posY, posZ, posX + iSize, posY + jSize, posZ + kSize));
	}

	@Override
	public void moveEntity(double d, double d1, double d2) {
		setPosition(posX + d, posY + d1, posZ + d2);
	}

	public void setBrightness(int brightness) {
		this.brightness = brightness;
	}

	@Override
	protected void entityInit() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound data) {
		iSize = data.getDouble("iSize");
		jSize = data.getDouble("jSize");
		kSize = data.getDouble("kSize");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound data) {
		data.setDouble("iSize", iSize);
		data.setDouble("jSize", jSize);
		data.setDouble("kSize", kSize);
	}

	@Override
	public int getBrightnessForRender(float par1) {
		return brightness > 0 ? brightness : super.getBrightnessForRender(par1);
	}
}
