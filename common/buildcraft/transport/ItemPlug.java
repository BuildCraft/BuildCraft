package buildcraft.transport;

import buildcraft.core.ItemBuildCraft;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemPlug extends ItemBuildCraft {

	public ItemPlug(int i) {
		super(i);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		return "item.PipePlug";
	}
	
//	@Override
//	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World worldObj, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
//		if (worldObj.isRemote)
//			return false;
//		TileEntity tile = worldObj.getBlockTileEntity(x, y, z);
//		if (!(tile instanceof TileGenericPipe))
//			return false;
//		TileGenericPipe pipeTile = (TileGenericPipe) tile;
//
//		if (player.isSneaking()) { // Strip plug
//			if (!pipeTile.hasPlug(ForgeDirection.VALID_DIRECTIONS[side]))
//				return false;
//			pipeTile.removeAndDropPlug(ForgeDirection.VALID_DIRECTIONS[side]);
//			return true;
//		} else {
//			if (((TileGenericPipe) tile).addPlug(ForgeDirection.VALID_DIRECTIONS[side])){
//				if (!player.capabilities.isCreativeMode) {
//					stack.stackSize--;
//				}
//				return true;
//			}
//			return false;
//		}
//	}
	
	@Override
	public boolean shouldPassSneakingClickToBlock(World worldObj, int x, int y, int z ) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
	    // NOOP
	}

	@Override
    @SideOnly(Side.CLIENT)
    public int getSpriteNumber()
    {
        return 0;
    }
	
}
