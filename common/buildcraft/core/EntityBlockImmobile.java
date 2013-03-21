package buildcraft.core;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.World;

public class EntityBlockImmobile extends EntityBlock {

	public EntityBlockImmobile(World world) {
		super(world);
	}

	public EntityBlockImmobile(World world, double xPos, double yPos,
			double zPos) {
		super(world, xPos, yPos, zPos);
	}

	public EntityBlockImmobile(World world, double i, double j, double k,
			double iSize, double jSize, double kSize) {
		super(world, i, j, k, iSize, jSize, kSize);
	}

	public EntityBlockImmobile(World world, double i, double j, double k,
			double iSize, double jSize, double kSize, int textureID) {
		super(world, i, j, k, iSize, jSize, kSize, textureID);
	}
	
	@Override
	public void moveEntity(double d, double d1, double d2) {
	}
	
	@Override
	public void addVelocity(double par1, double par3, double par5){
    }
	
	@SideOnly(Side.CLIENT)
	@Override
    public void setVelocity(double par1, double par3, double par5){
    }
	
}
