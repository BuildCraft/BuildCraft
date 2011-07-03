package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.BuildCraftEnergy;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EnumMovingObjectType;
import net.minecraft.src.Item;
import net.minecraft.src.ItemBucket;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.MathHelper;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.Vec3D;
import net.minecraft.src.World;

public class ItemBuildCraftBucket extends ItemBucket {

	public ItemBuildCraftBucket(int i, int j) {
		super(i, j);
		// TODO Auto-generated constructor stub
	}
	
	 public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	    {
	        float f = 1.0F;
	        float f1 = entityplayer.prevRotationPitch + (entityplayer.rotationPitch - entityplayer.prevRotationPitch) * f;
	        float f2 = entityplayer.prevRotationYaw + (entityplayer.rotationYaw - entityplayer.prevRotationYaw) * f;
	        double d = entityplayer.prevPosX + (entityplayer.posX - entityplayer.prevPosX) * (double)f;
	        double d1 = (entityplayer.prevPosY + (entityplayer.posY - entityplayer.prevPosY) * (double)f + 1.6200000000000001D) - (double)entityplayer.yOffset;
	        double d2 = entityplayer.prevPosZ + (entityplayer.posZ - entityplayer.prevPosZ) * (double)f;
	        Vec3D vec3d = Vec3D.createVector(d, d1, d2);
	        float f3 = MathHelper.cos(-f2 * 0.01745329F - 3.141593F);
	        float f4 = MathHelper.sin(-f2 * 0.01745329F - 3.141593F);
	        float f5 = -MathHelper.cos(-f1 * 0.01745329F);
	        float f6 = MathHelper.sin(-f1 * 0.01745329F);
	        float f7 = f4 * f5;
	        float f8 = f6;
	        float f9 = f3 * f5;
	        double d3 = 5D;
	        Vec3D vec3d1 = vec3d.addVector((double)f7 * d3, (double)f8 * d3, (double)f9 * d3);
	        MovingObjectPosition movingobjectposition = world.rayTraceBlocks_do(vec3d, vec3d1, this == Item.bucketEmpty);
	        if(movingobjectposition == null)
	        {
	            return itemstack;
	        }
	        if(movingobjectposition.typeOfHit == EnumMovingObjectType.TILE)
	        {
	            int i = movingobjectposition.blockX;
	            int j = movingobjectposition.blockY;
	            int k = movingobjectposition.blockZ;

	            if(this == Item.bucketEmpty)
	            {
	                if((world.getBlockId(i, j, k) == BuildCraftEnergy.oilStill.blockID
	                		|| world.getBlockId(i, j, k) == BuildCraftEnergy.oilMoving.blockID)
	                		&& world.getBlockMetadata(i, j, k) == 0)
	                {
	                    world.setBlockWithNotify(i, j, k, 0);
	                    return new ItemStack(BuildCraftEnergy.bucketOil);
	                }
	            } 
	        }
	        
	        return super.onItemRightClick(itemstack, world, entityplayer);
	    }

}
