package net.minecraft.src.buildcraft.api;


/**
 * Interface used to put objects into pipes, implemented by pipe tile entities.
 */
public interface IPipeEntry {
	
	public void entityEntering(EntityPassiveItem item, Orientations orientation);

	public boolean acceptItems ();
}
