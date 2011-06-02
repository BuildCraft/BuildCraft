package net.minecraft.src;

import net.minecraft.src.buildcraft.api.EntityPassiveItem;

public class mod_BuildCraftCore extends BaseModMp {	
	
	BuildCraftCore proxy = new BuildCraftCore();
		
	public static void initialize () {
		BuildCraftCore.initialize ();
	}
		
	public void ModsLoaded () {
		mod_BuildCraftCore.initialize();
		BuildCraftCore.initializeModel(this);
		
		ModLoaderMp.RegisterEntityTracker(EntityPassiveItem.class, 160, 1);
		ModLoaderMp.RegisterEntityTrackerEntry(EntityPassiveItem.class, 156);
	}
	
	@Override
	public String Version() {
		return "1.5_01.4";
	}
	
    public void HandlePacket(Packet230ModLoader packet230modloader, EntityPlayerMP entityplayermp)
    {
    	System.out.println ("HANDLE PACKET");
    }
}
