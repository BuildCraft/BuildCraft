package net.minecraft.src.buildcraft.api.pipes;

import net.minecraft.src.EntityPlayer;

public interface IOwnable {

	boolean isSecure();
	String getOwnerName();
	void setOwner(EntityPlayer player);

}
