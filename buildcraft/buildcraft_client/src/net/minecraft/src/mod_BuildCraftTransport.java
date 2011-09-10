package net.minecraft.src;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.transport.GuiDiamondPipe;
import net.minecraft.src.buildcraft.transport.ItemPipe;
import net.minecraft.src.buildcraft.transport.RenderPipe;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
//import net.minecraft.src.buildcraft.transport.RenderPipe;
//import net.minecraft.src.buildcraft.transport.TileDiamondPipe;
//import net.minecraft.src.buildcraft.transport.TilePipe;
import net.minecraft.src.forge.ICustomItemRenderer;
import net.minecraft.src.forge.MinecraftForgeClient;

public class mod_BuildCraftTransport extends BaseModMp implements ICustomItemRenderer {
		
	public static mod_BuildCraftTransport instance;

	public void ModsLoaded () {
		super.ModsLoaded();
		BuildCraftTransport.ModsLoaded();
		
		ModLoaderMp.RegisterGUI(this,
				Utils.packetIdToInt(PacketIds.DiamondPipeGUI));
		

		MinecraftForgeClient.registerCustomItemRenderer(
				BuildCraftTransport.pipeItemsWood.shiftedIndex, this);
		MinecraftForgeClient.registerCustomItemRenderer(
				BuildCraftTransport.pipeItemsStone.shiftedIndex, this);
		MinecraftForgeClient.registerCustomItemRenderer(
				BuildCraftTransport.pipeLiquidsWood.shiftedIndex, this);
		MinecraftForgeClient.registerCustomItemRenderer(
				BuildCraftTransport.pipeLiquidsStone.shiftedIndex, this);
		MinecraftForgeClient.registerCustomItemRenderer(
				BuildCraftTransport.pipePowerWood.shiftedIndex, this);
		MinecraftForgeClient.registerCustomItemRenderer(
				BuildCraftTransport.pipePowerStone.shiftedIndex, this);
		MinecraftForgeClient.registerCustomItemRenderer(
				BuildCraftTransport.pipePowerGold.shiftedIndex, this);
		
		ModLoader.RegisterTileEntity(TileGenericPipe.class,
				"net.minecraft.src.buildcraft.GenericPipe", new RenderPipe());		
		
//		ModLoader
//				.RegisterTileEntity(TilePipe.class,
//						"net.minecraft.src.buildcraft.Pipe",
//						new RenderPipe());
		
		instance = this;
	}
	
	@Override
	public String Version() {
		return "2.1.1";
	}
	
//    public GuiScreen HandleGUI(int i) {    	
//    	if (Utils.intToPacketId(i) == PacketIds.DiamondPipeGUI) {
//			return new GuiDiamondPipe(
//					ModLoader.getMinecraftInstance().thePlayer.inventory,
//					new TileDiamondPipe());
//    	} else {
//    		return null;
//    	}
//    }
    
    public void HandlePacket(Packet230ModLoader packet) {    	
		int x = packet.dataInt [0];
		int y = packet.dataInt [1];
		int z = packet.dataInt [2];
		
//		if (packet.packetType == PacketIds.PipeItem.ordinal()) {						
//			if (APIProxy.getWorld().blockExists(x, y, z)) {
//				TileEntity tile = APIProxy.getWorld().getBlockTileEntity(x, y, z);
//				
//				if (tile instanceof TilePipe) {
//					((TilePipe) tile).handleItemPacket(packet);	
//					
//					return;
//				}
//			}
//		} else if (packet.packetType == PacketIds.DiamondPipeContents.ordinal()) {	
//			if (APIProxy.getWorld().blockExists(x, y, z)) {
//				TileEntity tile = APIProxy.getWorld().getBlockTileEntity(x, y, z);
//				
//				if (tile instanceof TileDiamondPipe) {
//					((TileDiamondPipe) tile).handlePacket(packet);	
//					
//					return;
//				}
//			}
//			
//			BlockIndex index = new BlockIndex(x, y, z);
//			
//			if (BuildCraftCore.bufferedDescriptions.containsKey(index)) {
//				BuildCraftCore.bufferedDescriptions.remove(index);
//			}			
//			
//			BuildCraftCore.bufferedDescriptions.put(index, packet);
//		}			
    }

	@Override
	public void renderInventory(RenderBlocks renderblocks, int itemID,
			int meta) {
		Tessellator tessellator = Tessellator.instance;

		Block block = BuildCraftTransport.genericPipeBlock;
		int textureID = ((ItemPipe) Item.itemsList [itemID]).getTextureIndex();
		
		block.setBlockBounds(Utils.pipeMinPos, 0.0F, Utils.pipeMinPos,
				Utils.pipeMaxPos, 1.0F, Utils.pipeMaxPos);
		block.setBlockBoundsForItemRender();
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1F, 0.0F);
		renderblocks.renderBottomFace(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		renderblocks.renderTopFace(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, -1F);
		renderblocks.renderEastFace(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		renderblocks.renderWestFace(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(-1F, 0.0F, 0.0F);
		renderblocks.renderNorthFace(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		renderblocks.renderSouthFace(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		
	}
}
