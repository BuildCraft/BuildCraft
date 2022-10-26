package buildcraft.silicon;

import java.util.List;

import net.minecraft.block.BlockDispenser;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.silicon.render.PackageFontRenderer;

public class ItemPackage extends ItemBuildCraft {
	public static final class DispenseBehaviour extends BehaviorDefaultDispenseItem {
		@Override
		public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
			if (stack != null && stack.getItem() instanceof ItemPackage) {
				World world = source.getWorld();
				EnumFacing enumfacing = BlockDispenser.func_149937_b(source.getBlockMetadata());

				EntityPackage entityPackage = new EntityPackage(source.getWorld(),
						source.getX() + enumfacing.getFrontOffsetX(),
						source.getY() + enumfacing.getFrontOffsetY(),
						source.getZ() + enumfacing.getFrontOffsetZ(), stack.copy());
				entityPackage.setThrowableHeading(enumfacing.getFrontOffsetX(), enumfacing.getFrontOffsetY() + 0.1F, enumfacing.getFrontOffsetZ(), 1.1F, 6.0F);
				world.spawnEntityInWorld(entityPackage);
				stack.splitStack(1);
			}
			return stack;
		}
	}

	public ItemPackage() {
		super();
		setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {

	}

	public static void update(ItemStack stack) {

	}

	public static ItemStack getStack(ItemStack stack, int slot) {
		NBTTagCompound tag = NBTUtils.getItemData(stack);
		if (tag.hasKey("item" + slot)) {
			return ItemStack.loadItemStackFromNBT(tag.getCompoundTag("item" + slot));
		} else {
			return null;
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

		if (!world.isRemote) {
			world.spawnEntityInWorld(new EntityPackage(world, player, stack.copy()));
		}

		if (!player.capabilities.isCreativeMode) {
			stack.stackSize--;
		}

		return stack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack) {
		return new PackageFontRenderer(stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List strings, boolean adv) {
		NBTTagCompound tag = NBTUtils.getItemData(stack);
		if (!tag.hasNoTags()) {
			strings.add("{{BC_PACKAGE_SPECIAL:0}}");
			strings.add("{{BC_PACKAGE_SPECIAL:1}}");
			strings.add("{{BC_PACKAGE_SPECIAL:2}}");
		}
	}
}
