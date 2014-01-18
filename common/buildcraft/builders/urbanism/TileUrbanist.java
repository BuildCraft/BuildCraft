package buildcraft.builders.urbanism;

import buildcraft.core.IBuilderInventory;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCSide;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class TileUrbanist extends TileBuildCraft implements IBuilderInventory {

	public EntityUrbanist urbanist;
	EntityLivingBase player;
	int thirdPersonView = 0;

	double posX, posY, posZ;
	float yaw;


	@RPC (RPCSide.SERVER)
	public void setBlock (int x, int y, int z) {
		worldObj.setBlock(x, y, z, Block.brick.blockID);
	}

	@RPC (RPCSide.SERVER)
	public void eraseBlock (int x, int y, int z) {
		worldObj.setBlock(x, y, z, 0);
	}

	public void rpcEraseBlock (int x, int y, int z) {
		RPCHandler.rpcServer(this, "eraseBlock", x, y, z);
	}


	public void createUrbanistEntity() {
		if (worldObj.isRemote) {
			if (urbanist == null) {
				urbanist = new EntityUrbanist(worldObj);
				worldObj.spawnEntityInWorld(urbanist);
				player = Minecraft.getMinecraft().renderViewEntity;

				urbanist.copyLocationAndAnglesFrom(player);
				urbanist.tile = this;
				urbanist.player = player;

				urbanist.rotationYaw = 0;
				urbanist.rotationPitch = 0;

				Minecraft.getMinecraft().renderViewEntity = urbanist;
				thirdPersonView = Minecraft.getMinecraft().gameSettings.thirdPersonView;
				Minecraft.getMinecraft().gameSettings.thirdPersonView = 8;

				posX = urbanist.posX;
				posY = urbanist.posY + 10;
				posZ = urbanist.posZ;

				yaw = 0;

				urbanist.setPositionAndRotation(posX, posY, posZ, yaw, 50);
				urbanist.setPositionAndUpdate(posX, posY, posZ);
			}
		}

	}

	public void destroyUrbanistEntity() {
		Minecraft.getMinecraft().renderViewEntity = player;
		Minecraft.getMinecraft().gameSettings.thirdPersonView = thirdPersonView;
		worldObj.removeEntity(urbanist);
		urbanist = null;
	}

	@Override
	public int getSizeInventory() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getInvName() {
		// TODO Auto-generated method stub
		return "Urbanist";
	}

	@Override
	public int getInventoryStackLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void openChest() {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeChest() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBuildingMaterial(int i) {
		// TODO Auto-generated method stub
		return false;
	}

}
