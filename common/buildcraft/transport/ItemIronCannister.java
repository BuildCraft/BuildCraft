package buildcraft.transport;

import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.fluids.FluidUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ItemFluidContainer;

import java.util.List;

public class ItemIronCannister extends ItemFluidContainer {

	public IIcon overlay;
	
	public ItemIronCannister(int id) {
		super(id);
		this.setUnlocalizedName("ironCannister");
		this.setMaxStackSize(16);
		this.setCapacity(FluidContainerRegistry.BUCKET_VOLUME);
		this.setCreativeTab(CreativeTabBuildCraft.TIER_3.get());
	}
	
	@Override
	public IIcon getIconFromDamage(int damage) {
		return itemIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack iStack, EntityPlayer player, List list, boolean visible) {
		FluidStack fStack = FluidUtils.getFluidStackFromItemStack(iStack);
		if (fStack == null)
			list.add("Can hold 1 bucket of liquid");
		else
			list.add("Currently stores " + Integer.toString(fStack.amount)+ " mB of " + fStack.getFluid().getLocalizedName());
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		this.itemIcon = par1IconRegister.registerIcon("buildcraft:ironCannister");
		this.overlay = par1IconRegister.registerIcon("buildcraft:fluidOverlay");
	}
}