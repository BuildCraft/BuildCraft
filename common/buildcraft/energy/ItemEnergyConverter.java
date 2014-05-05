package buildcraft.energy;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import buildcraft.core.utils.StringUtils;

public class ItemEnergyConverter extends ItemBlock {
	public ItemEnergyConverter(Block block) {
		super(block);
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean adv) {
		super.addInformation(itemStack, player, list, adv);
		list.add(TileEnergyConverter.getLocalizedModeName(itemStack));
		list.add("");
		list.addAll(Arrays.asList(StringUtils.localize("tile.energyConverter.tooltip").split("\\|")));
	}
}
