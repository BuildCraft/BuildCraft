package buildcraft.core;

import buildcraft.BuildCraftTransport;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ItemFluidContainer;

import java.util.List;

public class ItemDiamondCanister extends ItemFluidContainer {
	
	public ItemDiamondCanister(int id)
	{
		super(id);
		this.setUnlocalizedName("diamondCanister");
		this.setMaxStackSize(16);
		this.setCapacity(FluidContainerRegistry.BUCKET_VOLUME*9);
		setCreativeTab(CreativeTabBuildCraft.TIER_3.get());
	}
 
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack iStack, EntityPlayer player, List list, boolean visible)
	{
		FluidStack fStack = getFluidStackFromItemStack(iStack);
		if (fStack==null){
			list.add("Can hold 9 buckets of liquid");
		} else {
			list.add("Currently stores " + Integer.toString(fStack.amount)+ " mB of " + fStack.getFluid().getLocalizedName().toString());
		}
		
 
	}
 
	public ItemStack getFilledItemStack(FluidStack fStack)
	{
		ItemStack iStack = new ItemStack(BuildCraftTransport.diamondCanister);
		if (iStack.getTagCompound() == null)
			iStack.setTagCompound(new NBTTagCompound());
		NBTTagCompound fluidTag = fStack.writeToNBT(new NBTTagCompound());
 
		if (fStack.amount > FluidContainerRegistry.BUCKET_VOLUME)
			fluidTag.setInteger("Amount", FluidContainerRegistry.BUCKET_VOLUME);
 
		iStack.getTagCompound().setTag("Fluid", fluidTag);
 
		return iStack;
	}
	public static FluidStack getFluidStackFromItemStack(ItemStack iStack)
	{
		if (iStack.stackTagCompound == null || !iStack.getTagCompound().hasKey("Fluid"))
			return null;
 
		NBTTagCompound fluidTag = iStack.getTagCompound().getCompoundTag("Fluid");
 
		return FluidStack.loadFluidStackFromNBT(fluidTag);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister)
	{
		this.itemIcon = par1IconRegister.registerIcon("buildcraft:diamondCannister");
	}

}
