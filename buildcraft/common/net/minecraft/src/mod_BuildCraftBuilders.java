package net.minecraft.src;

import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.builders.BlockFiller;
import net.minecraft.src.buildcraft.builders.BlockMarker;
import net.minecraft.src.buildcraft.builders.TileFiller;
import net.minecraft.src.buildcraft.builders.TileMarker;

public class mod_BuildCraftBuilders extends BaseMod {

	public static BlockMarker markerBlock;
	public static BlockFiller fillerBlock;
	
	public void ModsLoaded () {		
		super.ModsLoaded();
		
		mod_BuildCraftCore.initialize();
		BuildCraftCore.initializeGears();
		
		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		markerBlock = new BlockMarker(Utils.getSafeBlockId("marker.blockId",
				154));		
		ModLoader.RegisterBlock(markerBlock);
		CoreProxy.addName(markerBlock.setBlockName("markerBlock"), "Marker");
		craftingmanager.addRecipe(new ItemStack(markerBlock, 1), new Object[] {
				"l ", "r ", Character.valueOf('l'),
				new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('r'),
				Block.torchRedstoneActive });	
		
		fillerBlock = new BlockFiller(Utils.getSafeBlockId("filler.blockId",
				155));		
		ModLoader.RegisterBlock(fillerBlock);
		CoreProxy.addName(fillerBlock.setBlockName("fillerBlock"), "Filler");
		craftingmanager.addRecipe(new ItemStack(fillerBlock, 1), new Object[] {
			"lll", "lcl", "ggg", Character.valueOf('l'), new ItemStack(Item.dyePowder, 1, 4),
			Character.valueOf('c'), Block.chest,
			Character.valueOf('g'), BuildCraftCore.stoneGearItem});	

		
		ModLoader.RegisterTileEntity(TileMarker.class, "Marker");
		ModLoader.RegisterTileEntity(TileFiller.class, "Filler");
		
		Utils.saveProperties();
	}
	
	@Override
	public String Version() {
		return "1.6.6.1";
	}

}
