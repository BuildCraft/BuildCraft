package buildcraft.core;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ItemFluidContainer;
import buildcraft.BuildCraftCore;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemGoldCanister extends ItemFluidContainer {
	
	public ItemGoldCanister(int id)
	{
		super(id);
		this.setUnlocalizedName("goldCanister");
		this.setMaxStackSize(16);
		this.setCapacity(FluidContainerRegistry.BUCKET_VOLUME*3);
		setCreativeTab(CreativeTabBuildCraft.TIER_3.get());
	}
 
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack iStack, EntityPlayer player, List list, boolean visible)
	{
		FluidStack fStack = getFluidStackFromItemStack(iStack);
		if (fStack==null){
			list.add("Can hold 3 buckets of liquid");
		} else {
			list.add("Currently stores " + Integer.toString(fStack.amount)+ " mB of " + fStack.getFluid().getLocalizedName().toString());
		}
		
 
	}
 
	public ItemStack getFilledItemStack(FluidStack fStack)
	{
		ItemStack iStack = new ItemStack(BuildCraftCore.goldCanister);
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
		this.itemIcon = par1IconRegister.registerIcon("buildcraft:goldCannister");
	}

}
