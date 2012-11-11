package buildcraft.transport.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;


public class PacketLiquidUpdate extends PacketCoordinates{

	public LiquidStack[] displayLiquid = new LiquidStack[ForgeDirection.values().length];

	public PacketLiquidUpdate(int xCoord, int yCoord, int zCoord) {
		super(PacketIds.PIPE_LIQUID, xCoord, yCoord, zCoord);
	}
	
	public PacketLiquidUpdate() {

	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		for (ForgeDirection direction : ForgeDirection.values()){
			int liquidId = data.readInt();
			int liquidQuantity = data.readInt();
			int liquidMeta = data.readInt();
			displayLiquid[direction.ordinal()] = new LiquidStack(liquidId, liquidQuantity, liquidMeta);
		}
		
	}
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		for (ForgeDirection direction : ForgeDirection.values()){
			if (displayLiquid[direction.ordinal()] != null){
				data.writeInt(displayLiquid[direction.ordinal()].itemID);
				data.writeInt(displayLiquid[direction.ordinal()].amount);
				data.writeInt(displayLiquid[direction.ordinal()].itemMeta);
			} else {
				data.writeInt(0);
				data.writeInt(0);
				data.writeInt(0);
			}
			
		}
	}

}
