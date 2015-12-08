package buildcraft.transport.stripes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import buildcraft.BuildCraftTransport;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.utils.TransportUtils;

public class PipeExtensionListener {
    private class PipeExtensionRequest {
        public ItemStack stack;
        public BlockPos pos;
        public EnumFacing o;
        public IStripesActivator h;
    }

    private final Map<World, HashSet<PipeExtensionRequest>> requests = new HashMap<World, HashSet<PipeExtensionRequest>>();

    public void requestPipeExtension(ItemStack stack, World world, BlockPos pos, EnumFacing o, IStripesActivator h) {
        if (world.isRemote) {
            return;
        }

        if (!requests.containsKey(world)) {
            requests.put(world, new HashSet<PipeExtensionRequest>());
        }
        PipeExtensionRequest r = new PipeExtensionRequest();
        r.stack = stack;
        r.pos = pos;
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
                Vec3 target = Utils.convert(r.pos);

                boolean retract = r.stack.getItem() == BuildCraftTransport.pipeItemsVoid;
                List<ItemStack> removedPipeStacks = null;

                if (retract) {
                    target = target.add(Utils.convert(r.o, -1));
                } else {
                    target = target.add(Utils.convert(r.o, 1));
                    if (!w.isAirBlock(Utils.convertFloor(target))) {
                        r.h.sendItem(r.stack, r.o.getOpposite());
                        continue;
                    }
                }

                // Step 1: Copy over and remove existing pipe
                IBlockState oldState = w.getBlockState(r.pos);
                NBTTagCompound nbt = new NBTTagCompound();
                w.getTileEntity(r.pos).writeToNBT(nbt);
                w.setBlockToAir(r.pos);

                boolean failedPlacement = false;

                // Step 2: If retracting, remove previous pipe; if extending, add new pipe
                BlockPos targetPos = Utils.convertFloor(target);
                if (retract) {
                    removedPipeStacks = w.getBlockState(targetPos).getBlock().getDrops(w, targetPos, w.getBlockState(targetPos), 0);

                    w.setBlockToAir(targetPos);
                } else {
                    r.stack.getItem().onItemUse(r.stack, CoreProxy.proxy.getBuildCraftPlayer((WorldServer) w, r.pos).get(), w, r.pos, EnumFacing.UP,
                            0, 0, 0);
                }

                // Step 3: Place stripes pipe back
                // - Correct NBT coordinates
                nbt.setInteger("x", MathHelper.floor_double(target.xCoord));
                nbt.setInteger("y", MathHelper.floor_double(target.yCoord));
                nbt.setInteger("z", MathHelper.floor_double(target.zCoord));
                // - Create block and tile
                TileGenericPipe pipeTile = (TileGenericPipe) TileEntity.createAndLoadEntity(nbt);

                w.setBlockState(targetPos, oldState, 3);
                w.setTileEntity(targetPos, pipeTile);

                pipeTile.setWorldObj(w);
                pipeTile.validate();
                pipeTile.update();

                // Step 4: Hope for the best, clean up.
                PipeTransportItems items = (PipeTransportItems) ((Pipe) pipeTile.getPipe()).transport;
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
                    TileGenericPipe newPipeTile = (TileGenericPipe) w.getTileEntity(r.pos);
                    newPipeTile.update();
                    pipeTile.scheduleNeighborChange();
                    if (pipeTile.getPipe() != null) {
                        ((Pipe) pipeTile.getPipe()).scheduleWireUpdate();
                    }
                }
            }
            rSet.clear();
        }
    }

    private void sendItem(PipeTransportItems transport, ItemStack itemStack, EnumFacing direction) {
        Vec3 pos = Utils.convert(transport.container.getPos());
        pos = pos.addVector(0.5, TransportUtils.getPipeFloorOf(itemStack), 0.5);
        TravelingItem newItem = TravelingItem.make(pos, itemStack);
        transport.injectItem(newItem, direction);
    }
}
