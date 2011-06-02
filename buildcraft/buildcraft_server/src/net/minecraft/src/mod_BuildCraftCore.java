package net.minecraft.src;

public class mod_BuildCraftCore extends BaseModMp {	
	
	BuildCraftCore proxy = new BuildCraftCore();
		
	public static void initialize () {
		BuildCraftCore.initialize ();
	}
		
	public void ModsLoaded () {
		mod_BuildCraftCore.initialize();
		BuildCraftCore.initializeModel(this);
		
//		ModLoaderMp.RegisterEntityTracker(EntityPassiveItem.class, 160, 1);
//		ModLoaderMp.RegisterEntityTrackerEntry(EntityPassiveItem.class, BuildCraftCore.trackedPassiveEntityId);
	}
	
	@Override
	public String Version() {
		return "1.6.6.1";
	}
	
//    public void HandlePacket(Packet230ModLoader packet230modloader, EntityPlayerMP entityplayermp)
//    {
//    	System.out.println ("HANDLE PACKET");
//    }
}
