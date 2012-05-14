package net.minecraft.src.buildcraft.core.utils;

import net.minecraft.src.EntityPlayer;

public interface IOwnable {

	boolean isSecure();
	String getOwnerName();
	void setOwner(EntityPlayer player);

}
