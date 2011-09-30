/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 * 
 * As a special exception, this file is part of the BuildCraft API and is 
 * allowed to be redistributed, either in source or binaries form.
 */

package net.minecraft.src.buildcraft.api;

import net.minecraft.src.NBTTagCompound;

public abstract class PowerFramework {

	static private String baseNBTName = "net.minecraft.src.buildcarft.Power";

	public abstract PowerProvider createPowerProvider ();
	
	public void loadPowerProvider(IPowerReceptor receptor,
			NBTTagCompound compound) {
		
		PowerProvider provider = createPowerProvider();
		
		if (compound.hasKey(baseNBTName)) {
			NBTTagCompound cpt = compound.getCompoundTag(baseNBTName);
			if (cpt.getString("class").equals(this.getClass().getName())) {
				provider.readFromNBT(cpt.getCompoundTag("contents"));
			}			
		}
		
		receptor.setPowerProvider(provider);
	}
	
	public void savePowerProvider(IPowerReceptor receptor,
			NBTTagCompound compound) {
		
		PowerProvider provider = receptor.getPowerProvider();
		
		if (provider == null) {
			return;
		}
		
		NBTTagCompound cpt = new NBTTagCompound();
		
		cpt.setString("class", this.getClass().getName());
		
		NBTTagCompound contents = new NBTTagCompound();
		
		provider.writeToNBT(contents);
		
		cpt.setTag("contents", contents);
		compound.setTag(baseNBTName, cpt);		
	}
	
}
