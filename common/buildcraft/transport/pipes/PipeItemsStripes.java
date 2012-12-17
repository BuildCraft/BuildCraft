/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft.transport.pipes;

/*
 import java.util.ArrayList;
 import java.util.TreeMap;

 import buildcraft.BuildCraftBlockUtil;
 import buildcraft.BuildCraftTransport;
 import net.minecraftforge.common.ForgeDirection;
 import buildcraft.api.core.Position;
 import buildcraft.api.power.IPowerProvider;
 import buildcraft.api.power.IPowerReceptor;
 import buildcraft.api.power.PowerFramework;
 import buildcraft.api.power.PowerProvider;
 import buildcraft.api.transport.IPipedItem;
 import buildcraft.core.DefaultProps;
 import buildcraft.core.EntityPassiveItem;
 import buildcraft.core.Utils;
 import buildcraft.transport.BlockGenericPipe;
 import buildcraft.transport.EntityData;
 import buildcraft.transport.IItemTravelingHook;
 import buildcraft.transport.ItemPipe;
 import buildcraft.transport.Pipe;
 import buildcraft.transport.PipeLogicStripes;
 import buildcraft.transport.PipeTransportItems;

 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;

 public class PipeItemsStripes extends Pipe implements IItemTravelingHook, IPowerReceptor {

 private IPowerProvider powerProvider;

 public PipeItemsStripes(int itemID) {
 super(new PipeTransportItems(), new PipeLogicStripes(), itemID);

 ((PipeTransportItems) transport).travelHook = this;

 powerProvider = PowerFramework.currentFramework.createPowerProvider();
 powerProvider.configure(25, 1, 1, 1, 1);
 powerProvider.configurePowerPerdition(1, 1);
 }

 @Override
 public String getTextureFile() {
 return DefaultProps.TEXTURE_BLOCKS;
 }

 @Override
 public int getTextureIndex(ForgeDirection direction) {
 return 16 * 7 + 14;
 }


 @Override
 public void doWork() {
 if (powerProvider.useEnergy(1, 1, true) == 1) {
 ForgeDirection o = getOpenOrientation();

 if (o != ForgeDirection.Unknown) {
 Position p = new Position(xCoord, yCoord, zCoord, o);
 p.moveForwards(1.0);

 ArrayList<ItemStack> stacks = BuildCraftBlockUtil
 .getItemStackFromBlock(worldObj, (int) p.x, (int) p.y, (int) p.z);

 if (stacks != null)
 for (ItemStack s : stacks)
 if (s != null) {
 IPipedItem newItem = new EntityPassiveItem(worldObj, xCoord + 0.5, yCoord
 + Utils.getPipeFloorOf(s), zCoord + 0.5, s);

 this.container.entityEntering(newItem, o.reverse());
 }

 worldObj.setBlock((int) p.x, (int) p.y, (int) p.z, 0);
 }
 }

 }

 @Override
 public void drop(PipeTransportItems pipe, EntityData data) {
 Position p = new Position(xCoord, yCoord, zCoord, data.orientation);
 p.moveForwards(1.0);

 if (convertPipe(pipe, data))
 BuildCraftTransport.pipeItemsStipes.onItemUse(new ItemStack(BuildCraftTransport.pipeItemsStipes),
 CoreProxy.getBuildCraftPlayer(worldObj), worldObj, (int) p.x, (int) p.y - 1, (int) p.z, 1);
 else if (worldObj.getBlockId((int) p.x, (int) p.y, (int) p.z) == 0)
 data.item.getItemStack().getItem().tryPlaceIntoWorld(data.item.getItemStack(), CoreProxy.getBuildCraftPlayer(worldObj), worldObj, (int) p.x,
 (int) p.y - 1, (int) p.z, 1, 0.0f, 0.0f, 0.0f);
 else
 data.item.getItemStack().getItem().tryPlaceIntoWorld(data.item.getItemStack(), CoreProxy.getBuildCraftPlayer(worldObj), worldObj, (int) p.x,
 (int) p.y, (int) p.z, 1, 0.0f, 0.0f, 0.0f);
 }

 @Override
 public void centerReached(PipeTransportItems pipe, EntityData data) {
 convertPipe(pipe, data);
 }

 @SuppressWarnings("unchecked")
 public boolean convertPipe(PipeTransportItems pipe, EntityData data) {

 if (data.item.getItemStack().getItem() instanceof ItemPipe)

 if (!(data.item.getItemStack().itemID == BuildCraftTransport.pipeItemsStipes.shiftedIndex)) {

 Pipe newPipe = BlockGenericPipe.createPipe(data.item.getItemStack().itemID);
 newPipe.setTile(this.container);
 this.container.pipe = newPipe;
 ((PipeTransportItems) newPipe.transport).travelingEntities = (TreeMap<Integer, EntityData>) pipe.travelingEntities
 .clone();

 data.item.getItemStack().stackSize--;

 if (data.item.getItemStack().stackSize <= 0)
 ((PipeTransportItems) newPipe.transport).travelingEntities.remove(data.item.getEntityId());

 pipe.scheduleRemoval(data.item);

 return true;
 }

 return false;
 }

 @Override
 public void setPowerProvider(IPowerProvider provider) {
 powerProvider = provider;
 }

 @Override
 public IPowerProvider getPowerProvider() {
 return powerProvider;
 }

 @Override
 public int powerRequest() {
 return getPowerProvider().getMaxEnergyReceived();
 }

 @Override
 public void endReached(PipeTransportItems pipe, EntityData data, TileEntity tile) {

 }
 }
 */
