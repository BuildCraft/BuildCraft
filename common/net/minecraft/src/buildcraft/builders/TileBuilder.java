/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.builders;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.BuildCraftBuilders;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.LaserKind;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.PowerFramework;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.api.TileNetworkData;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.Box;
import net.minecraft.src.buildcraft.core.BptBase;
import net.minecraft.src.buildcraft.core.BptBlueprint;
import net.minecraft.src.buildcraft.core.BptBuilderBase;
import net.minecraft.src.buildcraft.core.BptBuilderBlueprint;
import net.minecraft.src.buildcraft.core.BptBuilderTemplate;
import net.minecraft.src.buildcraft.core.BptContext;
import net.minecraft.src.buildcraft.core.EntityLaser;
import net.minecraft.src.buildcraft.core.EntityRobot;
import net.minecraft.src.buildcraft.core.IBuilderInventory;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.SurroundingInventory;
import net.minecraft.src.buildcraft.core.TileBuildCraft;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.core.network.PacketUpdate;

public class TileBuilder extends TileBuildCraft implements IBuilderInventory, IPowerReceptor, IMachine {
	
	private final ItemStack items [] = new ItemStack [28];
	
	private BptBuilderBase bluePrintBuilder;
	
	public @TileNetworkData Box box = new Box ();
	
	private PowerProvider powerProvider;
	
	private LinkedList <BlockIndex> path;
	
	private LinkedList <EntityLaser> pathLasers;
	
	private EntityRobot builderRobot;
	
	private class PathIterator {
		public Iterator <BlockIndex> currentIterator;
		public double cx, cy, cz;
		public float ix, iy, iz;
		public BlockIndex to;
		public double lastDistance;
		AxisAlignedBB oldBoundingBox = null;
		Orientations o = null;
		
		public PathIterator (BlockIndex from, Iterator <BlockIndex> it) {
			this.to = it.next();
			
			currentIterator = it;
			
			double dx = to.i - from.i;
			double dy = to.j - from.j;
			double dz = to.k - from.k;
			
			double size = Math.sqrt(dx * dx + dy * dy + dz * dz);
			
			cx = dx / size / 10;
			cy = dy / size / 10;
			cz = dz / size / 10;
			
			ix = from.i;
			iy = from.j;
			iz = from.k;
			
			lastDistance = (ix - to.i) * (ix - to.i) + (iy - to.j)
					* (iy - to.j) + (iz - to.k) * (iz - to.k);
			
			if (Math.abs(dx) > Math.abs(dz)) {
				if (dx > 0) {
					o = Orientations.XPos;
				} else {
					o = Orientations.XNeg;
				}
			} else {
				if (dz > 0) {
					o = Orientations.ZPos;
				} else {
					o = Orientations.ZNeg;
				}
			}
		}
		
		/**
		 * Return false when reached the end of the iteration
		 */
		public BptBuilderBase next () {
			while (true) {
				BptBuilderBase bpt;
				
				int newX = Math.round(ix);
				int newY = Math.round(iy);
				int newZ = Math.round(iz);
			
				bpt = instanciateBluePrint(newX, newY, newZ, o);

				if (bpt == null) {
					return null;
				}
			
				AxisAlignedBB boundingBox = bpt.getBoundingBox();
			
				if (oldBoundingBox == null
						|| !collision (oldBoundingBox, boundingBox)) {

					oldBoundingBox = boundingBox;
					
					if (bpt != null) {
						return bpt;
					}					
				}

				ix += cx;
				iy += cy;
				iz += cz;
			
				double distance = (ix - to.i)  * (ix - to.i)  + (iy - to.j) * (iy - to.j) + (iz - to.k) * (iz - to.k);

				if (distance > lastDistance) {
					return null;
				} else {
					lastDistance = distance;
				}		
			}			
		}
		
		public PathIterator iterate () {			
			if (currentIterator.hasNext()) {				
				PathIterator next = new PathIterator(to, currentIterator);
				next.oldBoundingBox = oldBoundingBox;
				
				return next;
			} else {
				return null;
			}
		}
		
		public boolean collision(AxisAlignedBB left, AxisAlignedBB right) {
			if (left.maxX < right.minX || left.minX > right.maxX) {
				return false;
			}
			if (left.maxY < right.minY || left.minY > right.maxY) {
				return false;
			}
			if (left.maxZ < right.minZ || left.minZ > right.maxZ) {
				return false;
			}
			return true;
		}
	}
	
	public PathIterator currentPathIterator;

	private boolean done = true;
	
	public TileBuilder () {
		super ();
		
		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(10, 25, 25, 25, 25);
	}
	
	@Override
	public void initialize () {
		super.initialize();
		
		for (int x = xCoord - 1; x <= xCoord + 1; ++x) {
			for (int y = yCoord - 1; y <= yCoord + 1; ++y) {
				for (int z = zCoord - 1; z <= zCoord + 1; ++z) {
					TileEntity tile = worldObj.getBlockTileEntity(x, y, z);
					
					if (tile instanceof TilePathMarker) {
						path = ((TilePathMarker) tile).getPath();
						
						for (BlockIndex b : path) {
							worldObj.setBlockWithNotify(b.i, b.j, b.k, 0);							
							
							BuildCraftBuilders.pathMarkerBlock.dropBlockAsItem(worldObj,
									b.i, b.j, b.k,
									BuildCraftBuilders.pathMarkerBlock.blockID, 0);
						}						
						
						break;
					}
				}
			}
		}
		
		if (path != null && pathLasers == null) {
			path.getFirst().i = xCoord;
			path.getFirst().j = yCoord;
			path.getFirst().k = zCoord;
			
			createLasersForPath();
		}
		
		iterateBpt();
	}
	
	public void createLasersForPath () {
		pathLasers = new LinkedList<EntityLaser>();
		BlockIndex previous = null;
		
		for (BlockIndex b : path) {
			if (previous != null) {
				EntityLaser laser = new EntityLaser(worldObj);
				
				laser.setPositions(previous.i + 0.5,
						previous.j + 0.5, previous.k + 0.5,
						b.i + 0.5, b.j + 0.5, b.k + 0.5);
				laser.setTexture("/net/minecraft/src/buildcraft/core/gui/stripes.png");
				worldObj.spawnEntityInWorld(laser);
				pathLasers.add(laser);
			}
			
			previous = b;
		}
	}
	
	public BptBuilderBase instanciateBluePrint (int x, int y, int z, Orientations o) {						
		BptBase bpt = BuildCraftBuilders.getBptRootIndex().getBluePrint(items[0]
				.getItemDamage());

		if (bpt == null) {
			return null;
		}
		
		bpt = bpt.clone ();
		
		BptContext context = new BptContext(worldObj, null, bpt.getBoxForPos (x, y, z));
		
		if (o == Orientations.XPos) {
			// Do nothing
		} else if (o == Orientations.ZPos) {
			bpt.rotateLeft(context);
		} else if (o == Orientations.XNeg) {
			bpt.rotateLeft(context);
			bpt.rotateLeft(context);
		} else if (o == Orientations.ZNeg) {
			bpt.rotateLeft(context);
			bpt.rotateLeft(context);
			bpt.rotateLeft(context);
		}

		if (items [0].getItem() instanceof ItemBptTemplate) {
			return new BptBuilderTemplate(bpt, worldObj, x, y, z);
		} else if (items [0].getItem() instanceof ItemBptBluePrint) { 
			return new BptBuilderBlueprint((BptBlueprint) bpt, worldObj, x, y,
					z);
		} else {
			return null;
		}
	}
	
	@Override
	public void doWork() {
		if (APIProxy.isClient(worldObj)) {
			return;
		}
		
		if (done) {
			return;
		}
		
		if (builderRobot != null && !builderRobot.readyToBuild()) {
			return;
		}

		if (powerProvider.useEnergy(25, 25, true) < 25) {
			return;
		}
		
		iterateBpt ();

		if (bluePrintBuilder != null && !bluePrintBuilder.done) {
			if (!box.isInitialized()) {
				box.initialize(bluePrintBuilder);
			}
			
			if (builderRobot == null) {
				builderRobot = new EntityRobot(worldObj, box);
				worldObj.spawnEntityInWorld(builderRobot);
			}
			
			box.createLasers(worldObj, LaserKind.Stripes);
			
			builderRobot.scheduleContruction(bluePrintBuilder.getNextBlock(
					worldObj, new SurroundingInventory(worldObj, xCoord,
							yCoord, zCoord)),bluePrintBuilder.getContext());
		}		
	}
	
	public void iterateBpt () {
		if (items[0] == null
				|| !(items[0].getItem() instanceof ItemBptBase)) {
		
			if (bluePrintBuilder != null) {
				bluePrintBuilder = null;
			}
			
			if (builderRobot != null) {
				builderRobot.setDead();
				builderRobot = null;
			}
			
			if (box.isInitialized()) {
				box.deleteLasers();
				box.reset();
			}
			
			if (currentPathIterator != null) {				
				currentPathIterator = null;
			}
			
			return;
		}
		
		if (bluePrintBuilder == null || bluePrintBuilder.done) {
			if (path != null) {
				if (currentPathIterator == null) {
					Iterator<BlockIndex> it = path.iterator();
					BlockIndex start = it.next();
					currentPathIterator = new PathIterator(start, it);
				}
				
				if (bluePrintBuilder != null && builderRobot != null) {
					builderRobot.markEndOfBlueprint(bluePrintBuilder);
				}
				
				bluePrintBuilder = currentPathIterator.next();
				
				if (bluePrintBuilder != null) {
					box.deleteLasers();
					box.reset();
					box.initialize(bluePrintBuilder);
					box.createLasers(worldObj, LaserKind.Stripes);
				}
				
				if (builderRobot != null) {
					builderRobot.setBox (box);
				}

				if (bluePrintBuilder == null) {
					currentPathIterator = currentPathIterator.iterate();
				} 
				
				if (currentPathIterator == null) {
					done = true;
				}
			} else {
				if (bluePrintBuilder != null && bluePrintBuilder.done) {					
					if (builderRobot != null) {
						builderRobot.markEndOfBlueprint(bluePrintBuilder);
					}
					
					done = true;
					bluePrintBuilder = null;
				} else {
					bluePrintBuilder = instanciateBluePrint(xCoord, yCoord,
							zCoord,
							Orientations.values()[worldObj.getBlockMetadata(
									xCoord, yCoord, zCoord)].reverse());
					
					if (bluePrintBuilder != null) {
						box.initialize(bluePrintBuilder);
						box.createLasers(worldObj, LaserKind.Stripes);
					}
				}
			}
		}		
	}

	@Override
	public int getSizeInventory() {
		return items.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return items [i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		ItemStack result;
		if (items [i] == null) {
			result = null;
		} else if (items [i].stackSize > j) {
			result = items [i].splitStack(j);
		} else {
			ItemStack tmp = items [i];
			items [i] = null;
			result = tmp;
		}
		
		if (i == 0) {
			iterateBpt();
		}
		
		return result;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		items [i] = itemstack;
		
		if (i == 0) {
			iterateBpt ();
			done = false;
		}		
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		if(items[slot] == null)
			return null;
		ItemStack toReturn = items[slot];
		items[slot] = null;
		return toReturn;
	}

	@Override
	public String getInvName() {
		return "Builder";
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readFromNBT(nbttagcompound);
        
        Utils.readStacksFromNBT(nbttagcompound, "Items", items);
        
        if (nbttagcompound.hasKey("box")) {
        	box.initialize(nbttagcompound.getCompoundTag("box"));
        }
        
        if (nbttagcompound.hasKey("path")) {
        	path = new LinkedList<BlockIndex>();
        	NBTTagList list = nbttagcompound.getTagList("path");
        	
        	for (int i = 0; i < list.tagCount(); ++i) {
        		path.add(new BlockIndex((NBTTagCompound) list.tagAt(i)));
        	}
        }
        
        done = nbttagcompound.getBoolean("done");

    }

	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeToNBT(nbttagcompound);
        
        Utils.writeStacksToNBT(nbttagcompound, "Items", items);
        
        if (box.isInitialized()) {
        	NBTTagCompound boxStore = new NBTTagCompound();
        	box.writeToNBT(boxStore);
        	nbttagcompound.setTag("box", boxStore);
        }
        
        if (path != null) {
        	NBTTagList list = new NBTTagList();

        	for (BlockIndex i : path) {
        		NBTTagCompound c = new NBTTagCompound();
        		i.writeTo(c);
        		list.appendTag(c);
        	}
        	
			nbttagcompound.setTag("path", list);
        }
        
        nbttagcompound.setBoolean("done", done);
    }

    @Override
    public void invalidate () {
    	destroy ();
    }
    
    @Override
	public void destroy() {		
		if (box.isInitialized()) {
			box.deleteLasers();
		}
		
		if (builderRobot != null) {
			builderRobot.setDead();
			builderRobot = null;
		}
		
		cleanPathLasers();
	}

	@Override
	public void setPowerProvider(PowerProvider provider) {
		powerProvider = provider;
	}

	@Override
	public PowerProvider getPowerProvider() {
		return powerProvider;
	}
	
	@Override
	public void handleDescriptionPacket(PacketUpdate packet) {
		boolean initialized = box.isInitialized();
		
		super.handleDescriptionPacket(packet);		
		
		if (!initialized && box.isInitialized()) {
			box.createLasers(worldObj, LaserKind.Stripes);			
		}
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) {
		boolean initialized = box.isInitialized();
		
		super.handleUpdatePacket(packet);
		
		if (!initialized && box.isInitialized()) {
			box.createLasers(worldObj, LaserKind.Stripes);
		}
	}

	@Override
	public void openChest() {
		
	}

	@Override
	public void closeChest() {
		
	}

	@Override
	public int powerRequest() {
		if ((bluePrintBuilder != null || currentPathIterator != null) && !done) {
			return powerProvider.maxEnergyReceived;
		} else {
			return 0;
		}
	}
	
	@Override
	public void updateEntity () {
		
		super.updateEntity();
		
		if ((bluePrintBuilder == null || bluePrintBuilder.done)
				&& box.isInitialized() 
				&& (builderRobot == null || builderRobot.done())) {
			
			box.deleteLasers();
			box.reset();

			if (APIProxy.isServerSide()) {
				sendNetworkUpdate();
			}

			return;
		}
		
		if (!box.isInitialized() && bluePrintBuilder == null && builderRobot != null) {	
			builderRobot.setDead();
			builderRobot = null;
		}
	}

	@Override
	public boolean isActive() {
		return !done;
	}

	@Override
	public boolean manageLiquids() {
		return false;
	}

	@Override
	public boolean manageSolids() {
		return true;
	}
	
	public void cleanPathLasers () {
		if (pathLasers != null) {
			for (EntityLaser laser : pathLasers) {
				laser.setDead();			
			}

			pathLasers = null;
		}
	}
	
	public boolean isBuildingBlueprint() {
		return getStackInSlot(0) != null
				&& getStackInSlot(0).getItem() instanceof ItemBptBluePrint;
	}
	
	public Collection <ItemStack> getNeededItems () {
		if (bluePrintBuilder instanceof BptBuilderBlueprint) {
			return ((BptBuilderBlueprint) bluePrintBuilder).neededItems;
		} else {
			return null;
		}
	}

	@Override
	public boolean isBuildingMaterial(int i) {
		return i != 0;
	}
	
	@Override
	public boolean allowActions () {
		return false;
	}
}
