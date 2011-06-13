package net.minecraft.src;

import net.minecraft.src.buildcraft.core.BluePrint;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.builders.BlockBuilder;
import net.minecraft.src.buildcraft.builders.BlockFiller;
import net.minecraft.src.buildcraft.builders.BlockMarker;
import net.minecraft.src.buildcraft.builders.BlockTemplate;
import net.minecraft.src.buildcraft.builders.ItemTemplate;
import net.minecraft.src.buildcraft.builders.TileBuilder;
import net.minecraft.src.buildcraft.builders.TileFiller;
import net.minecraft.src.buildcraft.builders.TileMarker;
import net.minecraft.src.buildcraft.builders.TileTemplate;

public class mod_BuildCraftBuilders extends BaseModMp {

	public static BlockMarker markerBlock;
	public static BlockFiller fillerBlock;
	public static BlockBuilder builderBlock;
	public static BlockTemplate templateBlock;
	public static ItemTemplate templateItem;
	
	public void ModsLoaded () {		
		super.ModsLoaded();
		
		mod_BuildCraftCore.initialize();
		BuildCraftCore.initializeGears();
		
		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		markerBlock = new BlockMarker(Utils.getSafeBlockId("marker.blockId",
				154));		
		ModLoader.RegisterBlock(markerBlock);
		CoreProxy.addName(markerBlock.setBlockName("markerBlock"), "Land Mark");
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
		
		builderBlock = new BlockBuilder(Utils.getSafeBlockId("builder.blockId",
				157));
		ModLoader.RegisterBlock(builderBlock);
		CoreProxy.addName(builderBlock.setBlockName("builderBlock"), "Builder");
		
		templateBlock = new BlockTemplate(Utils.getSafeBlockId("template.blockId",
				158));
		ModLoader.RegisterBlock(templateBlock);
		CoreProxy.addName(templateBlock.setBlockName("templateBlock"), "Template Drawing Table");
		
		templateItem = new ItemTemplate (Integer.parseInt(Utils.getProperty(
				"templateItem.id", "3805")));
		templateItem.setItemName("templateItem");
		CoreProxy.addName(templateItem, "Template");
		
		ModLoader.RegisterTileEntity(TileMarker.class, "Marker");
		ModLoader.RegisterTileEntity(TileFiller.class, "Filler");
		ModLoader.RegisterTileEntity(TileBuilder.class,
				"net.minecraft.src.builders.TileBuilder");
		ModLoader.RegisterTileEntity(TileTemplate.class,
				"net.minecraft.src.builders.TileTemplate");
		
		Utils.saveProperties();
	}
	
	@Override
	public String Version() {
		return "1.6.6.3";
	}
	
	public static BluePrint bluePrints [] = new BluePrint [65025];
	
	public static int storeBluePrint (BluePrint bluePrint) {
		for (int i = 0; i < bluePrints.length; ++i) {
			if (bluePrints [i] == null) {
				bluePrints [i] = bluePrint;
				
				return i;
			}
		}
		
		throw new RuntimeException("No more blueprint slot available.");
	}

}
