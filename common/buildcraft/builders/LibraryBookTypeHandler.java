package buildcraft.builders;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.library.LibraryTypeHandlerNBT;
import buildcraft.core.lib.utils.NBTUtils;

public class LibraryBookTypeHandler extends LibraryTypeHandlerNBT {
	public LibraryBookTypeHandler() {
		super("book");
	}

	@Override
	public boolean isHandler(ItemStack stack, HandlerType type) {
		if (type == HandlerType.STORE) {
			return stack.getItem() == Items.written_book;
		} else {
			return stack.getItem() == Items.writable_book || stack.getItem() == Items.written_book;
		}
	}

	@Override
	public int getTextColor() {
		return 0x684804;
	}

	@Override
	public String getName(ItemStack stack) {
		String s = NBTUtils.getItemData(stack).getString("title");
		return s != null ? s : "";
	}

	@Override
	public ItemStack load(ItemStack stack, NBTTagCompound compound) {
		ItemStack out = new ItemStack(Items.written_book);
		NBTTagCompound outNBT = new NBTTagCompound();
		outNBT.setString("title", compound.getString("title"));
		outNBT.setString("author", compound.getString("author"));
		outNBT.setTag("pages", compound.getTagList("pages", 8));
		out.setTagCompound(outNBT);
		return out;
	}

	@Override
	public boolean store(ItemStack stack, NBTTagCompound compound) {
		NBTTagCompound inNBT = NBTUtils.getItemData(stack);
		compound.setString("title", inNBT.getString("title"));
		compound.setString("author", inNBT.getString("author"));
		compound.setTag("pages", inNBT.getTagList("pages", 8));
		return true;
	}
}
