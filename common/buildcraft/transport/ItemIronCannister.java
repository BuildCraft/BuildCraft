package buildcraft.transport;

import java.util.List;

import javax.swing.Icon;
import javax.swing.Renderer;

import org.lwjgl.opengl.GL11;

import buildcraft.BuildCraftCore;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.fluids.FluidUtils;
import buildcraft.core.render.RenderUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ItemFluidContainer;

public class ItemIronCannister extends ItemFluidContainer{
	
	public IIcon overlay;
	
	public ItemIronCannister(int id)
	{
		super(id);
		this.setUnlocalizedName("ironCannister");
		this.setMaxStackSize(16);
		this.setCapacity(FluidContainerRegistry.BUCKET_VOLUME);
		setCreativeTab(CreativeTabBuildCraft.TIER_3.get());
	}
	
	@Override
	public boolean requiresMultipleRenderPasses() {
		return true;
	}
	public IIcon getIcon(ItemStack stack, int pass)
    {
		IIcon i = itemIcon;
		if(pass == 0) {
				return itemIcon;
		}
		if (FluidUtils.getFluidStackFromItemStack(stack) != null){
			return overlay;
		}
		
		return itemIcon;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack iStack, EntityPlayer player, List list, boolean visible)
	{
		FluidStack fStack = FluidUtils.getFluidStackFromItemStack(iStack);
		if (fStack==null){
			list.add("Can hold 1 bucket of liquid");
		} else {
			list.add("Currently stores " + Integer.toString(fStack.amount)+ " mB of " + fStack.getFluid().getLocalizedName().toString());
		}
		
 
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister)
	{
		this.itemIcon = par1IconRegister.registerIcon("buildcraft:ironCannister");
		this.overlay = par1IconRegister.registerIcon("buildcraft:fluidOverlay");
	}
	
}