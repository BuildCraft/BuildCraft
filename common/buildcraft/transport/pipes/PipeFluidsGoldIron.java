package buildcraft.transport.pipes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeFluidsGoldIron extends PipeFluidsIron {

	public PipeFluidsGoldIron(int itemID) {
		super(itemID);
	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		updateStats();
		super.onNeighborBlockChange(blockId);
	}

	@Override
	public void onBlockPlaced() {
		updateStats();
		super.onBlockPlaced();
	}
	
	private void updateStats() {
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity neighborTile = container.worldObj.getBlockTileEntity(container.xCoord + dir.offsetX, container.yCoord + dir.offsetY, container.zCoord + dir.offsetZ);
			if(neighborTile != null && neighborTile instanceof TileGenericPipe) {
				Pipe neighborPipe = ((TileGenericPipe)neighborTile).pipe;
				if(neighborPipe.transport instanceof PipeTransportFluids) {
					short neighborFlowRate = ((PipeTransportFluids) neighborPipe.transport).flowRate;
					short neighborTravelDelay = ((PipeTransportFluids) neighborPipe.transport).travelDelay;
					if(neighborFlowRate > transport.flowRate)
						transport.flowRate = neighborFlowRate;
						transport.travelDelay = neighborTravelDelay;
				}
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}
	
	@Override
	public int getIconIndex(ForgeDirection direction) {
		return PipeIconProvider.TYPE.PipeFluidsGoldIron.ordinal();
	}
}