package buildcraft.transport;

import buildcraft.api.transport.PipeWire;
import buildcraft.core.ItemBuildCraft;
import buildcraft.silicon.ItemRedstoneChipset.Chipset;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemPipeWire extends ItemBuildCraft {

	private Icon[] icons;

	public ItemPipeWire() {
		super();
		setHasSubtypes(true);
		setMaxDamage(0);
		setPassSneakClick(true);
		setBlockName("pipeWire");
	}

	@Override
	public IIcon getIconFromDamage(int damage) {
		return icons[damage];
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "item." + PipeWire.fromOrdinal(stack.getItemDamage()).getTag();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int id, CreativeTabs tab, List itemList) {
		for (PipeWire pipeWire : PipeWire.VALUES) {
			itemList.add(pipeWire.getStack());
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		icons = new Icon[PipeWire.VALUES.length];
		for (PipeWire pipeWire : PipeWire.VALUES) {
			icons[pipeWire.ordinal()] = par1IconRegister.registerIcon("buildcraft:" + pipeWire.getTag());
		}
	}

	public void registerItemStacks() {
		for (PipeWire pipeWire : PipeWire.VALUES) {
			GameRegistry.registerCustomItemStack(pipeWire.getTag(), pipeWire.getStack());
		}
	}
}
