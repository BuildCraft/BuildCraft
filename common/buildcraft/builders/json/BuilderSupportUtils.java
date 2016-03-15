package buildcraft.builders.json;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public final class BuilderSupportUtils {
	public static class BlockItemPair {
		public final Block block;
		public final int meta;

		public BlockItemPair(Block block, int meta) {
			this.block = block;
			this.meta = meta;
		}
	}

	private BuilderSupportUtils() {

	}

	static void validateItemStack(BuilderSupportEntry e, String s) throws JSONValidationException {
		if (s.contains("!") && s.contains("@") && s.indexOf("@") > s.indexOf("!")) {
			throw new JSONValidationException(e, "Item count in stack defintion '" + s + "' must always be after metadata!");
		}

		if (!s.contains(":")) {
			throw new JSONValidationException(e, "Invalid item in stack defintion '" + s + "'!");
		}

		String n = s.split("!")[0].split("@")[0];
		if (Item.itemRegistry.getObject(new ResourceLocation(n)) == null && Block.getBlockFromName(n) == null) {
			throw new JSONValidationException(e, "Item in stack defintion '" + s + "' does not exist!");
		}
	}

	static BlockItemPair parseBlockItemPair(String s1) {
		if (s1.contains(":")) {
			String s = s1;
			int metadata = 0;
			if (s.contains("!")) {
				s = s.split("!")[0];
			}
			if (s.contains("@")) {
				metadata = new Integer(s.split("@")[1]).intValue();
				s = s.split("@")[0];
			}
			Block i = Block.getBlockFromName(s);
			if (i != null) {
				return new BlockItemPair(i, metadata);
			}
		}
		return null;
	}

	static ItemStack parseItemStack(String s1) {
		if (s1.contains(":")) {
			String s = s1;
			int count = 1;
			int metadata = 0;
			if (s.contains("!")) {
				count = new Integer(s.split("!")[1]).intValue();
				s = s.split("!")[0];
			}
			if (s.contains("@")) {
				metadata = new Integer(s.split("@")[1]).intValue();
				s = s.split("@")[0];
			}
			Item i = Item.itemRegistry.getObject(new ResourceLocation(s));
			if (i != null) {
				return new ItemStack(i, count, metadata);
			}
		}
		return null;
	}
}
