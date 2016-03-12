package buildcraft.transport.stripes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.utils.TransportUtils;

public class PipeExtensionListener {
	private class PipeExtensionRequest {
		public ItemStack stack;
		public int x, y, z;
		public ForgeDirection o;
		public IStripesActivator h;
	}

	private final Map<World, HashSet<PipeExtensionRequest>> requests = new HashMap<World, HashSet<PipeExtensionRequest>>();

	public void requestPipeExtension(ItemStack stack, World world, int x, int y, int z, ForgeDirection o, IStripesActivator h) {
		if (world.isRemote) {
			return;
		}

		if (!requests.containsKey(world)) {
			requests.put(world, new HashSet<PipeExtensionRequest>());
		}
		PipeExtensionRequest r = new PipeExtensionRequest();
		r.stack = stack;
		r.x = x;
		r.y = y;
		r.z = z;
		r.o = o;
		r.h = h;
		requests.get(world).add(r);
	}

	@SubscribeEvent
	public void tick(TickEvent.WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.END && requests.containsKey(event.world)) {
			HashSet<PipeExtensionRequest> rSet = requests.get(event.world);
			World w = event.world;
			for (PipeExtensionRequest r : rSet) {
				Position target = new Position(r.x, r.y, r.z);
				target.orientation = r.o;

				boolean retract = r.stack.getItem() == BuildCraftTransport.pipeItemsVoid;
				ArrayList<ItemStack> removedPipeStacks = null;

				if (retract) {
					target.moveBackwards(1.0D);
					if (w.getBlock((int) target.x, (int) target.y, (int) target.z) != BuildCraftTransport.genericPipeBlock) {
						r.h.sendItem(r.stack, r.o.getOpposite());
						continue;
					}

					target.moveBackwards(1.0D);
					if (w.getBlock((int) target.x, (int) target.y, (int) target.z) != BuildCraftTransport.genericPipeBlock) {
						r.h.sendItem(r.stack, r.o.getOpposite());
						continue;
					}

					target.moveForwards(1.0D);
				} else {
					target.moveForwards(1.0D);
					if (!w.isAirBlock((int) target.x, (int) target.y, (int) target.z)) {
						r.h.sendItem(r.stack, r.o.getOpposite());
						continue;
					}
				}

				// Step	1: Copy over and remove existing pipe
				Block oldBlock = w.getBlock(r.x, r.y, r.z);
				int oldMeta = w.getBlockMetadata(r.x, r.y, r.z);
				NBTTagCompound nbt = new NBTTagCompound();
				w.getTileEntity(r.x, r.y, r.z).writeToNBT(nbt);
				w.setBlockToAir(r.x, r.y, r.z);

				boolean failedPlacement = false;

				// Step 2: If retracting, remove previous pipe; if extending, add new pipe
				if (retract) {
					removedPipeStacks = w.getBlock((int) target.x, (int) target.y, (int) target.z).getDrops(w, (int) target.x, (int) target.y, (int) target.z,
							w.getBlockMetadata((int) target.x, (int) target.y, (int) target.z), 0);

					w.setBlockToAir((int) target.x, (int) target.y, (int) target.z);
				} else {
					if (!r.stack.getItem().onItemUse(r.stack,
							CoreProxy.proxy.getBuildCraftPlayer((WorldServer) w, r.x, r.y, r.z).get(),
							w, r.x, r.y, r.z, 1, 0, 0, 0)) {
						failedPlacement = true;
						target.moveBackwards(1.0D);
					}
				}

				// Step 3: Place stripes pipe back
				// - Correct NBT coordinates
				nbt.setInteger("x", (int) target.x);
				nbt.setInteger("y", (int) target.y);
				nbt.setInteger("z", (int) target.z);
				// - Create block and tile
				TileGenericPipe pipeTile = (TileGenericPipe) TileEntity.createAndLoadEntity(nbt);

				w.setBlock((int) target.x, (int) target.y, (int) target.z, oldBlock, oldMeta, 3);
				w.setTileEntity((int) target.x, (int) target.y, (int) target.z, pipeTile);

				pipeTile.setWorldObj(w);
				pipeTile.validate();
				pipeTile.updateEntity();

				// Step 4: Hope for the best, clean up.
				PipeTransportItems items = (PipeTransportItems) pipeTile.pipe.transport;
				if (!retract && !failedPlacement) {
					r.stack.stackSize--;
				}

				if (r.stack.stackSize > 0) {
					sendItem(items, r.stack, r.o.getOpposite());
				}
				if (removedPipeStacks != null) {
					for (ItemStack s : removedPipeStacks) {
						sendItem(items, s, r.o.getOpposite());
					}
				}

				if (!retract && !failedPlacement) {
					TileGenericPipe newPipeTile = (TileGenericPipe) w.getTileEntity(r.x, r.y, r.z);
					newPipeTile.updateEntity();
					pipeTile.scheduleNeighborChange();
					if (pipeTile.getPipe() != null) {
						((Pipe) pipeTile.getPipe()).scheduleWireUpdate();
					}
				}
			}
			rSet.clear();
		}
	}

	private void sendItem(PipeTransportItems transport, ItemStack itemStack, ForgeDirection direction) {
		TravelingItem newItem = TravelingItem.make(
				transport.container.xCoord + 0.5,
				transport.container.yCoord + TransportUtils.getPipeFloorOf(itemStack),
				transport.container.zCoord + 0.5, itemStack);
		transport.injectItem(newItem, direction);
	}
}
