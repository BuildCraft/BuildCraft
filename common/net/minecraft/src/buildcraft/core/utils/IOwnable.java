package net.minecraft.src.buildcraft.core.utils;

import net.minecraft.src.EntityPlayer;

public interface IOwnable {

	String getOwnerName();

	void setOwner(EntityPlayer player);

}
