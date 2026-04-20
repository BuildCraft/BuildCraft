package ct.buildcraft.core.item;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.core.IEngineType;
import ct.buildcraft.api.enums.EnumEngineType;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ItemEngine_BC8<E extends Enum<E> & IEngineType> extends BlockItem{

	public final E type;

	
	public ItemEngine_BC8(Block p_40565_, Properties p_40566_, E type) {
		super(p_40565_, p_40566_);
		this.type = type;
	}

	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {
		if(this.allowedIn(tab)) {
			list.add(new ItemStack(this));}
	}

	@Override
	public Component getName(ItemStack p_41458_) {
		String s = type instanceof EnumEngineType ? ((EnumEngineType)type).unlocalizedTag : type.name();
		String p = getDescriptionId();
		BCLog.logger.debug(Boolean.toString(p_41458_.getTag() == null));
		return Component.translatable(p+"_" + s);
	}
	
	
}
