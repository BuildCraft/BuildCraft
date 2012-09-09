/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.Orientations;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.liquids.ILiquidTank;
import buildcraft.api.liquids.ITankContainer;
import buildcraft.api.liquids.LiquidManager;
import buildcraft.api.liquids.LiquidStack;
import buildcraft.api.liquids.LiquidTank;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

public class TileTank extends TileBuildCraft implements ITankContainer
{

    public final ILiquidTank tank = new LiquidTank(LiquidManager.BUCKET_VOLUME * 16);
    public boolean hasUpdate = false;
    public SafeTimeTracker tracker = new SafeTimeTracker();

    /* UPDATING */
    @Override
    public void updateEntity()
    {
        if(CoreProxy.proxy.isSimulating(worldObj) && hasUpdate && tracker.markTimeIfDelay(worldObj, 2 * BuildCraftCore.updateFactor)) {
            sendNetworkUpdate();
            hasUpdate = false;
        }

        if(CoreProxy.proxy.isRemote(worldObj)) {
            return;
        }

        // Have liquid flow down into tanks below or adjacent, if any.
		if(stored > 0){
			moveLiquidBelow();
			moveLiquidDiffuseSide();
		}
    }

    /* NETWORK */
    @Override
    public PacketPayload getPacketPayload()
    {
        PacketPayload payload = new PacketPayload(3, 0, 0);
        if(tank.getLiquid() != null) {
            payload.intPayload[0] = tank.getLiquid().itemID;
            payload.intPayload[1] = tank.getLiquid().itemMeta;
            payload.intPayload[2] = tank.getLiquid().amount;
        } else {
            payload.intPayload[0] = 0;
            payload.intPayload[1] = 0;
            payload.intPayload[2] = 0;
        }
        return payload;
    }

    @Override
    public void handleUpdatePacket(PacketUpdate packet)
    {
        if(packet.payload.intPayload[0] > 0) {
            LiquidStack liquid = new LiquidStack(packet.payload.intPayload[0], packet.payload.intPayload[2], packet.payload.intPayload[1]);
            tank.setLiquid(liquid);
        } else {
            tank.setLiquid(null);
        }
    }

    /* SAVING & LOADING */
    @Override
    public void readFromNBT(NBTTagCompound data)
    {
        super.readFromNBT(data);
        
        if(data.hasKey("stored") && data.hasKey("liquidId"))
        {
	        LiquidStack liquid = new LiquidStack(data.getInteger("liquidId"), data.getInteger("stored"), 0);
	        tank.setLiquid(liquid);
        }
        else
        {        
	        LiquidStack liquid = new LiquidStack(0, 0, 0);
	        liquid.readFromNBT(data.getCompoundTag("tank"));
	        tank.setLiquid(liquid);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound data)
    {
        super.writeToNBT(data);
        if(tank.getLiquid() != null)
            data.setTag("tank", tank.getLiquid().writeToNBT(new NBTTagCompound()));
    }

    /* HELPER FUNCTIONS */
    /**
     * @return Last tank block below this one or this one if it is the last.
     */
    public TileTank getBottomTank()
    {

        TileTank lastTank = this;

        while(true) {
            TileTank below = getTankBelow(lastTank);
            if(below != null) {
                lastTank = below;
            } else {
                break;
            }
        }

        return lastTank;
    }

    public TileTank getTopTank()
    {

        TileTank lastTank = this;

        while(true) {
            TileTank above = getTankAbove(lastTank);
            if(above != null) {
                lastTank = above;
            } else {
                break;
            }
        }

        return lastTank;
    }

    public static TileTank getTankBelow(TileTank tile)
    {
        TileEntity below = tile.worldObj.getBlockTileEntity(tile.xCoord, tile.yCoord - 1, tile.zCoord);
        if(below instanceof TileTank) {
            return (TileTank)below;
        } else {
            return null;
        }
    }

    public static TileTank getTankAbove(TileTank tile)
    {
        TileEntity above = tile.worldObj.getBlockTileEntity(tile.xCoord, tile.yCoord + 1, tile.zCoord);
        if(above instanceof TileTank) {
            return (TileTank)above;
        } else {
            return null;
        }
    }
    
    public TileTank getTankBeside(TileTank tile,int XZDirection) {
		
		TileEntity next = worldObj.getBlockTileEntity(tile.xCoord+Direction.offsetX[XZDirection], tile.yCoord, tile.zCoord+Direction.offsetZ[XZDirection]);
		if(next instanceof TileTank)
			return(TileTank)next;
		else
			return null;			
	}
    public void moveLiquidBelow()
    {
        TileTank below = getTankBelow(this);
        if(below == null) {
            return;
        }

        int used = below.tank.fill(tank.getLiquid(), true);
        if(used>0) {
            hasUpdate= true; // not redundant because tank.drain operates on an ILiquidTank, not a tile
            below.hasUpdate=true; // redundant because below.fill sets hasUpdate

            tank.drain(used, true);
        }
    }

    
	public void moveLiquidDiffuseSide() {
		for(int i=0;i<4;i++){ // for each xz adjacent tank
			LiquidStack src= this.tank.getLiquid();
			if(src==null || src.amount==0)
				break; // if we have no liquid, don't bother.
			
			TileTank next = getTankBeside(this,i);
			if(next == null)
				continue;
	
			LiquidStack target= next.tank.getLiquid();
			LiquidStack toMove=src.copy(); // so that when we manipulate it, we don't damage this.
                		toMove.amount=Math.min(toMove.amount,100); // maximum amount to move in 1 update.
			if(target==null){ // if there is nothing in the target
				target=src.copy(); // we know what liquid we want to move
				target.amount=0; // but there's n
			} else{
				if(src.amount >= target.amount-1) // -1 is so that small deltas don't trigger endless packets.
					continue; // don't diffuse to tanks with more liquid
			
			if(src.isLiquidEqual(target)==false)
				continue; // don't diffuse to a tank with something different
			}
			if(target.amount==0 && src.amount<10){
				// if there's only a small amount, and the next tank is empty, move it over to the next tank in 1 hit, as it might fall through to a lower tank
				toMove.amount =src.amount;
			} else
			{
				// div4 is because of 4 directions to diffuse, the +1 (div 5 instead of 4) is to make it not-instant, to prevent unstable slosh)
				toMove.amount =Math.min(toMove.amount,(src.amount-target.amount)/5);
			}
			int moved = next.fill(toMove, true);
			tank.drain(moved, true);
		}
		
	}
    /* ITANKCONTAINER */
    @Override
    public int fill(Orientations from, LiquidStack resource, boolean doFill)
    {
        return fill(0, resource, doFill);
    }

    @Override
    public int fill(int tankIndex, LiquidStack resource, boolean doFill)
    {
        if(tankIndex != 0 || resource == null)
           return 0;

        resource = resource.copy();
        int totalUsed = 0;
        TileTank tankToFill = getBottomTank();
        while(tankToFill != null && resource.amount > 0){
            int used = tankToFill.tank.fill(resource, doFill);
            resource.amount -= used;
            if(used>0)
                tankToFill.hasUpdate=true;

            totalUsed += used;
            tankToFill = getTankAbove(tankToFill);
        }
        if(totalUsed>0)
            hasUpdate= true;
        return totalUsed;
    }

    @Override
    public LiquidStack drain(Orientations from, int maxEmpty, boolean doDrain)
    {
        return drain(0, maxEmpty, doDrain);
    }

    @Override
    public LiquidStack drain(int tankIndex, int maxEmpty, boolean doDrain)
    {
        TileTank bottom=getBottomTank();
    	bottom.hasUpdate=true;
        return bottom.tank.drain(maxEmpty, doDrain);
    }

    @Override
    public ILiquidTank[] getTanks()
    {
        ILiquidTank compositeTank = new LiquidTank(tank.getCapacity());

        TileTank tile = getBottomTank();

        int capacity = tank.getCapacity();

        if(tile != null && tile.tank.getLiquid() != null) {
            compositeTank.setLiquid(tile.tank.getLiquid().copy());
        } else {
            return new ILiquidTank[]{compositeTank};
        }

        tile = getTankAbove(tile);

        while(tile != null){

            if(tile.tank.getLiquid() == null || !compositeTank.getLiquid().isLiquidEqual(tile.tank.getLiquid()))
                break;

            compositeTank.getLiquid().amount += tile.tank.getLiquid().amount;
            capacity += tile.tank.getCapacity();

            tile = getTankAbove(tile);
        }

        compositeTank.setCapacity(capacity);
        return new ILiquidTank[]{compositeTank};
    }
}
