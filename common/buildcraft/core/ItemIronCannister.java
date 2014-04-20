package buildcraft.core;

import java.util.List;

import buildcraft.BuildCraftCore;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ItemFluidContainer;

public class ItemIronCannister extends ItemFluidContainer{
	
	public ItemIronCannister(int id)
	{
		super(id);
		this.setUnlocalizedName("ironCannister");
		this.setMaxStackSize(16);
		this.setCapacity(FluidContainerRegistry.BUCKET_VOLUME);
		setCreativeTab(CreativeTabBuildCraft.TIER_3.get());
	}
 
	
 
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack iStack, EntityPlayer player, List list, boolean visible)
	{
		FluidStack fStack = getFluidStackFromItemStack(iStack);
		if (fStack==null){
			list.add("Can hold 1 bucket of liquid");
		} else {
			list.add("Currently stores " + Integer.toString(fStack.amount)+ " buckets of " + fStack.getFluid().getLocalizedName().toString());
		}
		
 
	}
 
	public ItemStack getFilledItemStack(FluidStack fStack)
	{
		ItemStack iStack = new ItemStack(BuildCraftCore.ironCannister);
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
		this.itemIcon = par1IconRegister.registerIcon("buildcraft:ironCannister");
	}
}
