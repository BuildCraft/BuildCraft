package net.minecraft.src.buildcraft.core;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.buildcraft.api.tools.IToolWrench;

public class ItemWrench extends ItemBuildCraft implements IToolWrench {

	public ItemWrench(int i) {
		super(i);
	}

	@Override
	public boolean canWrench(EntityPlayer player, int x, int y, int z) {
		return true;
	}

	@Override
	public void wrenchUsed(EntityPlayer player, int x, int y, int z) {}
}
